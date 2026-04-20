package com.example.paymentgateway.service;

import com.example.paymentgateway.dto.request.InitiatePaymentRequest;
import com.example.paymentgateway.dto.request.IpnCallbackRequest;
import com.example.paymentgateway.dto.response.InitiatePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GatewayPaymentService {

    private final GatewaySignatureService gatewaySignatureService;
    private final LmsCallbackClient lmsCallbackClient;

    @Value("${payment.gateway.secret-key:GATEWAY_SECRET_KEY}")
    private String secretKey;

    @Value("${payment.merchant.api-key:LMS_API_KEY}")
    private String apiKey;

    public InitiatePaymentResponse initiate(InitiatePaymentRequest request) {
        String gatewayTransactionRef = UUID.randomUUID().toString();
        String status = "SUCCESS"; // mock demo: đổi sang FAILED nếu muốn test

        IpnCallbackRequest callbackRequest = IpnCallbackRequest.builder()
                .gatewayTransactionRef(gatewayTransactionRef)
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status(status)
                .timestamp(request.getTimestamp())
                .nonce(request.getNonce())
                .build();

        String payload = callbackRequest.getGatewayTransactionRef() + "|" +
                callbackRequest.getOrderId() + "|" +
                callbackRequest.getUserId() + "|" +
                callbackRequest.getAmount() + "|" +
                callbackRequest.getStatus() + "|" +
                callbackRequest.getTimestamp() + "|" +
                callbackRequest.getNonce();

        String signature = gatewaySignatureService.sign(payload, secretKey);
        lmsCallbackClient.notifyLms(request.getIpnUrl(), callbackRequest, apiKey, signature);

        return InitiatePaymentResponse.builder()
                .gatewayTransactionRef(gatewayTransactionRef)
                .paymentUrl("http://localhost:8090/mock-payment/" + gatewayTransactionRef)
                .status("PENDING")
                .build();
    }
}
