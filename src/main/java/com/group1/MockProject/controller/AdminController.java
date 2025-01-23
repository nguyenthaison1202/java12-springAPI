package com.group1.MockProject.controller;

import com.group1.MockProject.dto.ApiResponseDto;
import com.group1.MockProject.dto.MessageDTO;
import com.group1.MockProject.dto.request.CategoryRequest;
import com.group1.MockProject.dto.request.SignInRequest;
import com.group1.MockProject.dto.response.*;
import com.group1.MockProject.dto.response.AdminDashboardResponse;
import com.group1.MockProject.dto.response.CategoryDTO;
import com.group1.MockProject.dto.response.CourseDTO;
import com.group1.MockProject.dto.response.CourseListResponse;
import com.group1.MockProject.dto.response.SignInResponse;
import com.group1.MockProject.entity.CourseStatus;
import com.group1.MockProject.entity.User;
import com.group1.MockProject.repository.InstructorRepository;
import com.group1.MockProject.repository.UserRepository;
import com.group1.MockProject.service.AdminService;
import com.group1.MockProject.service.CategoryService;
import com.group1.MockProject.service.CourseService;
import com.group1.MockProject.service.InstructorService;
import com.group1.MockProject.service.StudentService;
import com.group1.MockProject.service.UserService;
import com.group1.MockProject.utils.AppContants;
import com.group1.MockProject.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  private StudentService studentService;
  private InstructorService instructorService;
  private UserService userService;
  private final CourseService courseService;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final AdminService adminService;
  private final CategoryService categoryService;
  private final InstructorRepository instructorRepository;

  public AdminController(
      StudentService studentService,
      InstructorService instructorService,
      UserService userService,
      CourseService courseService,
      JwtUtil jwtUtil,
      UserRepository userRepository,
      AdminService adminService,
      CategoryService categoryService,
      InstructorRepository instructorRepository) {
    this.studentService = studentService;
    this.instructorService = instructorService;
    this.userService = userService;
    this.courseService = courseService;
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
    this.adminService = adminService;
    this.categoryService = categoryService;
    this.instructorRepository = instructorRepository;
  }

  // GET /api/v1/admin/user get student and instructor
  @PreAuthorize(("hasRole('ADMIN')"))
  @GetMapping("/users/students")
  public ResponseEntity<?> getAllStudents(
      @RequestHeader("Authorization") String token,
      @RequestParam(
              value = "pageNo",
              defaultValue = AppContants.DEFAULT_PAGE_NUMBER,
              required = false)
          int pageNo,
      @RequestParam(
              value = "pageSize",
              defaultValue = AppContants.DEFAULT_PAGE_SIZE,
              required = false)
          int pageSize,
      @RequestParam(value = "sortBy", defaultValue = AppContants.DEFAULT_SORT_BY, required = false)
          String sortBy,
      @RequestParam(
              value = "sortDir",
              defaultValue = AppContants.DEFAULT_SORT_DIR,
              required = false)
          String sortDir) {
    PageStudentsDTO response = studentService.getAllStudents(pageNo, pageSize, sortBy, sortDir);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<PageStudentsDTO>builder()
                .status(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @PreAuthorize(("hasRole('ADMIN')"))
  @GetMapping("/users/instructors")
  public ResponseEntity<?> getAllInstructors(
      @RequestHeader("Authorization") String token,
      @RequestParam(
              value = "pageNo",
              defaultValue = AppContants.DEFAULT_PAGE_NUMBER,
              required = false)
          int pageNo,
      @RequestParam(
              value = "pageSize",
              defaultValue = AppContants.DEFAULT_PAGE_SIZE,
              required = false)
          int pageSize,
      @RequestParam(value = "sortBy", defaultValue = AppContants.DEFAULT_SORT_BY, required = false)
          String sortBy,
      @RequestParam(
              value = "sortDir",
              defaultValue = AppContants.DEFAULT_SORT_DIR,
              required = false)
          String sortDir) {
    PageInstructorsDTO response =
        instructorService.getAllInstructors(pageNo, pageSize, sortBy, sortDir);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<PageInstructorsDTO>builder()
                .status(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  // PUT /api/v1/user/block/{userid}
  @PreAuthorize(("hasRole('ADMIN')"))
  @PutMapping("/users/block/{id}")
  public ResponseEntity<?> blockUser(@PathVariable("id") String id) {
    try {
      int userId = Integer.parseInt(id);

      userService.blockUserById(userId);

      return ResponseEntity.ok()
          .body(
              ApiResponseDto.<MessageDTO>builder()
                  .status(200)
                  .message(HttpStatus.OK.getReasonPhrase())
                  .response(new MessageDTO("Người dùng với id " + userId + " đã bị khóa"))
                  .build());
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponseDto.<MessageDTO>builder()
                  .status(400)
                  .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
                  .response(new MessageDTO("Id người dùng không hợp lệ: " + id))
                  .build());
    }
  }

  // PUT /api/v1/user/block/{userid}
  @PreAuthorize(("hasRole('ADMIN')"))
  @PutMapping("/users/unblock/{id}")
  public ResponseEntity<?> unblockUser(@PathVariable("id") String id) {
    try {
      int userId = Integer.parseInt(id);

      userService.unblockUserById(userId);

      return ResponseEntity.ok()
          .body(
              ApiResponseDto.<MessageDTO>builder()
                  .status(200)
                  .message(HttpStatus.OK.getReasonPhrase())
                  .response(new MessageDTO("Người dùng với id " + userId + " đã mở khóa"))
                  .build());
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest()
          .body(
              ApiResponseDto.<MessageDTO>builder()
                  .status(400)
                  .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
                  .response(new MessageDTO("Id người dùng không hợp lệ: " + id))
                  .build());
    }
  }

  // API chỉ dành cho Admin
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/data")
  public ResponseEntity<?> getAdminData(@RequestHeader("Authorization") String token) {
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO("Admin Data"))
                .build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/courses/{courseId}/approve")
  public ResponseEntity<ApiResponseDto<MessageDTO>> approveCourse(
      @PathVariable int courseId, @RequestHeader("Authorization") String authorizationHeader) {
    // Xác thực token
    String token = authorizationHeader.replace("Bearer ", "");

    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn");
    }

    // Kiểm tra quyền admin
    String email = JwtUtil.extractEmail(token);

    userRepository
        .findByEmail(email)
        .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    courseService.updateCourseStatus(courseId, CourseStatus.APPROVED);

    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO("Khóa học đã được phê duyệt thành công"))
                .build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/courses/{courseId}/reject")
  public ResponseEntity<ApiResponseDto<MessageDTO>> rejectCourse(
      @PathVariable int courseId,
      @RequestParam(required = false) String reason,
      @RequestHeader("Authorization") String authorizationHeader) {
    // Xác thực token
    String token = authorizationHeader.replace("Bearer ", "");
    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ");
    }

    // Kiểm tra quyền admin
    String email = JwtUtil.extractEmail(token);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    courseService.updateCourseStatus(courseId, CourseStatus.REJECTED, reason);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(
                    new MessageDTO(
                        "Khóa học đã bị từ chối" + (reason != null ? ". Lý do: " + reason : "")))
                .build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponseDto<AdminDashboardResponse>> getDashboardData(
      @RequestHeader("Authorization") String authorizationHeader) {
    // Xác thực token
    String token = authorizationHeader.replace("Bearer ", "");
    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ");
    }

    // Kiểm tra người dùng
    String email = JwtUtil.extractEmail(token);

    userRepository
        .findByEmail(email)
        .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    AdminDashboardResponse dashboardData = adminService.getDashboardData();

    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<AdminDashboardResponse>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(dashboardData)
                .build());
  }

  @PostMapping("/category/create")
  public ResponseEntity<?> createCategory(
      @Valid @RequestBody CategoryRequest categoryRequest, HttpServletRequest request) {

    // Lấy token từ header Authorization
    String token = request.getHeader("Authorization");

    // Kiểm tra xem token có hợp lệ không
    if (token == null || !token.startsWith("Bearer ")) {
      throw new BadCredentialsException("Token xác thực không tìm thấy hoặc không đúng");
    }

    token = token.substring(7); // Loại bỏ "Bearer " để lấy chỉ phần token

    // Tiến hành tạo Category
    CategoryDTO response = categoryService.createCategory(categoryRequest, token);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponseDto.<CategoryDTO>builder()
                .status(201)
                .message(HttpStatus.CREATED.getReasonPhrase())
                .response(response)
                .build()); // Trả về CategoryDTO trong body của response
  }

  @PutMapping("/category/{categoryId}")
  public ResponseEntity<?> updateCategory(
      @PathVariable int categoryId,
      @RequestBody CategoryRequest categoryRequest,
      HttpServletRequest request) {
    // Lấy token từ header Authorization
    String token = request.getHeader("Authorization");

    // Kiểm tra xem token có hợp lệ không
    if (token == null || !token.startsWith("Bearer ")) {
      throw new BadCredentialsException("Token xác thực không tìm thấy hoặc không đúng");
    }

    token = token.substring(7); // Loại bỏ "Bearer " để lấy chỉ phần token

    // Xác thực token
    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn");
    }

    String email = JwtUtil.extractEmail(token);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy nguời dùng", 1));

    // Kiểm tra role
    if (user.getRole() == null) {
      throw new BadCredentialsException("Không tìm thấy vai trò trong token");
    }

    // Kiểm tra instructor
    if (user.getId() == 0) {
      throw new BadCredentialsException("Không tìm thấy người hướng dẫn trong token");
    }

    if (!user.getRole().toString().equals("ADMIN")) {
      throw new AccessDeniedException("Bạn không có quyền để cập nhật phân loại khoá học này");
    }

    CategoryDTO response = categoryService.updateCategory(categoryId, categoryRequest, token);

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<CategoryDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @DeleteMapping("/category/{categoryId}")
  public ResponseEntity<?> deleteCategory(
      @PathVariable int categoryId, HttpServletRequest request) {
    // Lấy token từ header Authorization
    String token = request.getHeader("Authorization");

    // Kiểm tra token
    if (token == null || !token.startsWith("Bearer ")) {
      throw new BadCredentialsException("Token xác thực không tìm thấy hoặc không đúng");
    }

    // Loại bỏ "Bearer " để lấy chỉ phần token
    token = token.substring(7);

    // Xác thực token
    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn");
    }

    String email = JwtUtil.extractEmail(token);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    // Kiểm tra role
    if (user.getRole() == null) {
      throw new BadCredentialsException("Không tìm thấy vai trò trong token");
    }

    // Kiểm tra role
    if (user.getId() == 0) {
      throw new BadCredentialsException("Không tìm thấy admin trong token");
    }

    if (!user.getRole().toString().equals("ADMIN")) {
      throw new AccessDeniedException("Bạn không có quyền để xoá phân loại khoá học này");
    }

    categoryService.deleteCategory(categoryId);

    // Trả về thông báo xóa thành công
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(
            ApiResponseDto.<CategoryDTO>builder()
                .status(204)
                .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/courses")
  public ResponseEntity<ApiResponseDto<CourseListResponse>> getAllCourses() {
    List<CourseDTO> courses = courseService.getAllCourses();
    CourseListResponse response = new CourseListResponse(courses);

    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<CourseListResponse>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @PostMapping("/sign-in")
  public ResponseEntity<ApiResponseDto<SignInResponse>> signIn(
      @Valid @RequestBody SignInRequest signInRequest) {
    SignInResponse signInResponse = adminService.authenticate(signInRequest);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<SignInResponse>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(signInResponse)
                .build());
  }

  @GetMapping("/all-users")
  public ResponseEntity<ApiResponseDto<List<User>>> getAllUsers(
      @RequestHeader("Authorization") String token) {
    List<User> userList = adminService.getAllUsers();
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<List<User>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(userList)
                .build());
  }

  @PostMapping("/users/reject/{id}")
  public ResponseEntity<ApiResponseDto<MessageDTO>> rejectInstructor(
      @RequestHeader("Authorization") String token, @PathVariable("id") int id) {
    MessageDTO response = adminService.setRejectInstructor(id);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @PostMapping("/users/approve/{id}")
  public ResponseEntity<ApiResponseDto<MessageDTO>> approveInstructor(
      @RequestHeader("Authorization") String token, @PathVariable("id") int id) {
    MessageDTO response = adminService.setApproveInstructor(id);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @DeleteMapping("/users/delete/{id}")
  public ResponseEntity<?> deleteInstructor(
      @RequestHeader("Authorization") String token, @PathVariable("id") int id) {
    adminService.deleteUser(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(204)
                .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/course/{courseId}")
  public ResponseEntity<?> getCourseById(
      HttpServletRequest request, @PathVariable("courseId") int id) {
    // Lấy token từ header Authorization
    String token = request.getHeader("Authorization");

    // Kiểm tra token
    if (token == null || !token.startsWith("Bearer ")) {
      throw new BadCredentialsException("Token xác thực không tìm thấy hoặc không đúng");
    }

    // Loại bỏ "Bearer " để lấy chỉ phần token
    token = token.substring(7);

    // Xác thực token
    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn");
    }

    String email = JwtUtil.extractEmail(token);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    // Kiểm tra role
    if (user.getRole() == null) {
      throw new BadCredentialsException("Không tìm thấy vai trò trong token");
    }

    // Kiểm tra role
    if (user.getId() == 0) {
      throw new BadCredentialsException("Không tìm thấy người hướng dẫn trong token");
    }

    CourseDTO course = courseService.getCourseById(id);

    // Trả về danh sách khóa học dưới dạng API response
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<CourseDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(course)
                .build());
  }
}
