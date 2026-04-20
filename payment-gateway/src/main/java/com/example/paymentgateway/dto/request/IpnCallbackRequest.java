package com.example.paymentgateway.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IpnCallbackRequest {
    String gatewayTransactionRef;
    Long orderId;
    Long userId;
    BigDecimal amount;
    String status;
    String timestamp;
    String nonce;
}
