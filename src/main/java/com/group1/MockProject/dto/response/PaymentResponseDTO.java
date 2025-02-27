package com.group1.MockProject.dto.response;

import com.group1.MockProject.dto.PaymentDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private List<PaymentDetailDTO> payments;
    private double totalPrice;

    // Constructor tính tổng giá
    public PaymentResponseDTO(List<PaymentDetailDTO> payments) {
        this.payments = payments;
        this.totalPrice = payments.stream().mapToDouble(PaymentDetailDTO::getPrice).sum();
    }
}
