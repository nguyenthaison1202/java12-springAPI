package com.group1.MockProject.service;

import com.group1.MockProject.dto.PaymentDTO;
import com.group1.MockProject.dto.request.PaymentRequest;
import com.group1.MockProject.dto.response.AddPaymentResponse;
import com.group1.MockProject.dto.response.PaymentResponse;
import com.group1.MockProject.dto.response.PaymentResponseDTO;
import com.group1.MockProject.entity.Payment;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface PaymentService {
  AddPaymentResponse addPaymentDetail(String email, PaymentRequest request);

  PaymentResponse callbackPayment(String vnp_TxnRef);

  PaymentDTO.VNPayResponse createPayment(int idCourse) throws UnsupportedEncodingException;

  Payment checkPayment(String email, PaymentRequest request);

  void handleVnPayReturn(Map<String, String> allParams);

  PaymentDTO.VNPayResponse paymentSavedCourse(String token) throws UnsupportedEncodingException;

  PaymentResponse freePayment(Payment payment);

  PaymentResponse callbackPaymentFail(String vnp_TxnRef);

  PaymentResponseDTO getAllPayment(String email);

  void deletePaymentDetail(String email, PaymentRequest request);
}
