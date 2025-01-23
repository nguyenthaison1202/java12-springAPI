package com.group1.MockProject.service.implementation;

import static org.junit.jupiter.api.Assertions.*;

import com.group1.MockProject.dto.PaymentDTO;
import com.group1.MockProject.dto.request.AddPaymentRequest;
import com.group1.MockProject.dto.request.PaymentRequest;
import com.group1.MockProject.dto.response.AddPaymentResponse;
import com.group1.MockProject.dto.response.GetSavedCourseResponse;
import com.group1.MockProject.dto.response.PaymentResponse;
import com.group1.MockProject.entity.*;
import com.group1.MockProject.repository.*;
import com.group1.MockProject.utils.JwtUtil;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
  @Mock private PaymentRepository paymentRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private PaymentDetailRepository paymentDetailRepository;

  @Mock private SavedCourseRepository savedCourseRepository;

  @Mock private StudentRepository studentRepository;

  @Mock private UserRepository userRepository;

  @Mock private MyCourseRepository myCourseRepository;

  @Mock private SavedCourseServiceImpl savedCourseService;

  @InjectMocks private PaymentServiceImpl paymentService;

  private User mockUser;
  private Student mockStudent;
  private Course mockCourse;
  private Payment mockPayment;
  private final String mockEmail = "mock@email.com";
  private final int courseId = 1;
  private final int studentId = 1;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setEmail(mockEmail);

    mockStudent = new Student();
    mockStudent.setId(studentId);
    mockStudent.setUser(mockUser);
    mockUser.setStudent(mockStudent);

    mockCourse = new Course();
    mockCourse.setId(courseId);
    mockCourse.setPrice(50000.0);

    mockPayment = new Payment();
    mockPayment.setTotal_price(100000L);
  }

  @Test
  public void testAddPaymentDetail_Success() {
    String mockEmail = "email@email.com";
    int courseId = 1;

    AddPaymentRequest request = new AddPaymentRequest();
    request.setCourseId(courseId);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(Mockito.eq(request.getCourseId())))
        .thenReturn(Optional.of(mockCourse));
    Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenReturn(mockPayment);
    Mockito.when(
            myCourseRepository.findByCourseAndStudent(
                Mockito.any(Course.class), Mockito.any(Student.class)))
        .thenReturn(Optional.empty());
    Mockito.when(
            paymentDetailRepository.findByPaymentAndCourse(
                Mockito.any(Payment.class), Mockito.any(Course.class)))
        .thenReturn(Optional.empty());
    Mockito.when(paymentDetailRepository.save(Mockito.any(PaymentDetail.class)))
        .thenReturn(new PaymentDetail());

    AddPaymentResponse result = paymentService.addPaymentDetail(mockEmail, request);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Thêm khóa học vào hóa đơn thành công", result.getMessage());
  }

  @Test
  public void testAddPaymentDetail_UserNotFound() {
    int courseId = 1;

    AddPaymentRequest request = new AddPaymentRequest();
    request.setCourseId(courseId);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail))).thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class,
            () -> paymentService.addPaymentDetail(mockEmail, request));

    Assertions.assertEquals("Người dùng không tồn tại trong hệ thống", exception.getMessage());
  }

  @Test
  public void testAddPaymentDetail_AlreadySavedCourse() {
    String mockEmail = "email@email.com";
    int courseId = 1;

    AddPaymentRequest request = new AddPaymentRequest();
    request.setCourseId(courseId);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(Mockito.eq(request.getCourseId())))
        .thenReturn(Optional.of(mockCourse));
    Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenReturn(mockPayment);
    Mockito.when(
            myCourseRepository.findByCourseAndStudent(
                Mockito.any(Course.class), Mockito.any(Student.class)))
        .thenReturn(Optional.of(new MyCourse()));

    Exception exception =
        Assertions.assertThrows(
            DataIntegrityViolationException.class,
            () -> paymentService.addPaymentDetail(mockEmail, request));

    Assertions.assertEquals("Khóa học đã được mua", exception.getMessage());
  }

  @Test
  public void testAddPaymentDetail_AlreadyHavePaymentDetail() {
    String mockEmail = "email@email.com";
    int courseId = 1;

    AddPaymentRequest request = new AddPaymentRequest();
    request.setCourseId(courseId);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(courseRepository.findById(Mockito.eq(request.getCourseId())))
        .thenReturn(Optional.of(mockCourse));
    Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenReturn(mockPayment);
    Mockito.when(
            myCourseRepository.findByCourseAndStudent(
                Mockito.any(Course.class), Mockito.any(Student.class)))
        .thenReturn(Optional.empty());
    Mockito.when(
            paymentDetailRepository.findByPaymentAndCourse(
                Mockito.any(Payment.class), Mockito.any(Course.class)))
        .thenReturn(Optional.of(new PaymentDetail()));

    Exception exception =
        Assertions.assertThrows(
            DataIntegrityViolationException.class,
            () -> paymentService.addPaymentDetail(mockEmail, request));

    Assertions.assertEquals("Đã có khóa học trong hóa đơn", exception.getMessage());
  }

  @Test
  public void testAddPaymentDetail_UserNotActive() {
    String mockEmail = "email@email.com";
    int courseId = 1;

    AddPaymentRequest request = new AddPaymentRequest();
    request.setCourseId(courseId);
    mockUser.setStatus(0);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));

    Exception exception =
        Assertions.assertThrows(
            AccessDeniedException.class, () -> paymentService.addPaymentDetail(mockEmail, request));

    Assertions.assertEquals("Tài khoản của bạn chưa được kích hoạt", exception.getMessage());
  }

  @Test
  public void testCheckPayment_Success() {
    String mockEmail = "email@email.com";

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);
    mockPayment.setStatus(1);
    mockUser.setStatus(1);
    mockPayment.setStudent(mockStudent);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(paymentRepository.findById(Mockito.eq(request.getPaymentId())))
        .thenReturn(Optional.of(mockPayment));

    Payment result = paymentService.checkPayment(mockEmail, request);

    Assertions.assertNotNull(result);
  }

  @Test
  public void testCheckPayment_UserNotFound() {
    String mockEmail = "email@email.com";

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail))).thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class,
            () -> paymentService.checkPayment(mockEmail, request));

    Assertions.assertEquals("Người dùng không tồn tại trong hệ thống", exception.getMessage());
  }

  @Test
  public void testCheckPayment_UserNotActive() {
    String mockEmail = "email@email.com";

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));

    Exception exception =
        Assertions.assertThrows(
            AccessDeniedException.class, () -> paymentService.checkPayment(mockEmail, request));

    Assertions.assertEquals("Tài khoản của bạn chưa được kích hoạt", exception.getMessage());
  }

  @Test
  public void testCheckPayment_DoNotHaveAccess() {
    String mockEmail = "email@email.com";

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser))).thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            AccessDeniedException.class, () -> paymentService.checkPayment(mockEmail, request));

    Assertions.assertEquals("Bạn không có quyền thanh toán khóa học", exception.getMessage());
  }

  @Test
  public void testCheckPayment_PaymentNotFound() {
    String mockEmail = "email@email.com";

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);
    mockUser.setStatus(1);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(paymentRepository.findById(Mockito.eq(request.getPaymentId())))
        .thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class,
            () -> paymentService.checkPayment(mockEmail, request));

    Assertions.assertEquals("Không tìm thấy hóa đơn", exception.getMessage());
  }

  @Test
  public void testCheckPayment_CannotPayForAnotherUser() {
    String mockEmail = "email@email.com";

    Student anotherStudent = new Student();

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);
    mockUser.setStatus(1);
    mockPayment.setStudent(anotherStudent);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(paymentRepository.findById(Mockito.eq(request.getPaymentId())))
        .thenReturn(Optional.of(mockPayment));

    Exception exception =
        Assertions.assertThrows(
            AccessDeniedException.class, () -> paymentService.checkPayment(mockEmail, request));

    Assertions.assertEquals(
        "Bạn không thể thanh toán hóa đơn của người khác", exception.getMessage());
  }

  @Test
  public void testCheckPayment_PaymentAlreadyPaid() {
    String mockEmail = "email@email.com";

    PaymentRequest request = new PaymentRequest();
    request.setPaymentId(1);
    mockUser.setStatus(1);
    mockPayment.setStudent(mockStudent);

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findByUser(Mockito.eq(mockUser)))
        .thenReturn(Optional.of(mockStudent));
    Mockito.when(paymentRepository.findById(Mockito.eq(request.getPaymentId())))
        .thenReturn(Optional.of(mockPayment));

    Exception exception =
        Assertions.assertThrows(
            DataIntegrityViolationException.class,
            () -> paymentService.checkPayment(mockEmail, request));

    Assertions.assertEquals("Hóa đơn đã được thanh toán", exception.getMessage());
  }

  @Test
  public void testCallbackPayment_Success() {
    String vnp_TxnRef = "1";

    Mockito.when(paymentRepository.findByPaymentCode(Mockito.eq(vnp_TxnRef)))
        .thenReturn(Optional.of(mockPayment));
    Mockito.when(paymentDetailRepository.findByPayment(Mockito.any(Payment.class)))
        .thenReturn(List.of(new PaymentDetail()));
    Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenReturn(mockPayment);
    Mockito.when(myCourseRepository.save(Mockito.any(MyCourse.class))).thenReturn(new MyCourse());

    PaymentResponse result = paymentService.callbackPayment(vnp_TxnRef);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Thanh toán thành công", result.getMessage());
  }

  @Test
  public void testCallbackPayment_PaymentNotFound() {
    String vnp_TxnRef = "1";

    Mockito.when(paymentRepository.findByPaymentCode(Mockito.eq(vnp_TxnRef)))
        .thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class, () -> paymentService.callbackPayment(vnp_TxnRef));

    Assertions.assertEquals("Không tìm thấy hóa đơn", exception.getMessage());
  }

  @Test
  public void testCreatePayment_Success() throws UnsupportedEncodingException {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Authentication authentication = Mockito.mock(Authentication.class);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(authentication.getName()).thenReturn(mockEmail);
    Mockito.when(courseRepository.findById(Mockito.eq(courseId)))
        .thenReturn(Optional.of(mockCourse));
    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(studentRepository.findById(Mockito.eq(studentId)))
        .thenReturn(Optional.of(mockStudent));

    PaymentDTO.VNPayResponse response = paymentService.createPayment(courseId);

    Assertions.assertNotNull(response);
  }

  @Test
  public void testHandleVnPayReturn_Success() throws UnsupportedEncodingException {
    String vnp_TxnRef = "vnp_TxnRef";
    String vnp_ResponseCode = "00";
    Map<String, String> allParams = new HashMap<>();
    allParams.put("vnp_TxnRef", vnp_TxnRef);
    allParams.put("vnp_ResponseCode", vnp_ResponseCode);

    PaymentDetail paymentDetail = new PaymentDetail();
    paymentDetail.setCourse(mockCourse);
    mockPayment.setPaymentDetails(Set.of(paymentDetail));

    Mockito.when(paymentRepository.findByPaymentCode(Mockito.eq(vnp_TxnRef)))
        .thenReturn(Optional.of(mockPayment));

    paymentService.handleVnPayReturn(allParams);

    Mockito.verify(myCourseRepository, Mockito.times(1)).save(Mockito.any(MyCourse.class));
    Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
  }

  @Test
  public void testHandleVnPayReturn_PaymentResponseCodeNotMatch()
      throws UnsupportedEncodingException {
    String vnp_TxnRef = "vnp_TxnRef";
    String vnp_ResponseCode = "01";
    Map<String, String> allParams = new HashMap<>();
    allParams.put("vnp_TxnRef", vnp_TxnRef);
    allParams.put("vnp_ResponseCode", vnp_ResponseCode);

    Exception exception =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> paymentService.handleVnPayReturn(allParams));
    Assertions.assertEquals(
        "Thông tin thanh toán không hợp lệ với VnPayResponseCode: " + vnp_ResponseCode,
        exception.getMessage());
  }

  @Test
  public void testPaymentSavedCourse_Success() throws UnsupportedEncodingException {
    String mockToken = "mockToken";

    SavedCourse savedCourse = new SavedCourse();
    savedCourse.setCourse(mockCourse);
    GetSavedCourseResponse getSavedCourseResponse = new GetSavedCourseResponse();
    getSavedCourseResponse.setSavedCourse(List.of(savedCourse));

    try (MockedStatic<JwtUtil> jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class)) {
      jwtUtilMockedStatic
          .when(() -> JwtUtil.extractEmail(Mockito.eq(mockToken)))
          .thenReturn(mockEmail);

      Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
          .thenReturn(Optional.of(mockUser));
      Mockito.when(studentRepository.findByUser(Mockito.any(User.class)))
          .thenReturn(Optional.of(mockStudent));
      Mockito.when(savedCourseService.getSavedCoursesByEmail(Mockito.eq(mockEmail)))
          .thenReturn(getSavedCourseResponse);

      PaymentDTO.VNPayResponse response = paymentService.paymentSavedCourse(mockToken);

      Assertions.assertNotNull(response);
      Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
      Mockito.verify(paymentDetailRepository, Mockito.times(1))
          .save(Mockito.any(PaymentDetail.class));
    }
  }

  @Test
  public void testPaymentSavedCourse_UserNotFound() throws UnsupportedEncodingException {
    String mockToken = "mockToken";

    try (MockedStatic<JwtUtil> jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class)) {
      jwtUtilMockedStatic
          .when(() -> JwtUtil.extractEmail(Mockito.eq(mockToken)))
          .thenReturn(mockEmail);

      Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail))).thenReturn(Optional.empty());

      Exception exception =
          Assertions.assertThrows(
              EmptyResultDataAccessException.class,
              () -> paymentService.paymentSavedCourse(mockToken));
      Assertions.assertEquals("Người dùng không tồn tại trong hệ thống", exception.getMessage());
    }
  }

  @Test
  public void testPaymentSavedCourse_ListOfSavedCourseNotFound()
      throws UnsupportedEncodingException {
    String mockToken = "mockToken";

    SavedCourse savedCourse = new SavedCourse();
    savedCourse.setCourse(mockCourse);
    GetSavedCourseResponse getSavedCourseResponse = new GetSavedCourseResponse();
    getSavedCourseResponse.setSavedCourse(List.of());

    try (MockedStatic<JwtUtil> jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class)) {
      jwtUtilMockedStatic
          .when(() -> JwtUtil.extractEmail(Mockito.eq(mockToken)))
          .thenReturn(mockEmail);

      Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
          .thenReturn(Optional.of(mockUser));
      Mockito.when(studentRepository.findByUser(Mockito.any(User.class)))
          .thenReturn(Optional.of(mockStudent));
      Mockito.when(savedCourseService.getSavedCoursesByEmail(Mockito.eq(mockEmail)))
          .thenReturn(getSavedCourseResponse);

      Exception exception =
          Assertions.assertThrows(
              EmptyResultDataAccessException.class,
              () -> paymentService.paymentSavedCourse(mockToken));
      Assertions.assertEquals(
          "Bạn chưa có danh sách khóa học cần thanh toán", exception.getMessage());
    }
  }
}
