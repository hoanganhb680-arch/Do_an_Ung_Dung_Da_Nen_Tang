package com.example.paymentgateway.controller;

import com.example.paymentgateway.dto.request.InitiatePaymentRequest;
import com.example.paymentgateway.dto.response.InitiatePaymentResponse;
import com.example.paymentgateway.service.GatewayPaymentService;
import com.example.paymentgateway.service.GatewaySignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/gateway/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class GatewayPaymentController {

    private final GatewayPaymentService gatewayPaymentService;
    private final GatewaySignatureService gatewaySignatureService;

    @Value("${payment.merchant.api-key:LMS_API_KEY}")
    private String expectedApiKey;

    @Value("${payment.gateway.secret-key:SHARED_HMAC_SECRET_2024}")
    private String secretKey;

    @Value("${payment.gateway.allowed-ips:127.0.0.1,0:0:0:0:0:0:0:1,::1,localhost}")
    private String allowedIps;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Signature", required = false) String signature,
            HttpServletRequest httpServletRequest,
            @RequestBody InitiatePaymentRequest request) {

        log.info("=== Initiate Payment Request ===");
        log.info("Received API Key: [{}], Expected: [{}]", apiKey, expectedApiKey);

        if (apiKey == null || !expectedApiKey.equals(apiKey)) {
            log.warn("API Key mismatch! Rejecting.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key");
        }

        String payload = buildPayload(request);
        String expectedSig = gatewaySignatureService.sign(payload, secretKey);
        log.info("Payload built: [{}]", payload);
        log.info("Received Signature: [{}]", signature);
        log.info("Expected Signature: [{}]", expectedSig);

        if (signature == null || !gatewaySignatureService.verify(payload, signature, secretKey)) {
            log.warn("Signature mismatch! Rejecting.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        String clientIp = resolveClientIp(httpServletRequest);
        log.info("Client IP: [{}], Allowed IPs: [{}]", clientIp, allowedIps);
        if (!isAllowedIp(clientIp)) {
            log.warn("IP not allowed! Rejecting.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("IP not allowed");
        }

        InitiatePaymentResponse response = gatewayPaymentService.initiate(request);
        return ResponseEntity.ok(response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAllowedIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return false;
        }
        for (String allowed : allowedIps.split(",")) {
            if (clientIp.equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }
        return false;
    }

    private String buildPayload(InitiatePaymentRequest request) {
        return request.getTransactionRef() + "|" +
                request.getOrderId() + "|" +
                request.getUserId() + "|" +
                request.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + "|" +
                request.getReturnUrl() + "|" +
                request.getIpnUrl() + "|" +
                request.getTimestamp() + "|" +
                request.getNonce();
    }
}
