package com.group1.MockProject.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.group1.MockProject.entity.Instructor;
import com.group1.MockProject.entity.User;
import com.group1.MockProject.exception.GlobalExceptionHandler;
import com.group1.MockProject.repository.InstructorRepository;
import com.group1.MockProject.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

  private MockMvc mockMvc;

  @Mock private SecurityContextHolder securityContextHolder;

  @Mock private InstructorRepository instructorRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private DashboardController dashboardController;

  private Instructor mockInstructor;
  private User mockUserIsInstructor;
  private String instructorEmail;

  @BeforeEach
  void setup() {
    dashboardController = new DashboardController(instructorRepository, userRepository);

    instructorEmail = "instructor@example.com";

    mockInstructor = new Instructor();
    mockUserIsInstructor = new User();
    mockUserIsInstructor.setEmail(instructorEmail);
    mockUserIsInstructor.setInstructor(mockInstructor);
    mockInstructor.setUser(mockUserIsInstructor);

    this.mockMvc =
        MockMvcBuilders.standaloneSetup(dashboardController)
            .alwaysDo(print())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  public void testGetInstructorDashboard_Success() throws Exception {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(authentication.getName()).thenReturn(instructorEmail);
    Mockito.when(userRepository.findByEmail(Mockito.eq(instructorEmail)))
        .thenReturn(Optional.of(mockUserIsInstructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/dashboard/instructor")
                .contentType("application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists());
  }

  @Test
  public void testGetInstructorDashboard_UserNotFound() throws Exception {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(authentication.getName()).thenReturn(instructorEmail);
    Mockito.when(userRepository.findByEmail(Mockito.eq(instructorEmail)))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/dashboard/instructor")
                .contentType("application/json"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Not Found"))
        .andExpect(jsonPath("$.response.message").value("Không tìm thấy người dùng"));
  }

  @Test
  public void testGetInstructorDashboard_DoNotHaveAccess() throws Exception {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);
    mockUserIsInstructor.setInstructor(null);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(authentication.getName()).thenReturn(instructorEmail);
    Mockito.when(userRepository.findByEmail(Mockito.eq(instructorEmail)))
        .thenReturn(Optional.of(mockUserIsInstructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/dashboard/instructor")
                .contentType("application/json"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(jsonPath("$.response.message").value("Không có quyền truy cập"));
  }

  @Test
  public void testViewStudentDashboard_Success() throws Exception {

    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"));
    Authentication auth = new UsernamePasswordAuthenticationToken("student", null, authorities);
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(auth);
    SecurityContextHolder.setContext(securityContext);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/dashboard/student").contentType("application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response.totalCourses").value(10))
        .andExpect(jsonPath("$.response.totalEnrollments").value(15))
        .andExpect(jsonPath("$.response.totalPurchasedCourses").value(20));
  }

  @Test
  public void testViewStudentDashboard_NotHaveAccess() throws Exception {

    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"));
    Authentication auth = new UsernamePasswordAuthenticationToken("instructor", null, authorities);
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(auth);
    SecurityContextHolder.setContext(securityContext);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/dashboard/student").contentType("application/json"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(jsonPath("$.response.message").value("Bạn không có quyền để xem nội dung này"));
  }

  @Test
  public void testViewStudentDashboard_Unauthorized() throws Exception {
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(null);
    SecurityContextHolder.setContext(securityContext);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/dashboard/student").contentType("application/json"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"))
        .andExpect(
            jsonPath("$.response.message")
                .value("Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục"));
  }
}
