package com.example.paymentgateway.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitiatePaymentResponse {
    String gatewayTransactionRef;
    String paymentUrl;
    String status;
}
