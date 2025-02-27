package com.group1.MockProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailDTO {
    private int id;
    private String nameCoures;
    private LocalDate paymentDate;
    private double price;
}
