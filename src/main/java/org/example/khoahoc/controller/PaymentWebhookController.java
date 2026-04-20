package org.example.khoahoc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.khoahoc.dto.request.WebhookCallbackRequest;
import org.example.khoahoc.dto.response.PaymentTransactionResponse;
import org.example.khoahoc.service.PaymentTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentTransactionService paymentTransactionService;

    @PostMapping({"/webhook/payment", "/payments/ipn", "/payments/callback"})
    public ResponseEntity<String> handlePaymentWebhook(
            @RequestHeader(value = "X-Api-Key", required = false) String requestApiKey,
            @RequestHeader(value = "X-Signature", required = false) String requestSignature,
            @RequestBody WebhookCallbackRequest request) {

        PaymentTransactionResponse updatedTransaction = paymentTransactionService.handleWebhook(
                request,
                requestApiKey,
                requestSignature
        );

        log.info("Webhook handled successfully for transactionRef={}, status={}",
                updatedTransaction.getTransactionRef(), updatedTransaction.getStatus());

        return ResponseEntity.ok("Webhook xử lý thành công");
    }
}
