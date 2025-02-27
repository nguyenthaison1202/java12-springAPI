//package com.group1.MockProject.controller;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.group1.MockProject.dto.MessageDTO;
//import com.group1.MockProject.dto.request.CategoryRequest;
//import com.group1.MockProject.dto.request.SignInRequest;
//import com.group1.MockProject.dto.response.*;
//import com.group1.MockProject.entity.User;
//import com.group1.MockProject.entity.UserRole;
//import com.group1.MockProject.exception.GlobalExceptionHandler;
//import com.group1.MockProject.repository.InstructorRepository;
//import com.group1.MockProject.repository.UserRepository;
//import com.group1.MockProject.service.*;
//import com.group1.MockProject.utils.JwtUtil;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
////import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//@ExtendWith(MockitoExtension.class)
//class AdminControllerTest {
//  private MockMvc mockMvc;
//
//  @Mock private StudentService studentService;
//
//  @Mock private InstructorService instructorService;
//
//  @Mock private UserService userService;
//
//  @Mock private CourseService courseService;
//
//  @Mock private JwtUtil jwtUtil;
//
//  @Mock private UserRepository userRepository;
//
//  @Mock private AdminService adminService;
//
//  @Mock private CategoryService categoryService;
//
//  @Mock private InstructorRepository instructorRepository;
//
//  @InjectMocks private AdminController adminController;
//
//  private final ObjectMapper objectMapper = new ObjectMapper();
//  private MockedStatic<JwtUtil> jwtUtilMock;
//  private String mockToken = "mockToken";
//  private final String mockEmail = "email@email.com";
//
//  @BeforeEach
//  void setUp() {
//    adminController =
//        new AdminController(
//            studentService,
//            instructorService,
//            userService,
//            courseService,
//            jwtUtil,
//            userRepository,
//            adminService,
//            categoryService,
//            instructorRepository);
//
//    jwtUtilMock = Mockito.mockStatic(JwtUtil.class);
//    jwtUtilMock.when(() -> JwtUtil.extractEmail(Mockito.eq(mockToken))).thenReturn(mockEmail);
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(true);
//
//    this.mockMvc =
//        MockMvcBuilders.standaloneSetup(adminController)
//            .alwaysDo(print())
//            .setControllerAdvice(new GlobalExceptionHandler())
//            .build();
//  }
//
//  @AfterEach
//  void tearDown() {
//    if (jwtUtilMock != null) {
//      jwtUtilMock.close();
//    }
//  }
//
//  @Test
//  public void testGetAllStudents_Success() throws Exception {
//    int pageNo = 1;
//    int pageSize = 10;
//    String sortBy = "email";
//    String sortDir = "asc";
//
//    PageStudentsDTO response = new PageStudentsDTO();
//
//    Mockito.when(
//            studentService.getAllStudents(
//                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
//        .thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/users/students")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .param("pageNo", String.valueOf(pageNo))
//                .param("pageSize", String.valueOf(pageSize))
//                .param("sortBy", sortBy)
//                .param("sortDir", sortDir)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testGetAllInstructors_Success() throws Exception {
//    int pageNo = 1;
//    int pageSize = 10;
//    String sortBy = "email";
//    String sortDir = "asc";
//
//    PageInstructorsDTO response = new PageInstructorsDTO();
//
//    Mockito.when(
//            instructorService.getAllInstructors(
//                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
//        .thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/users/instructors")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .param("pageNo", String.valueOf(pageNo))
//                .param("pageSize", String.valueOf(pageSize))
//                .param("sortBy", sortBy)
//                .param("sortDir", sortDir)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testBlockUser_Success() throws Exception {
//    int userId = 1;
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/users/block/" + userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(
//            jsonPath("$.response.message").value("Người dùng với id " + userId + " đã bị khóa"));
//  }
//
//  @Test
//  public void testBlockUser_WrongIdFormat() throws Exception {
//    String userId = "test1";
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/users/block/" + userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isBadRequest())
//        .andExpect(jsonPath("$.status").value(400))
//        .andExpect(jsonPath("$.message").value("Bad Request"))
//        .andExpect(jsonPath("$.response.message").value("Id người dùng không hợp lệ: " + userId));
//  }
//
//  @Test
//  public void testUnBlockUser_Success() throws Exception {
//    int userId = 1;
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/users/unblock/" + userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(
//            jsonPath("$.response.message").value("Người dùng với id " + userId + " đã mở khóa"));
//  }
//
//  @Test
//  public void testUnBlockUser_WrongIdFormat() throws Exception {
//    String userId = "test1";
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/users/unblock/" + userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isBadRequest())
//        .andExpect(jsonPath("$.status").value(400))
//        .andExpect(jsonPath("$.message").value("Bad Request"))
//        .andExpect(jsonPath("$.response.message").value("Id người dùng không hợp lệ: " + userId));
//  }
//
//  @Test
//  public void testGetAdminData_Success() throws Exception {
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/data")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response.message").value("Admin Data"));
//  }
//
//  @Test
//  public void testApproveCourse_Success() throws Exception {
//    int courseId = 1;
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(new User()));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/courses/" + courseId + "/approve")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testApproveCourse_InvalidToken() throws Exception {
//    int courseId = 1;
//
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/courses/" + courseId + "/approve")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ hoặc đã hết hạn"));
//  }
//
//  @Test
//  public void testRejectCourse_Success() throws Exception {
//    int courseId = 1;
//    String rejectReason = "reason";
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(new User()));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/courses/" + courseId + "/reject")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .param("reason", rejectReason)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(
//            jsonPath("$.response.message")
//                .value("Khóa học đã bị từ chối" + ". Lý do: " + rejectReason));
//  }
//
//  @Test
//  public void testRejectCourse_InvalidToken() throws Exception {
//    int courseId = 1;
//
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/courses/" + courseId + "/reject")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ"));
//  }
//
//  @Test
//  public void testGetDashboardData_Success() throws Exception {
//
//    AdminDashboardResponse response = AdminDashboardResponse.builder().build();
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(new User()));
//    Mockito.when(adminService.getDashboardData()).thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/dashboard")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testGetDashboardData_InvalidToken() throws Exception {
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/dashboard")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ"));
//  }
//
//  @Test
//  public void testCreateCategory_Success() throws Exception {
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    CategoryDTO response = new CategoryDTO();
//
//    Mockito.when(
//            categoryService.createCategory(
//                Mockito.any(CategoryRequest.class), Mockito.eq(mockToken)))
//        .thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/api/v1/admin/category/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.status").value(201))
//        .andExpect(jsonPath("$.message").value("Created"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testCreateCategory_InvalidToken() throws Exception {
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    mockToken = null;
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/api/v1/admin/category/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "a " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(
//            jsonPath("$.response.message").value("Token xác thực không tìm thấy hoặc không đúng"));
//  }
//
//  @Test
//  public void testUpdateCategory_Success() throws Exception {
//    int categoryId = 1;
//
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    CategoryDTO response = new CategoryDTO();
//
//    User mockUser = new User();
//    mockUser.setId(1);
//    mockUser.setEmail(mockEmail);
//    mockUser.setRole(UserRole.ADMIN);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//    Mockito.when(
//            categoryService.updateCategory(
//                Mockito.anyInt(), Mockito.any(CategoryRequest.class), Mockito.eq(mockToken)))
//        .thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testUpdateCategory_InvalidToken() throws Exception {
//    int categoryId = 1;
//
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Be " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(
//            jsonPath("$.response.message").value("Token xác thực không tìm thấy hoặc không đúng"));
//  }
//
//  @Test
//  public void testUpdateCategory_ValidatingTokenFailed() throws Exception {
//    int categoryId = 1;
//
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ hoặc đã hết hạn"));
//  }
//
//  @Test
//  public void testUpdateCategory_UserDoesNotHaveRole() throws Exception {
//    int categoryId = 1;
//
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    User mockUser = new User();
//    mockUser.setId(1);
//    mockUser.setEmail(mockEmail);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Không tìm thấy vai trò trong token"));
//  }
//
//  @Test
//  public void testUpdateCategory_UserDoesNotHaveId() throws Exception {
//    int categoryId = 1;
//
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    User mockUser = new User();
//    mockUser.setEmail(mockEmail);
//    mockUser.setRole(UserRole.ADMIN);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(
//            jsonPath("$.response.message").value("Không tìm thấy người hướng dẫn trong token"));
//  }
//
//  @Test
//  public void testUpdateCategory_UserDoesNotHaveAccess() throws Exception {
//    int categoryId = 1;
//
//    CategoryRequest request = new CategoryRequest();
//    request.setName("name");
//    request.setDescription("description");
//
//    User mockUser = new User();
//    mockUser.setId(1);
//    mockUser.setRole(UserRole.INSTRUCTOR);
//    mockUser.setEmail(mockEmail);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.put("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isForbidden())
//        .andExpect(jsonPath("$.status").value(403))
//        .andExpect(jsonPath("$.message").value("Forbidden"))
//        .andExpect(
//            jsonPath("$.response.message")
//                .value("Bạn không có quyền để cập nhật phân loại khoá học này"));
//  }
//
//  @Test
//  public void testDeleteCategory_Success() throws Exception {
//    int categoryId = 1;
//
//    User mockUser = new User();
//    mockUser.setId(1);
//    mockUser.setRole(UserRole.ADMIN);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isNoContent())
//        .andExpect(jsonPath("$.status").value(204))
//        .andExpect(jsonPath("$.message").value("No Content"));
//  }
//
//  @Test
//  public void testDeleteCategory_TokenNotFound() throws Exception {
//    int categoryId = 1;
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(
//            jsonPath("$.response.message").value("Token xác thực không tìm thấy hoặc không đúng"));
//  }
//
//  @Test
//  public void testDeleteCategory_InvalidToken() throws Exception {
//    int categoryId = 1;
//
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ hoặc đã hết hạn"));
//  }
//
//  @Test
//  public void testDeleteCategory_RoleOfUserNotFound() throws Exception {
//    int categoryId = 1;
//
//    User mockUser = new User();
//    mockUser.setId(1);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Không tìm thấy vai trò trong token"));
//  }
//
//  @Test
//  public void testDeleteCategory_IdOfUserNotFound() throws Exception {
//    int categoryId = 1;
//
//    User mockUser = new User();
//    mockUser.setRole(UserRole.ADMIN);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Không tìm thấy admin trong token"));
//  }
//
//  @Test
//  public void testDeleteCategory_DoNotHaveAccess() throws Exception {
//    int categoryId = 1;
//
//    User mockUser = new User();
//    mockUser.setId(1);
//    mockUser.setRole(UserRole.INSTRUCTOR);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/category/" + categoryId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isForbidden())
//        .andExpect(jsonPath("$.status").value(403))
//        .andExpect(jsonPath("$.message").value("Forbidden"))
//        .andExpect(
//            jsonPath("$.response.message")
//                .value("Bạn không có quyền để xoá phân loại khoá học này"));
//  }
//
//  @Test
//  public void testGetAllCourses_Success() throws Exception {
//    CourseDTO courseDTO = new CourseDTO();
//
//    Mockito.when(courseService.getAllCourses()).thenReturn(List.of(courseDTO));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/courses")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testGetAllUsers_Success() throws Exception {
//    User mockUser = new User();
//
//    Mockito.when(adminService.getAllUsers()).thenReturn(List.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/all-users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testAdminSignIn_Success() throws Exception {
//    SignInRequest request = new SignInRequest();
//    request.setEmail(mockEmail);
//    request.setPassword("<PASSWORD>");
//
//    Mockito.when(adminService.authenticate(Mockito.any(SignInRequest.class)))
//        .thenReturn(new SignInResponse());
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/api/v1/admin/sign-in")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .content(objectMapper.writeValueAsString(request))
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testRejectInstructor_Success() throws Exception {
//    int instructorId = 1;
//
//    MessageDTO response = new MessageDTO();
//
//    Mockito.when(adminService.setRejectInstructor(Mockito.eq(instructorId))).thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/api/v1/admin/users/reject/" + instructorId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testApproveInstructor_Success() throws Exception {
//    int instructorId = 1;
//
//    MessageDTO response = new MessageDTO();
//
//    Mockito.when(adminService.setApproveInstructor(Mockito.eq(instructorId))).thenReturn(response);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.post("/api/v1/admin/users/approve/" + instructorId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testDeleteInstructor_Success() throws Exception {
//    int instructorId = 1;
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.delete("/api/v1/admin/users/delete/" + instructorId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isNoContent())
//        .andExpect(jsonPath("$.status").value(204))
//        .andExpect(jsonPath("$.message").value("No Content"));
//  }
//
//  @Test
//  public void testGetCourseById_Success() throws Exception {
//    int courseId = 1;
//
//    User mockUser = new User();
//    mockUser.setId(1);
//    mockUser.setRole(UserRole.ADMIN);
//
//    CourseDTO courseDTO = new CourseDTO();
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//    Mockito.when(courseService.getCourseById(Mockito.eq(courseId))).thenReturn(courseDTO);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/course/" + courseId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testGetCourseById_TokenNotFound() throws Exception {
//    int courseId = 1;
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/course/" + courseId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(
//            jsonPath("$.response.message").value("Token xác thực không tìm thấy hoặc không đúng"));
//  }
//
//  @Test
//  public void testGetCourseById_InvalidToken() throws Exception {
//    int courseId = 1;
//
//    jwtUtilMock.when(() -> JwtUtil.validateToken(Mockito.eq(mockToken))).thenReturn(false);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/course/" + courseId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Token không hợp lệ hoặc đã hết hạn"));
//  }
//
//  @Test
//  public void testGetCourseById_RoleOfUserNotFound() throws Exception {
//    int courseId = 1;
//
//    User mockUser = new User();
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/course/" + courseId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(jsonPath("$.response.message").value("Không tìm thấy vai trò trong token"));
//  }
//
//  @Test
//  public void testGetCourseById_IdOfUserNotFound() throws Exception {
//    int courseId = 1;
//
//    User mockUser = new User();
//    mockUser.setRole(UserRole.ADMIN);
//
//    Mockito.when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/admin/course/" + courseId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isUnauthorized())
//        .andExpect(jsonPath("$.status").value(401))
//        .andExpect(jsonPath("$.message").value("Unauthorized"))
//        .andExpect(
//            jsonPath("$.response.message").value("Không tìm thấy người hướng dẫn trong token"));
//  }
//}
