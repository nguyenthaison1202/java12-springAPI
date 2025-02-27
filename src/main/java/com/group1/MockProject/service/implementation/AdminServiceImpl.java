package com.group1.MockProject.service.implementation;

import com.group1.MockProject.dto.MessageDTO;
import com.group1.MockProject.dto.request.SignInRequest;
import com.group1.MockProject.dto.response.AdminDashboardResponse;
import com.group1.MockProject.dto.response.SignInResponse;
import com.group1.MockProject.entity.*;
import com.group1.MockProject.entity.User;
import com.group1.MockProject.repository.CourseRepository;
import com.group1.MockProject.repository.PaymentRepository;
import com.group1.MockProject.repository.StudentRepository;
import com.group1.MockProject.repository.UserRepository;
import com.group1.MockProject.service.AdminService;
import com.group1.MockProject.service.EmailService;
import com.group1.MockProject.utils.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final CourseRepository courseRepository;
  private final EmailService emailService;
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;
  private final CourseServiceImpl courseService;
  private final PaymentRepository paymentRepository;
  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public void deleteUser(int userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public AdminDashboardResponse getDashboardData() {
    try {
      // Thống kê người dùng
      long totalUsers = userRepository.count();
      long totalStudents = studentRepository.count();
      long totalInstructors = userRepository.countByRole(UserRole.INSTRUCTOR);
      LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
      long newUsersThisMonth = userRepository.countByCreatedDateAfter(startOfMonth);
      long newPaymentsThisMonth = paymentRepository.countByCreatedDateAfter(startOfMonth);
      // Thống kê khóa học
      long totalCourses = courseRepository.count();
      long approvedCourses = courseRepository.countByStatus(1);
      long pendingCourses = courseRepository.countByStatus(0);
      long rejectedCourses = courseRepository.countByStatus(-1);

      // Thống kê doanh thu
      double totalRevenue = courseRepository.calculateTotalRevenue();

      // Thống kê đánh giá
      double averageRating = courseRepository.calculateAverageRating();
      long totalReviews = courseRepository.countTotalReviews();

      // Tạo và trả về response
      return AdminDashboardResponse.builder()
          .totalUsers((int) totalUsers)
          .totalStudents((int) totalStudents)
          .totalInstructors((int) totalInstructors)
          .newUsersThisMonth((int) newUsersThisMonth)
          .totalCourses((int) totalCourses)
          .totalRevenue(totalRevenue)
          .revenueThisMonth(totalRevenue)
          .averageRating(averageRating)
          .totalReviews((int) totalReviews)
          .approvedCourses((int) approvedCourses)
          .pendingCourses((int) pendingCourses)
          .rejectedCourses((int) rejectedCourses)
          .build();
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi lấy dữ liệu dashboard: " + e.getMessage());
    }
  }

  @Override
  public MessageDTO setRejectInstructor(int userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EmptyResultDataAccessException("Người dùng không tồn tại", 1));

    if (user.getStatus() == -1) {
      throw new DataIntegrityViolationException(
          "Người dùng đã bị từ chối, không thể từ chối lần nữa");
    }
    if (user.getRole() == UserRole.ADMIN) {
      throw new IllegalArgumentException("Không thể từ chối người dùng");
    }
    user.setStatus(-1);
    userRepository.save(user);
    String instructorName = user.getFullName();
    String reason = "Người dùng đã bị từ chối. Vui lòng đăng ký thông tin hợp lệ";
    emailService.send(user.getEmail(),emailService.buildRejectInstructorEmail(instructorName,reason));
    return new MessageDTO("Từ chối người dùng thành công");
  }

  @Override
  public MessageDTO setApproveInstructor(int userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EmptyResultDataAccessException("Người dùng không tồn tại", 1));

    if (user.getStatus() == 1) {
      throw new DataIntegrityViolationException(
          "Người dùng đã được duyệt, không thể duyệt lần nữa");
    }
    if(user.getRole() != UserRole.INSTRUCTOR) {
      throw new AccessDeniedException("Người dùng không phải là giảng viên");
    }

    user.setStatus(1);
    userRepository.save(user);
    String instructorName = user.getFullName();
    String reason = "Người dùng đã được duyệt thành công";
    emailService.send(user.getEmail(),emailService.buildApprovedInstructorEmail(instructorName,reason));
    return new MessageDTO("Duyệt người dùng thành công");
  }

  @Override
  public SignInResponse authenticate(SignInRequest request) {

    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Sai email hoặc mật khẩu"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Sai email hoặc mật khẩu");
    }

    // Giả sử bạn sử dụng JWT để tạo token
    String token = JwtUtil.generateToken(user); // Hàm này cần được triển khai riêng

    return new SignInResponse(token, "Bearer", "Đăng nhập thành công");
  }

//  @Override
//  public MessageDTO setRejectCourse(int courseId) {
//    courseService.updateCourseStatus(courseId, CourseStatus.APPROVED);
//    return new MessageDTO("Từ chối khóa học thành công");
//  }
}
