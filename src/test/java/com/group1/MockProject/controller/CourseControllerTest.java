package com.group1.MockProject.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.MockProject.dto.request.CourseRequest;
import com.group1.MockProject.dto.request.ReviewRequest;
import com.group1.MockProject.dto.response.CourseDTO;
import com.group1.MockProject.dto.response.RejectCourseResponse;
import com.group1.MockProject.dto.response.ReviewResponse;
import com.group1.MockProject.entity.*;
import com.group1.MockProject.exception.GlobalExceptionHandler;
import com.group1.MockProject.repository.*;
import com.group1.MockProject.service.CourseService;
import com.group1.MockProject.service.EnrollCourseService;
import com.group1.MockProject.service.ReviewCourseService;
import com.group1.MockProject.utils.JwtUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {
  private MockMvc mockMvc;

  @Mock private EnrollCourseService enrollCourseService;

  @Mock private ReviewCourseService reviewCourseService;

  @Mock private UserRepository userRepository;

  @Mock private StudentRepository studentRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private SavedCourseRepository savedCourseRepository;

  @Mock private InstructorRepository instructorRepository;

  @Mock private CourseService courseService;

  @Mock private PaymentRepository paymentRepository;

  @Mock private PaymentDetailRepository paymentDetailRepository;

  @InjectMocks private CourseController courseController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private MockedStatic<JwtUtil> jwtUtilMock;

  private final String mockToken = "mockToken";
  private final String mockEmail = "mock@email.com";

  @BeforeEach
  void setUp() {

    jwtUtilMock = Mockito.mockStatic(JwtUtil.class);
    jwtUtilMock.when(() -> JwtUtil.extractEmail(Mockito.eq(mockToken))).thenReturn(mockEmail);
    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(true);

    courseController =
        new CourseController(
            courseService,
            enrollCourseService,
            reviewCourseService,
            userRepository,
            studentRepository,
            courseRepository,
            savedCourseRepository,
            instructorRepository,
            paymentRepository,
            paymentDetailRepository);

    this.mockMvc =
        MockMvcBuilders.standaloneSetup(courseController)
            .alwaysDo(print())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @AfterEach
  void tearDown() {
    if (jwtUtilMock != null) {
      jwtUtilMock.close();
    }
  }

  @Test
  public void testCreateCourse_Success() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setStatus(1);
    instructor.setRole(UserRole.INSTRUCTOR);

    CourseDTO courseDTO = new CourseDTO();

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));
    Mockito.when(
            courseService.createCourse(Mockito.any(CourseRequest.class), Mockito.eq(mockToken)))
        .thenReturn(courseDTO);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.message").value("Created"))
        .andExpect(jsonPath("$.response").exists());
  }

  @Test
  public void testCreateCourse_TokenNotFound() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"));
  }

  @Test
  public void testCreateCourse_InvalidToken() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"));
  }

  @Test
  public void testCreateCourse_NotHaveAccess() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setRole(UserRole.STUDENT);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testCreateCourse_UserNotActive() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setRole(UserRole.INSTRUCTOR);

    CourseDTO courseDTO = new CourseDTO();

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(jsonPath("$.response.message").value("Bạn không có quyền để tạo khoá học"));
  }

  @Test
  public void testUpdateCourse_Success() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setStatus(1);
    instructor.setRole(UserRole.INSTRUCTOR);

    CourseDTO courseDTO = new CourseDTO();
    courseDTO.setId(1);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));
    Mockito.when(
            courseService.updateCourse(
                Mockito.eq(1), Mockito.any(CourseRequest.class), Mockito.eq(mockToken)))
        .thenReturn(courseDTO);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists());
  }

  @Test
  public void testUpdateCourse_TokenNotFound() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"));
  }

  @Test
  public void testUpdateCourse_InvalidToken() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"));
  }

  @Test
  public void testUpdateCourse_NotHaveAccess() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setRole(UserRole.STUDENT);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testUpdateCourse_UserNotActive() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setRole(UserRole.INSTRUCTOR);

    CourseDTO courseDTO = new CourseDTO();
    courseDTO.setId(1);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(
            jsonPath("$.response.message").value("Bạn không có quyền để cập nhật khoá học này"));
  }

  @Test
  public void testDeleteCourse_Success() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setStatus(1);
    instructor.setRole(UserRole.INSTRUCTOR);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$.status").value(204))
        .andExpect(jsonPath("$.message").value("No Content"));
  }

  @Test
  public void testDeleteCourse_TokenNotFound() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"));
  }

  @Test
  public void testDeleteCourse_InvalidToken() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"));
  }

  @Test
  public void testDeleteCourse_NotHaveAccess() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setRole(UserRole.STUDENT);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testDeleteCourse_UserNotActive() throws Exception {
    CourseRequest courseRequest = new CourseRequest();
    courseRequest.setTitle("Test Course");

    User instructor = new User();
    instructor.setId(1);
    instructor.setEmail(mockEmail);
    instructor.setRole(UserRole.INSTRUCTOR);

    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(instructor));

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete("/api/v1/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(courseRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(jsonPath("$.response.message").value("Bạn không có quyền để xoá khoá học này"));
  }

  @Test
  public void testGetCoursesByInstructor_Success() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.INSTRUCTOR);

    CourseDTO mockCourseDTO = new CourseDTO();

    Mockito.when(courseService.getCoursesByInstructor(Mockito.eq(mockToken)))
        .thenReturn(List.of(mockCourseDTO));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/get-all-courses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists());
  }

  @Test
  public void testEnrollCourse_Success() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Course mockCourse = new Course();
    mockCourse.setId(1);
    mockCourse.setTitle("Test Course");

    Payment mockPayment = new Payment();
    PaymentDetail mockPaymentDetail = new PaymentDetail();
    mockPaymentDetail.setCourse(mockCourse);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));
    Mockito.when(paymentRepository.findByStudent(Mockito.any(Student.class)))
        .thenReturn(mockPayment);
    Mockito.when(paymentDetailRepository.findByPayment(Mockito.any(Payment.class)))
        .thenReturn(List.of(mockPaymentDetail));

    String expectedResult = "Expected Result";

    Mockito.when(
            enrollCourseService.addEnroll(Mockito.any(Student.class), Mockito.any(Course.class)))
        .thenReturn(expectedResult);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/enrolled")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists())
        .andExpect(jsonPath("$.response.message").value(expectedResult));
  }

  @Test
  public void testEnrollCourse_UserNotActive() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(0);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/enrolled")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testEnrollCourse_NoCourseFound() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/enrolled")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Not Found"));
  }

  @Test
  public void testEnrollCourse_NotPaidForCourse() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Course mockCourse = new Course();
    mockCourse.setId(1);
    mockCourse.setTitle("Test Course");

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/enrolled")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isPaymentRequired())
        .andExpect(jsonPath("$.status").value(402))
        .andExpect(jsonPath("$.message").value("Payment Required"));
  }

  @Test
  public void testFinishedCourse_Success() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Course mockCourse = new Course();
    mockCourse.setId(1);
    mockCourse.setTitle("Test Course");

    SavedCourse mockSavedCourse = new SavedCourse();
    mockSavedCourse.setId(1);
    mockSavedCourse.setCourse(mockCourse);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));
    Mockito.when(savedCourseRepository.findByCourseAndStudent(mockCourse, mockStudent))
        .thenReturn(Optional.of(mockSavedCourse));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/finished")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists())
        .andExpect(
            jsonPath("$.response.message").value("Xin chúc mừng bạn đã hoàn thành khóa học."));
  }

  @Test
  public void testFinishedCourse_UserNotActive() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/finished")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testFinishedCourse_InvalidCourse() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/finished")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Not Found"));
  }

  @Test
  public void testFinishedCourse_AlreadyFinishedTheCourse() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Course mockCourse = new Course();
    mockCourse.setId(1);
    mockCourse.setTitle("Test Course");

    SavedCourse mockSavedCourse = new SavedCourse();
    mockSavedCourse.setId(1);
    mockSavedCourse.setCourse(mockCourse);
    mockSavedCourse.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));
    Mockito.when(savedCourseRepository.findByCourseAndStudent(mockCourse, mockStudent))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/finished")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value("Conflict"));
  }

  @Test
  public void testReviewCourse_Success() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Course mockCourse = new Course();
    mockCourse.setId(1);
    mockCourse.setTitle("Test Course");

    SavedCourse mockSavedCourse = new SavedCourse();
    mockSavedCourse.setId(1);
    mockSavedCourse.setCourse(mockCourse);
    mockSavedCourse.setStatus(1);

    ReviewRequest reviewRequest = new ReviewRequest();
    reviewRequest.setRating(5);
    reviewRequest.setComment("Test Comment");

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));
    Mockito.when(savedCourseRepository.findByCourseAndStudent(mockCourse, mockStudent))
        .thenReturn(Optional.of(mockSavedCourse));

    String expectedResult = "Expected Result";
    Mockito.when(
            reviewCourseService.addReview(
                Mockito.any(ReviewRequest.class),
                Mockito.any(Student.class),
                Mockito.any(Course.class)))
        .thenReturn(expectedResult);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(reviewRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists())
        .andExpect(jsonPath("$.response.message").value(expectedResult));
  }

  @Test
  public void testReviewCourse_UserNotActive() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);

    ReviewRequest reviewRequest = new ReviewRequest();
    reviewRequest.setRating(5);
    reviewRequest.setComment("Test Comment");

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(reviewRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testReviewCourse_InvalidCourse() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    ReviewRequest reviewRequest = new ReviewRequest();
    reviewRequest.setRating(5);
    reviewRequest.setComment("Test Comment");

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(reviewRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Not Found"));
  }

  @Test
  public void testReviewCourse_AlreadyFinishedTheCourse() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Student mockStudent = new Student();
    mockStudent.setId(1);
    mockStudent.setUser(mockUser);

    Course mockCourse = new Course();
    mockCourse.setId(1);
    mockCourse.setTitle("Test Course");

    SavedCourse mockSavedCourse = new SavedCourse();
    mockSavedCourse.setId(1);
    mockSavedCourse.setCourse(mockCourse);
    mockSavedCourse.setStatus(0);

    ReviewRequest reviewRequest = new ReviewRequest();
    reviewRequest.setRating(5);
    reviewRequest.setComment("Test Comment");

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(mockUser)).thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(1)).thenReturn(Optional.of(mockCourse));
    Mockito.when(savedCourseRepository.findByCourseAndStudent(mockCourse, mockStudent))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v1/courses/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(reviewRequest))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"));
  }

  @Test
  public void testGetAllReviewsOfInstructor_Success() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    User mockUserIsInstructor = new User();
    mockUserIsInstructor.setId(1);
    mockUserIsInstructor.setEmail(mockEmail);
    mockUserIsInstructor.setRole(UserRole.INSTRUCTOR);
    mockUserIsInstructor.setStatus(1);

    Instructor mockInstructor = new Instructor();
    mockInstructor.setId(1);
    mockInstructor.setUser(mockUserIsInstructor);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findById(1)).thenReturn(Optional.of(mockInstructor));

    List<ReviewResponse> response = List.of(new ReviewResponse());
    Mockito.when(reviewCourseService.getAllReviews(Mockito.any(Instructor.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists());
  }

  @Test
  public void testGetAllReviewsOfInstructor_InstructorNotFound() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findById(1)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Not Found"));
  }

  @Test
  public void testGetAllRejectCourses_Success() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.INSTRUCTOR);
    mockUser.setStatus(1);

    Instructor mockInstructor = new Instructor();
    mockInstructor.setId(1);
    mockInstructor.setUser(mockUser);
    mockUser.setInstructor(mockInstructor);

    RejectCourseResponse response = new RejectCourseResponse();

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findInstructorByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockInstructor));
    Mockito.when(
            courseService.viewAllRejectedCoursesByInstructor(Mockito.eq(mockInstructor.getId())))
        .thenReturn(List.of(response));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/rejects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.response").exists());
  }

  @Test
  public void testGetAllRejectCourses_InvalidToken() throws Exception {

    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/rejects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Unauthorized"))
        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ hoặc đã hết hạn"));
  }

  @Test
  public void testGetAllRejectCourses_UserNotFound() throws Exception {

    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.STUDENT);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/rejects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(
            jsonPath("$.response.message").value("Bạn không có quyền để thực hiện chức năng này!"));
  }

  @Test
  public void testGetAllRejectCourses_InstructorNotActive() throws Exception {
    User mockUser = new User();
    mockUser.setId(1);
    mockUser.setEmail(mockEmail);
    mockUser.setRole(UserRole.INSTRUCTOR);
    mockUser.setStatus(0);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findInstructorByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v1/courses/rejects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Forbidden"))
        .andExpect(
            jsonPath("$.response.message")
                .value(
                    "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email của bạn để kích hoạt tài khoản."));
  }

  @Test
  public void testReSubmitCourse_Success() throws Exception {
    int courseId = 1;

    CourseRequest request = new CourseRequest();
    CourseDTO response = new CourseDTO();

    Mockito.when(
            courseService.reSubmitCourse(
                Mockito.any(CourseRequest.class), Mockito.eq(mockToken), Mockito.eq(courseId)))
        .thenReturn(response);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/v1/courses/" + courseId + "/re-submit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + mockToken)
                .content(objectMapper.writeValueAsString(request))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value(202))
        .andExpect(jsonPath("$.message").value("Accepted"))
        .andExpect(
            jsonPath("$.response.message")
                .value("Cập nhật thành công. Khóa học của bạn đang được quản trị viên xử lý."));
  }
}
