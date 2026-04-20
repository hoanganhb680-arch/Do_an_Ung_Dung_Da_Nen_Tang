package org.example.khoahoc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.khoahoc.dto.request.OrderCreationRequest;
import org.example.khoahoc.dto.request.PaymentCheckoutRequest;
import org.example.khoahoc.dto.request.TransactionItemCreationRequest;
import org.example.khoahoc.dto.response.OrderResponse;
import org.example.khoahoc.dto.response.PaymentCheckoutResponse;
import org.example.khoahoc.entity.TransactionItem;
import org.example.khoahoc.repository.TransactionItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final PaymentTransactionService paymentTransactionService;
    private final TransactionItemRepository transactionItemRepository;

    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request, String clientIp) {
        long orderId = Instant.now().toEpochMilli();
        String orderCode = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        PaymentCheckoutResponse checkoutResponse = paymentTransactionService.checkout(
                PaymentCheckoutRequest.builder()
                        .userId(request.getUserId())
                        .orderId(orderId)
                        .amount(request.getAmount())
                        .returnUrl(request.getReturnUrl())
                        .build(),
                clientIp
        );

        List<Long> courseIds = request.getCourseIds() == null ? Collections.emptyList() : request.getCourseIds();
        if (!courseIds.isEmpty() && checkoutResponse.getTransaction() != null) {
            BigDecimal itemAmount = request.getAmount() == null
                    ? BigDecimal.ZERO
                    : request.getAmount().divide(BigDecimal.valueOf(courseIds.size()), 2, RoundingMode.HALF_UP);

            for (Long courseId : courseIds) {
                TransactionItem item = TransactionItem.builder()
                        .transactionId(checkoutResponse.getTransaction().getTransactionId())
                        .courseId(courseId)
                        .amount(itemAmount.doubleValue())
                        .build();
                transactionItemRepository.save(item);
            }
        }

        return OrderResponse.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .userId(request.getUserId())
                .courseIds(courseIds)
                .transaction(checkoutResponse.getTransaction())
                .paymentUrl(checkoutResponse.getPaymentUrl())
                .status(checkoutResponse.getTransaction() != null ? checkoutResponse.getTransaction().getStatus() : null)
                .build();
    }
}
