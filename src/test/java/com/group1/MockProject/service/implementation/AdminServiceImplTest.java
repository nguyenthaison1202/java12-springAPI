package com.group1.MockProject.service.implementation;

import static org.junit.jupiter.api.Assertions.*;

import com.group1.MockProject.dto.MessageDTO;
import com.group1.MockProject.dto.request.SignInRequest;
import com.group1.MockProject.dto.response.AdminDashboardResponse;
import com.group1.MockProject.dto.response.SignInResponse;
import com.group1.MockProject.entity.User;
import com.group1.MockProject.entity.UserRole;
import com.group1.MockProject.repository.CourseRepository;
import com.group1.MockProject.repository.StudentRepository;
import com.group1.MockProject.repository.UserRepository;
import com.group1.MockProject.utils.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {
  @Mock private CourseRepository courseRepository;

  @Mock private UserRepository userRepository;

  @Mock private StudentRepository studentRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AdminServiceImpl adminService;

  @Test
  public void testGetAllUser_Success() {
    User mockUser = new User();

    Mockito.when(userRepository.findAll()).thenReturn(List.of(mockUser));

    List<User> result = adminService.getAllUsers();

    Assertions.assertNotNull(result);
  }

  @Test
  public void testDeleteUser_Success() {
    int mockUserId = 1;

    adminService.deleteUser(mockUserId);

    Mockito.verify(userRepository, Mockito.times(1)).deleteById(Mockito.eq(mockUserId));
  }

  @Test
  public void testGetDashboardData_Success() {
    long totalUser = 10;
    long totalStudent = 10;
    long totalCourse = 10;
    long totalInstructor = 10;
    long newUsersThisMonth = 10;
    double totalRevenue = 10.0;
    double averageRating = 10.0;
    long totalReview = 10;

    Mockito.when(userRepository.count()).thenReturn(totalUser);
    Mockito.when(studentRepository.count()).thenReturn(totalStudent);
    Mockito.when(userRepository.countByRole(Mockito.eq(UserRole.INSTRUCTOR)))
        .thenReturn(totalInstructor);
    Mockito.when(userRepository.countByCreatedDateAfter(Mockito.any(LocalDateTime.class)))
        .thenReturn(newUsersThisMonth);
    Mockito.when(courseRepository.count()).thenReturn(totalCourse);
    Mockito.when(courseRepository.calculateTotalRevenue()).thenReturn(totalRevenue);
    Mockito.when(courseRepository.calculateAverageRating()).thenReturn(averageRating);
    Mockito.when(courseRepository.countTotalReviews()).thenReturn(totalReview);

    AdminDashboardResponse response = adminService.getDashboardData();

    Assertions.assertNotNull(response);
    Assertions.assertEquals(totalUser, response.getTotalUsers());
    Assertions.assertEquals(totalStudent, response.getTotalStudents());
    Assertions.assertEquals(totalInstructor, response.getTotalInstructors());
    Assertions.assertEquals(newUsersThisMonth, response.getNewUsersThisMonth());
    Assertions.assertEquals(totalCourse, response.getTotalCourses());
    Assertions.assertEquals(totalRevenue, response.getTotalRevenue());
    Assertions.assertEquals(averageRating, response.getAverageRating());
    Assertions.assertEquals(totalReview, response.getTotalReviews());
  }

  @Test
  public void testGetDashboardData_RuntimeException() {
    Mockito.when(userRepository.count()).thenThrow(RuntimeException.class);

    Exception exception =
        Assertions.assertThrows(RuntimeException.class, () -> adminService.getDashboardData());

    Assertions.assertEquals("Lỗi khi lấy dữ liệu dashboard: null", exception.getMessage());
  }

  @Test
  public void testSetRejectInstructor_Success() {
    int mockUserId = 1;
    User mockUser = new User();
    mockUser.setStatus(0);
    mockUser.setRole(UserRole.INSTRUCTOR);

    Mockito.when(userRepository.findById(Mockito.eq(mockUserId))).thenReturn(Optional.of(mockUser));

    MessageDTO result = adminService.setRejectInstructor(mockUserId);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Từ chối người dùng thành công", result.getMessage());
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
  }

  @Test
  public void testSetRejectInstructor_UserNotFound() {
    int mockUserId = 1;

    Mockito.when(userRepository.findById(Mockito.eq(mockUserId))).thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class,
            () -> adminService.setRejectInstructor(mockUserId));

    Assertions.assertEquals("Người dùng không tồn tại", exception.getMessage());
  }

  @Test
  public void testSetRejectInstructor_InstructorAlreadyRejected() {
    int mockUserId = 1;
    User mockUser = new User();
    mockUser.setStatus(-1);
    mockUser.setRole(UserRole.INSTRUCTOR);

    Mockito.when(userRepository.findById(Mockito.eq(mockUserId))).thenReturn(Optional.of(mockUser));

    Exception exception =
        Assertions.assertThrows(
            DataIntegrityViolationException.class,
            () -> adminService.setRejectInstructor(mockUserId));

    Assertions.assertEquals(
        "Người dùng đã bị từ chối, không thể từ chối lần nữa", exception.getMessage());
  }

  @Test
  public void testSetApproveInstructor_Success() {
    int mockUserId = 1;
    User mockUser = new User();
    mockUser.setStatus(0);
    mockUser.setRole(UserRole.INSTRUCTOR);

    Mockito.when(userRepository.findById(Mockito.eq(mockUserId))).thenReturn(Optional.of(mockUser));

    MessageDTO result = adminService.setApproveInstructor(mockUserId);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Duyệt người dùng thành công", result.getMessage());
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
  }

  @Test
  public void testSetApproveInstructor_UserNotFound() {
    int mockUserId = 1;

    Mockito.when(userRepository.findById(Mockito.eq(mockUserId))).thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class,
            () -> adminService.setApproveInstructor(mockUserId));

    Assertions.assertEquals("Người dùng không tồn tại", exception.getMessage());
  }

  @Test
  public void testSetApproveInstructor_InstructorAlreadyRejected() {
    int mockUserId = 1;
    User mockUser = new User();
    mockUser.setStatus(1);
    mockUser.setRole(UserRole.INSTRUCTOR);

    Mockito.when(userRepository.findById(Mockito.eq(mockUserId))).thenReturn(Optional.of(mockUser));

    Exception exception =
        Assertions.assertThrows(
            DataIntegrityViolationException.class,
            () -> adminService.setApproveInstructor(mockUserId));

    Assertions.assertEquals(
        "Người dùng đã được duyệt, không thể duyệt lần nữa", exception.getMessage());
  }

  @Test
  public void testAdminAuthentication_Success() {
    String mockEmail = "admin@email.com";
    String mockPassword = "<PASSWORD>";
    String mockToken = "token";

    User mockUser = new User();
    mockUser.setEmail(mockEmail);
    mockUser.setPassword(mockPassword);
    mockUser.setRole(UserRole.ADMIN);

    SignInRequest mockSignInRequest = new SignInRequest();
    mockSignInRequest.setEmail(mockEmail);
    mockSignInRequest.setPassword(mockPassword);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(
            passwordEncoder.matches(Mockito.eq(mockPassword), Mockito.eq(mockUser.getPassword())))
        .thenReturn(true);

    try (MockedStatic<JwtUtil> jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class)) {
      jwtUtilMockedStatic
          .when(() -> JwtUtil.generateToken(Mockito.any(User.class)))
          .thenReturn(mockToken);

      SignInResponse result = adminService.authenticate(mockSignInRequest);

      Assertions.assertNotNull(result);
    }
  }

  @Test
  public void testAdminAuthentication_WrongEmail() {
    String mockEmail = "admin@email.com";
    String mockPassword = "<PASSWORD>";

    SignInRequest mockSignInRequest = new SignInRequest();
    mockSignInRequest.setEmail(mockEmail);
    mockSignInRequest.setPassword(mockPassword);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail))).thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            BadCredentialsException.class, () -> adminService.authenticate(mockSignInRequest));

    Assertions.assertEquals("Sai email hoặc mật khẩu", exception.getMessage());
  }

  @Test
  public void testAdminAuthentication_WrongPassword() {
    String mockEmail = "admin@email.com";
    String mockPassword = "<PASSWORD>";

    User mockUser = new User();
    mockUser.setEmail(mockEmail);
    mockUser.setPassword(mockPassword);
    mockUser.setRole(UserRole.ADMIN);

    SignInRequest mockSignInRequest = new SignInRequest();
    mockSignInRequest.setEmail(mockEmail);
    mockSignInRequest.setPassword(mockPassword);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(
            passwordEncoder.matches(Mockito.eq(mockPassword), Mockito.eq(mockUser.getPassword())))
        .thenReturn(false);

    Exception exception =
        Assertions.assertThrows(
            BadCredentialsException.class, () -> adminService.authenticate(mockSignInRequest));

    Assertions.assertEquals("Sai email hoặc mật khẩu", exception.getMessage());
  }
}
