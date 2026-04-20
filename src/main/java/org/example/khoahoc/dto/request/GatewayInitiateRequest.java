package org.example.khoahoc.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GatewayInitiateRequest {
    String transactionRef;
    Long orderId;
    Long userId;
    BigDecimal amount;
    String returnUrl;
    String ipnUrl;
    String timestamp;
    String nonce;
}
