package org.example.khoahoc.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.khoahoc.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment_transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    Long transactionId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "order_id", nullable = false)
    Long orderId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    String paymentMethod;

    @Column(name = "ip_address", length = 50)
    String ipAddress;

    @Column(name = "transaction_ref", length = 100, unique = true)
    String transactionRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    PaymentStatus status;

    @Column(name = "callback_processed_at")
    LocalDateTime callbackProcessedAt;

    @Column(name = "created_date")
    LocalDateTime createdDate;

    @Column(name = "updated_date")
    LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (transactionRef == null || transactionRef.isBlank()) {
            transactionRef = java.util.UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
