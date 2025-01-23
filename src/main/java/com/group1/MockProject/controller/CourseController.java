package com.group1.MockProject.controller;

import com.group1.MockProject.dto.ApiResponseDto;
import com.group1.MockProject.dto.MessageDTO;
import com.group1.MockProject.dto.request.CourseRequest;
import com.group1.MockProject.dto.request.ReviewRequest;
import com.group1.MockProject.dto.response.CourseDTO;
import com.group1.MockProject.dto.response.RejectCourseResponse;
import com.group1.MockProject.dto.response.ReviewResponse;
import com.group1.MockProject.entity.*;
import com.group1.MockProject.exception.PaymentRequiredException;
import com.group1.MockProject.repository.*;
import com.group1.MockProject.service.CourseService;
import com.group1.MockProject.service.EnrollCourseService;
import com.group1.MockProject.service.ReviewCourseService;
import com.group1.MockProject.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

  private final CourseService courseService;
  private final EnrollCourseService enrollCourseService;
  private final ReviewCourseService reviewCourseService;
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final SavedCourseRepository savedCourseRepository;
  private final InstructorRepository instructorRepository;
  private final PaymentRepository paymentRepository;
  private final PaymentDetailRepository paymentDetailRepository;

  @PostMapping("/create")
  public ResponseEntity<?> createCourse(
      @RequestBody com.group1.MockProject.dto.request.CourseRequest courseRequest,
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

    // Lấy thông tin người dùng từ JWT
    //            String role = JwtUtil.extractRoleFromTokenCourse(token);
    //            int userId = JwtUtil.extractUserIdFromToken(token);

    String email = JwtUtil.extractEmail(token);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy nguời dùng", 1));

    // Kiểm tra nếu role là null hoặc không phải INSTRUCTOR
    //            if (user.getRole() == null) {
    //                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                        .body("Role not found in the token");
    //            }

    if (!user.getRole().toString().equals("INSTRUCTOR")) {
      throw new AccessDeniedException("Bạn không có quyền để tạo khoá học");
    }

    if (user.getStatus() == 0) {
      throw new AccessDeniedException("Bạn không có quyền để tạo khoá học");
    }

    // Tiến hành tạo khóa học
    CourseDTO response = courseService.createCourse(courseRequest, token);

    // Trả về phản hồi thành công với mã trạng thái 201 Created và thông tin khóa học mới tạo
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponseDto.<CourseDTO>builder()
                .status(201)
                .message(HttpStatus.CREATED.getReasonPhrase())
                .response(response)
                .build()); // Trả về CourseDTO trong body của response
  }

  // Endpoint to update an existing course
  @PutMapping("/{courseId}")
  public ResponseEntity<?> updateCourse(
      @PathVariable int courseId,
      @RequestBody CourseRequest courseRequest,
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
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    if (!user.getRole().toString().equals("INSTRUCTOR")) {
      throw new AccessDeniedException("Bạn không có quyền để cập nhật khoá học này");
    }
    // check status Instructor
    if (user.getStatus() == 0) {
      throw new AccessDeniedException("Bạn không có quyền để cập nhật khoá học này");
    }
    // Tiến hành tạo khóa học
    CourseDTO response = courseService.updateCourse(courseId, courseRequest, token);

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<CourseDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @DeleteMapping("/{courseId}")
  public ResponseEntity<?> deleteCourse(@PathVariable int courseId, HttpServletRequest request) {
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

    if (!user.getRole().toString().equals("INSTRUCTOR")) {
      throw new AccessDeniedException("Bạn không có quyền để xoá khoá học này");
    }
    // check status Instructor
    if (user.getStatus() == 0) {
      throw new AccessDeniedException("Bạn không có quyền để xoá khoá học này");
    }
    courseService.deleteCourse(courseId, token);

    // Trả về thông báo xóa thành công
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(
            ApiResponseDto.<CourseDTO>builder()
                .status(204)
                .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build());
  }

  @GetMapping("/get-all-courses")
  public ResponseEntity<?> getCoursesByInstructor(HttpServletRequest request) {
    // Lấy token từ header Authorization
    String token = request.getHeader("Authorization");

    // Loại bỏ "Bearer " để lấy chỉ phần token
    token = token.substring(7);

    List<CourseDTO> courses = courseService.getCoursesByInstructor(token);

    // Trả về danh sách khóa học dưới dạng API response
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<List<CourseDTO>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(courses)
                .build());
  }

  @GetMapping("/{courseId}/enrolled")
  public ResponseEntity<?> enrollCourse(
      @PathVariable("courseId") int courseId,
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    User user =
        userRepository
            .findByEmail(JwtUtil.extractEmail(token))
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));
    Optional<Student> student = studentRepository.findByUser(user);
    if (!(student.isPresent() && student.get().getUser().getStatus() == 1)) {
      throw new AccessDeniedException(
          "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email của bạn để kích hoạt tài khoản.");
    }
    Optional<Course> course = courseRepository.findById(courseId);
    if (course.isEmpty()) {
      throw new EmptyResultDataAccessException(
          "Khóa học không hợp lệ. Vui lòng liên hệ quản trị viên để biết thêm thông tin.", 1);
    }
    //    Optional<SavedCourse> savedCourse =
    //        savedCourseRepository.findByCourseAndStudent(course.get(), student.get());
    //    if (savedCourse.isEmpty()) {
    //      throw new PaymentRequiredException(
    //          "Bạn không có quyền truy cập khóa học này. Vui lòng thanh toán khóa học trước khi
    // truy cập.");
    //    }
    Payment payment = paymentRepository.findByStudent(student.get());
    List<PaymentDetail> paymentDetails = paymentDetailRepository.findByPayment(payment);
    boolean check = false;
    for (PaymentDetail paymentDetail : paymentDetails) {
      if (paymentDetail.getCourse().equals(course.get())) {
        check = true;
      }
    }
    if (!check) {
      throw new PaymentRequiredException(
          "Bạn không có quyền truy cập khóa học này. Vui lòng thanh toán khóa học trước khi truy cập.");
    }
    String response = enrollCourseService.addEnroll(student.get(), course.get());

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO(response))
                .build());
  }

  @GetMapping("/{courseId}/finished")
  public ResponseEntity<?> finishedCourse(
      @PathVariable("courseId") int courseId,
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy người dùng", 1));

    Student student =
        studentRepository
            .findByUser(user)
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email của bạn để kích hoạt tài khoản."));

    Optional<Course> course = courseRepository.findById(courseId);
    if (course.isEmpty()) {
      throw new EmptyResultDataAccessException(
          "Khóa học không hợp lệ. Vui lòng liên hệ quản trị viên để biết thêm thông tin.", 1);
    }
    Optional<SavedCourse> savedCourse =
        savedCourseRepository.findByCourseAndStudent(course.get(), student);
    if (!(savedCourse.isPresent() && savedCourse.get().getStatus() == 0)) {
      throw new DataIntegrityViolationException("Bạn đã hoàn thành khóa học này rồi.");
    }
    savedCourse.get().setStatus(1);
    savedCourseRepository.save(savedCourse.get());
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO("Xin chúc mừng bạn đã hoàn thành khóa học."))
                .build());
  }

  @PostMapping("/{courseId}/review")
  public ResponseEntity<?> reviewCourse(
      @RequestBody ReviewRequest request,
      @PathVariable("courseId") int courseId,
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy nguời dùng", 1));

    Optional<Student> student = studentRepository.findByUser(user);
    if (student.isEmpty()) {
      throw new AccessDeniedException(
          "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email của bạn để kích hoạt tài khoản.");
    }
    Optional<Course> course = courseRepository.findById(courseId);
    if (course.isEmpty()) {
      throw new EmptyResultDataAccessException(
          "Khóa học không hợp lệ. Vui lòng liên hệ quản trị viên để biết thêm thông tin.", 1);
    }
    Optional<SavedCourse> savedCourse =
        savedCourseRepository.findByCourseAndStudent(course.get(), student.get());
    if (!(savedCourse.isPresent() && savedCourse.get().getStatus() == 1)) {
      throw new AccessDeniedException(
          "Bạn chưa hoàn thành khóa học, không thể đánh giá khóa học này.");
    }
    String response = reviewCourseService.addReview(request, student.get(), course.get());
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO(response))
                .build());
  }

  @GetMapping("/{instructorId}/reviews")
  public ResponseEntity<ApiResponseDto<List<ReviewResponse>>> getAllReviewsOfInstructor(
      @PathVariable("instructorId") int instructorId,
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    userRepository
        .findByEmail(email)
        .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy nguời dùng", 1));

    Optional<Instructor> instructor = instructorRepository.findById(instructorId);
    if (instructor.isEmpty()) {
      throw new EmptyResultDataAccessException(
          "Không tìm thấy instructor hoặc bạn không có quyền để xem đánh giá. Vui lòng liên hệ quản trị viên để biết thêm thông tin.",
          1);
    }

    List<ReviewResponse> response = reviewCourseService.getAllReviews(instructor.get());
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<List<ReviewResponse>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  // xem list các khóa học bị từ chối
  @GetMapping("/rejects")
  public ResponseEntity<ApiResponseDto<List<RejectCourseResponse>>> getAllRejects(
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");

    // Xác thực token
    if (!JwtUtil.validateToken(token)) {
      throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn");
    }

    User user =
        userRepository
            .findByEmail(JwtUtil.extractEmail(token))
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy nguời dùng", 1));

    if (!user.getRole().toString().equals("INSTRUCTOR")) {
      throw new AccessDeniedException("Bạn không có quyền để thực hiện chức năng này!");
    }

    Instructor instructor =
        instructorRepository
            .findInstructorByUser(user)
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email của bạn để kích hoạt tài khoản."));

    List<RejectCourseResponse> response =
        courseService.viewAllRejectedCoursesByInstructor(instructor.getId());

    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<List<RejectCourseResponse>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  // re-submit course
  @PutMapping("/{courseId}/re-submit")
  public ResponseEntity<?> reSubmitCourse(
      @PathVariable int courseId,
      @RequestBody CourseRequest courseRequest,
      HttpServletRequest request) {
    // Lấy token từ header Authorization
    String token = request.getHeader("Authorization");

    token = token.substring(7); // Loại bỏ "Bearer " để lấy chỉ phần token

    courseService.reSubmitCourse(courseRequest, token, courseId);

    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(202)
                .message(HttpStatus.ACCEPTED.getReasonPhrase())
                .response(
                    new MessageDTO(
                        "Cập nhật thành công. Khóa học của bạn đang được quản trị viên xử lý."))
                .build());
  }
}
