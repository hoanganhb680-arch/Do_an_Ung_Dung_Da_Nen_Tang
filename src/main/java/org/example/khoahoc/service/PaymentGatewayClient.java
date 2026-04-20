package org.example.khoahoc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.khoahoc.dto.request.GatewayInitiateRequest;
import org.example.khoahoc.dto.response.GatewayInitiateResponse;
import org.example.khoahoc.entity.PaymentTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayClient {

    private final PaymentSignatureService paymentSignatureService;

    @Value("${payment.gateway.base-url:http://localhost:8090}")
    private String gatewayBaseUrl;

    @Value("${payment.gateway.api-key:LMS_API_KEY}")
    private String gatewayApiKey;

    @Value("${payment.gateway.secret-key:LMS_SECRET_KEY}")
    private String gatewaySecretKey;

    public GatewayInitiateResponse initiate(PaymentTransaction transaction, String returnUrl, String ipnUrl) {
        GatewayInitiateRequest request = GatewayInitiateRequest.builder()
                .transactionRef(transaction.getTransactionRef())
                .orderId(transaction.getOrderId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .returnUrl(returnUrl)
                .ipnUrl(ipnUrl)
                .timestamp(String.valueOf(Instant.now().toEpochMilli()))
                .nonce(UUID.randomUUID().toString())
                .build();

        String payload = buildPayload(request);
        String signature = paymentSignatureService.sign(payload, gatewaySecretKey);

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(gatewayBaseUrl)
                    .build();

            GatewayInitiateResponse response = restClient.post()
                    .uri("/gateway/payments/initiate")
                    .header("X-Api-Key", gatewayApiKey)
                    .header("X-Signature", signature)
                    .body(request)
                    .retrieve()
                    .body(GatewayInitiateResponse.class);

            if (response != null) {
                return response;
            }
        } catch (Exception ex) {
            log.warn("Cannot reach payment gateway at {}. Using local fallback response. Reason: {}", gatewayBaseUrl, ex.getMessage());
        }

        return GatewayInitiateResponse.builder()
                .gatewayTransactionRef(transaction.getTransactionRef())
                .paymentUrl("http://localhost:8090/mock-payment/" + transaction.getTransactionRef())
                .status("PENDING")
                .build();
    }

    private String buildPayload(GatewayInitiateRequest request) {
        return request.getTransactionRef() + "|" +
                request.getOrderId() + "|" +
                request.getUserId() + "|" +
                request.getAmount() + "|" +
                request.getReturnUrl() + "|" +
                request.getIpnUrl() + "|" +
                request.getTimestamp() + "|" +
                request.getNonce();
    }
}
