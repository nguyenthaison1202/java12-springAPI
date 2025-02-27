//package com.group1.MockProject.controller;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.group1.MockProject.dto.PaymentDTO;
//import com.group1.MockProject.exception.GlobalExceptionHandler;
//import com.group1.MockProject.service.PaymentService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//
//@ExtendWith(MockitoExtension.class)
//class PaymentControllerTest {
//
//  private MockMvc mockMvc;
//
//  @Mock private PaymentService paymentService;
//
//  @InjectMocks private PaymentController paymentController;
//
//  @BeforeEach
//  void setUp() {
//    paymentController = new PaymentController(paymentService);
//
//    mockMvc =
//        MockMvcBuilders.standaloneSetup(paymentController)
//            .alwaysDo(print())
//            .setControllerAdvice(new GlobalExceptionHandler())
//            .build();
//  }
//
//  @Test
//  public void testPaymentCourse_Success() throws Exception {
//    int courseId = 1;
//    PaymentDTO.VNPayResponse paymentResponse = PaymentDTO.VNPayResponse.builder().build();
//
//    Mockito.when(paymentService.createPayment(Mockito.eq(courseId))).thenReturn(paymentResponse);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/payment/create_payment/" + courseId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.status").value(201))
//        .andExpect(jsonPath("$.message").value("Created"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//
//  @Test
//  public void testPaymentVnPayReturn_Success() throws Exception {
//    MultiValueMap<String, String> allParams = new LinkedMultiValueMap<>();
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/payment/vnPay_return")
//                .contentType(MediaType.APPLICATION_JSON)
//                .params(allParams)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isNoContent())
//        .andExpect(jsonPath("$.status").value(204))
//        .andExpect(jsonPath("$.message").value("No Content"));
//  }
//
//  @Test
//  public void testPaymentSavedCourse_Success() throws Exception {
//    String mockToken = "mockToken";
//
//    PaymentDTO.VNPayResponse paymentResponse = PaymentDTO.VNPayResponse.builder().build();
//
//    Mockito.when(paymentService.paymentSavedCourse(Mockito.eq(mockToken)))
//        .thenReturn(paymentResponse);
//
//    mockMvc
//        .perform(
//            MockMvcRequestBuilders.get("/api/v1/payment/create-payment/savedCourses")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + mockToken)
//                .with(SecurityMockMvcRequestPostProcessors.csrf()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.status").value(200))
//        .andExpect(jsonPath("$.message").value("OK"))
//        .andExpect(jsonPath("$.response").exists());
//  }
//}
