package com.group1.MockProject.controller;

import com.group1.MockProject.dto.ApiResponseDto;
import com.group1.MockProject.dto.MessageDTO;
import com.group1.MockProject.dto.PaymentDTO;
import com.group1.MockProject.dto.request.PaymentRequest;
import com.group1.MockProject.dto.response.*;
import com.group1.MockProject.entity.Category;
import com.group1.MockProject.entity.Course;
import com.group1.MockProject.entity.Payment;
import com.group1.MockProject.exception.UnprocessableEntityException;
import com.group1.MockProject.repository.CategoryRepository;
import com.group1.MockProject.repository.CourseRepository;
import com.group1.MockProject.repository.PaymentDetailRepository;
import com.group1.MockProject.repository.PaymentRepository;
import com.group1.MockProject.service.PaymentService;
import com.group1.MockProject.service.SavedCourseService;
import com.group1.MockProject.service.StudentService;
import com.group1.MockProject.service.VNPayService;
import com.group1.MockProject.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {
  private final StudentService studentService;
  private final JwtUtil jwtUtil;
  private final CourseRepository courseRepository;
  private final CategoryRepository categoryRepository;
  private final ModelMapper modelMapper;
  private final SavedCourseService savedCourseService;
  private final PaymentService paymentService;
  private final VNPayService VNPayService;
  private final PaymentRepository paymentRepository;
  private final PaymentDetailRepository paymentDetailRepository;

  @GetMapping("/view-list-subscription")
  public ResponseEntity<ApiResponseDto<List<InstructorResponse>>> viewListSubscription(
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    List<InstructorResponse> instructors = studentService.viewListSubscription(email);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<List<InstructorResponse>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(instructors)
                .build());
  }

  @GetMapping("/search-instructor")
  public ResponseEntity<ApiResponseDto<List<InstructorResponse>>> searchInstructor(
      @RequestParam String name) {
    List<InstructorResponse> instructors = studentService.searchInstructor(name);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<List<InstructorResponse>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(instructors)
                .build());
  }

  @GetMapping("/subscribe/{instructorId}")
  public ResponseEntity<ApiResponseDto<?>> subscribeToInstructor(
      @PathVariable Integer instructorId,
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    String response = studentService.subscribeToInstructor(email, instructorId);

    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO(response))
                .build());
  }

  @PostMapping("/unsubscribe/{instructorId}")
  public ResponseEntity<ApiResponseDto<?>> unsubscribeFromInstructor(
      @PathVariable Integer instructorId,
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    String response = studentService.unsubscribeFromInstructor(email, instructorId);

    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<MessageDTO>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(new MessageDTO(response))
                .build());
  }

  @PreAuthorize("hasRole('STUDENT')")
  @PostMapping("/add-payment")
  public ResponseEntity<ApiResponseDto<AddPaymentResponse>> AddPaymentDetail(
          @RequestHeader("Authorization") String authorizationHeader,
          @Valid @RequestBody PaymentRequest request) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    AddPaymentResponse response = paymentService.addPaymentDetail(email, request);
    return ResponseEntity.ok()
            .body(
                    ApiResponseDto.<AddPaymentResponse>builder()
                            .status(200)
                            .message(HttpStatus.OK.getReasonPhrase())
                            .response(response)
                            .build());
  }
  @PreAuthorize("hasRole('STUDENT')")
  @DeleteMapping("/delete-payment")
  public ResponseEntity<?> deletePaymentDetail(
          @RequestHeader("Authorization") String authorizationHeader,
          @Valid @RequestBody PaymentRequest request) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    paymentService.deletePaymentDetail(email, request);
    // Trả về thông báo xóa thành công
    return ResponseEntity.status(HttpStatus.OK)
            .body(
                    ApiResponseDto.<PaymentResponse>builder()
                            .status(204)
                            .message(HttpStatus.OK.getReasonPhrase())
                            .build());
  }

  @PreAuthorize("hasRole('STUDENT')")
  @GetMapping("/all-payment")
  public ResponseEntity<ApiResponseDto<?>> getAllPayment(@RequestHeader("Authorization")String authorizationHeader){
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    PaymentResponseDTO response = paymentService.getAllPayment(email);
    return ResponseEntity.ok().body(
            ApiResponseDto.builder()
                    .status(200)
                    .message(HttpStatus.OK.getReasonPhrase())
                    .response(response)
                    .build()
    );
  }

  @PreAuthorize("hasRole('STUDENT')")
  @PostMapping("/payment/vn-pay")
  public ResponseEntity<ApiResponseDto<?>> payment(
      @RequestHeader("Authorization") String authorizationHeader,
      @Valid @RequestBody PaymentRequest request,
      HttpServletRequest httpRequest) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    Payment payment = paymentService.checkPayment(email, request);
    if (payment.getTotal_price() == 0.0) {

      PaymentResponse response = paymentService.freePayment(payment);

      return ResponseEntity.ok()
          .body(
              ApiResponseDto.<PaymentResponse>builder()
                  .status(200)
                  .message(HttpStatus.OK.getReasonPhrase())
                  .response(response)
                  .build());
    }

    if (payment.getTotal_price() <= 5000.0 && payment.getTotal_price() != 0.0) {
      paymentDetailRepository.deleteAll(payment.getPaymentDetails());
      paymentRepository.delete(payment);
      throw new UnprocessableEntityException("Tổng hoá đơn phải có giá trị tối thiểu 5000 VNĐ");
    }

    PaymentDTO.VNPayResponse response = VNPayService.createVnPayPayment(payment, httpRequest);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<PaymentDTO.VNPayResponse>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @GetMapping("/payment/vn-pay-callback")
  public ResponseEntity<ApiResponseDto<PaymentResponse>> payCallbackHandler(
      HttpServletRequest request) {
    String status = request.getParameter("vnp_ResponseCode");
    if (status.equals("00")) {
      PaymentResponse response = paymentService.callbackPayment(request.getParameter("vnp_TxnRef"));
      return ResponseEntity.ok()
          .body(
              ApiResponseDto.<PaymentResponse>builder()
                  .status(200)
                  .message(HttpStatus.OK.getReasonPhrase())
                  .response(response)
                  .build());
    } else {
      PaymentResponse response =
          paymentService.callbackPaymentFail(request.getParameter("vnp_TxnRef"));
      return ResponseEntity.ok()
          .body(
              ApiResponseDto.<PaymentResponse>builder()
                  .status(200)
                  .message(HttpStatus.OK.getReasonPhrase())
                  .response(response)
                  .build());
    }
  }

  @GetMapping("/savedCourses")
  public ResponseEntity<ApiResponseDto<GetSavedCourseResponse>> viewSavedCourse(
      @RequestHeader("Authorization") String authorizationHeader) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = JwtUtil.extractEmail(token);
    GetSavedCourseResponse response = savedCourseService.getSavedCoursesByEmail(email);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<GetSavedCourseResponse>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(response)
                .build());
  }

  @GetMapping("/category/{id}")
  public ResponseEntity<ApiResponseDto<List<CourseDTO>>> searchCourseByCategory(
      @PathVariable int id) {
    Category category =
        categoryRepository
            .findCategoryById(id)
            .orElseThrow(() -> new EmptyResultDataAccessException("Không tìm thấy danh mục", 1));
    List<Course> courses = courseRepository.findCourseByCategory(category);
    List<CourseDTO> courseDTOList =
        courses.stream()
            .map(course -> modelMapper.map(course, CourseDTO.class))
            .collect(Collectors.toList());
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponseDto.<List<CourseDTO>>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(courseDTOList)
                .build());
  }
}
