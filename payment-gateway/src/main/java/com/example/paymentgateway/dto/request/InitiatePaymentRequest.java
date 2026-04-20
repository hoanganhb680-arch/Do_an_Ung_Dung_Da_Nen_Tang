package com.example.paymentgateway.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitiatePaymentRequest {
    String transactionRef;
    Long orderId;
    Long userId;
    BigDecimal amount;
    String returnUrl;
    String ipnUrl;
    String timestamp;
    String nonce;
}
