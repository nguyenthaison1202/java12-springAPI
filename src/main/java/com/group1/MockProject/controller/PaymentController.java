package com.group1.MockProject.controller;

import com.group1.MockProject.dto.ApiResponseDto;
import com.group1.MockProject.dto.PaymentDTO;
import com.group1.MockProject.service.PaymentService;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping("/create_payment/{course_id}")
  public ResponseEntity<?> paymentCourse(@PathVariable("course_id") int courseId)
      throws UnsupportedEncodingException {
    PaymentDTO.VNPayResponse paymentResponse = paymentService.createPayment(courseId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponseDto.<PaymentDTO.VNPayResponse>builder()
                .status(201)
                .message(HttpStatus.CREATED.getReasonPhrase())
                .response(paymentResponse)
                .build());
  }

  @GetMapping("/vnPay_return")
  public ResponseEntity<?> vnPayReturn(@RequestParam Map<String, String> allParams) {
    paymentService.handleVnPayReturn(allParams);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(
            ApiResponseDto.builder()
                .status(204)
                .message(HttpStatus.NO_CONTENT.getReasonPhrase())
                .build());
  }

  @GetMapping("/create-payment/savedCourses")
  public ResponseEntity<?> paymentSavedCourse(
      @RequestHeader("Authorization") String authorizationHeader)
      throws UnsupportedEncodingException {
    String token = authorizationHeader.replace("Bearer ", "");

    PaymentDTO.VNPayResponse paymentResponse = paymentService.paymentSavedCourse(token);
    return ResponseEntity.ok()
        .body(
            ApiResponseDto.<PaymentDTO.VNPayResponse>builder()
                .status(200)
                .message(HttpStatus.OK.getReasonPhrase())
                .response(paymentResponse)
                .build());
  }
}
