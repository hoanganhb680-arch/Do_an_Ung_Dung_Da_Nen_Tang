package org.example.khoahoc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.khoahoc.dto.request.EnrollmentCreationRequest;
import org.example.khoahoc.dto.request.PaymentCheckoutRequest;
import org.example.khoahoc.dto.request.PaymentTransactionCreationRequest;
import org.example.khoahoc.dto.request.PaymentTransactionUpdateRequest;
import org.example.khoahoc.dto.request.WebhookCallbackRequest;
import org.example.khoahoc.dto.response.GatewayInitiateResponse;
import org.example.khoahoc.dto.response.PaymentCheckoutResponse;
import org.example.khoahoc.dto.response.PaymentTransactionResponse;
import org.example.khoahoc.entity.PaymentTransaction;
import org.example.khoahoc.entity.TransactionItem;
import org.example.khoahoc.enums.PaymentStatus;
import org.example.khoahoc.exception.AppException;
import org.example.khoahoc.exception.ErrorCode;
import org.example.khoahoc.repository.PaymentTransactionRepository;
import org.example.khoahoc.repository.TransactionItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final EnrollmentService enrollmentService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final PaymentSignatureService paymentSignatureService;

    @Value("${payment.webhook.api-key:GATEWAY_API_KEY}")
    String webhookApiKey;

    @Value("${payment.webhook.secret-key:GATEWAY_SECRET_KEY}")
    String webhookSecretKey;

    @Value("${payment.webhook.callback-url:http://localhost:8080/api/payments/ipn}")
    String webhookCallbackUrl;

    public PaymentTransactionResponse createTransaction(PaymentTransactionCreationRequest request) {
        log.info("Creating new payment transaction for orderId: {}", request.getOrderId());

        PaymentTransaction transaction = PaymentTransaction.builder()
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionRef(request.getTransactionRef())
                .ipAddress(request.getIpAddress())
                .status(PaymentStatus.PENDING)
                .build();

        transaction = paymentTransactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    @Transactional
    public PaymentCheckoutResponse checkout(PaymentCheckoutRequest request, String clientIp) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .userId(request.getUserId())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod("THIRD_PARTY_MOCK")
                .ipAddress(clientIp)
                .status(PaymentStatus.PENDING)
                .transactionRef(UUID.randomUUID().toString())
                .build();

        transaction = paymentTransactionRepository.save(transaction);

        GatewayInitiateResponse gatewayResponse = paymentGatewayClient.initiate(
                transaction,
                request.getReturnUrl(),
                webhookCallbackUrl
        );

        if (gatewayResponse != null && gatewayResponse.getGatewayTransactionRef() != null) {
            transaction.setTransactionRef(gatewayResponse.getGatewayTransactionRef());
            transaction = paymentTransactionRepository.save(transaction);
        }

        return PaymentCheckoutResponse.builder()
                .transaction(mapToResponse(transaction))
                .paymentUrl(gatewayResponse != null ? gatewayResponse.getPaymentUrl() : null)
                .build();
    }

    @Transactional
    public PaymentTransactionResponse handleWebhook(WebhookCallbackRequest request, String requestApiKey, String requestSignature) {
        if (requestApiKey == null || !webhookApiKey.equals(requestApiKey)) {
            throw new AppException(ErrorCode.INVALID_API_KEY);
        }

        String payload = buildWebhookPayload(request);
        if (requestSignature == null || !paymentSignatureService.verify(payload, requestSignature, webhookSecretKey)) {
            throw new AppException(ErrorCode.INVALID_SIGNATURE);
        }

        PaymentTransaction transaction = paymentTransactionRepository.findByTransactionRef(request.getTransactionRef())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if (transaction.getCallbackProcessedAt() != null) {
            log.info("Payment transaction {} already processed, skipping duplicate webhook.", transaction.getTransactionRef());
            return mapToResponse(transaction);
        }

        PaymentStatus newStatus;
        try {
            newStatus = PaymentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        transaction.setStatus(newStatus);
        transaction.setCallbackProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        if (newStatus == PaymentStatus.SUCCESS) {
            createEnrollmentsFromTransaction(transaction);
        }

        return mapToResponse(transaction);
    }

    public PaymentTransactionResponse getTransaction(Long id) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));
        return mapToResponse(transaction);
    }

    public List<PaymentTransactionResponse> getAllTransactions() {
        return paymentTransactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentTransactionResponse> getTransactionsByOrderId(Long orderId) {
        return paymentTransactionRepository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentTransactionResponse updateTransaction(Long id, PaymentTransactionUpdateRequest request) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));

        if (request.getStatus() != null) {
            try {
                transaction.setStatus(PaymentStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (Exception e) {
                throw new AppException(ErrorCode.INVALID_PAYMENT_STATUS);
            }
        }
        if (request.getTransactionRef() != null) transaction.setTransactionRef(request.getTransactionRef());

        transaction = paymentTransactionRepository.save(transaction);
        return mapToResponse(transaction);
    }

    public void deleteTransaction(Long id) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_TRANSACTION_NOT_FOUND));
        paymentTransactionRepository.delete(transaction);
    }

    private void createEnrollmentsFromTransaction(PaymentTransaction transaction) {
        List<TransactionItem> items = transactionItemRepository.findByTransactionId(transaction.getTransactionId());
        for (TransactionItem item : items) {
            try {
                enrollmentService.createEnrollment(EnrollmentCreationRequest.builder()
                        .userId(transaction.getUserId())
                        .courseId(item.getCourseId())
                        .build());
            } catch (AppException e) {
                if (e.getErrorCode() != ErrorCode.ENROLLMENT_EXISTED) {
                    throw e;
                }
            }
        }
    }

    private String buildWebhookPayload(WebhookCallbackRequest request) {
        return request.getTransactionRef() + "|" +
                request.getOrderId() + "|" +
                request.getUserId() + "|" +
                request.getAmount() + "|" +
                request.getStatus() + "|" +
                request.getTimestamp() + "|" +
                request.getNonce();
    }

    private PaymentTransactionResponse mapToResponse(PaymentTransaction transaction) {
        return PaymentTransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .orderId(transaction.getOrderId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionRef(transaction.getTransactionRef())
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .ipAddress(transaction.getIpAddress())
                .createdDate(transaction.getCreatedDate())
                .updatedDate(transaction.getUpdatedDate())
                .callbackProcessedAt(transaction.getCallbackProcessedAt())
                .build();
    }
}
