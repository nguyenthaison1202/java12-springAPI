package com.group1.MockProject.repository;

import com.group1.MockProject.entity.Course;
import com.group1.MockProject.entity.Payment;
import com.group1.MockProject.entity.PaymentDetail;
import com.group1.MockProject.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Integer> {

  List<PaymentDetail> findByPayment(Payment payment);
  Optional<PaymentDetail> findByCourse(Course course);
  Optional<PaymentDetail> findByPaymentAndCourse(Payment payment, Course course);
  Optional<PaymentDetail> findByCourseId(int courseId);
  List<PaymentDetail> findPaymentDetailByStudent(Student student);
  Optional<PaymentDetail> findPaymentDetailByCourseAndStudent(Course course, Student student);
}
