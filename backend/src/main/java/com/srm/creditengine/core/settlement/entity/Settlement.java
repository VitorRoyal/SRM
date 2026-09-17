package com.srm.creditengine.core.settlement.entity;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import com.srm.creditengine.core.receivable.entity.Receivable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Immutable
@Table(name = "settlement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receivable_id", nullable = false, unique = true)
    private Receivable receivable;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Column(name = "term_in_months", nullable = false)
    private int termInMonths;

    @Column(name = "base_rate_id", nullable = false)
    private Long baseRateId;

    @Column(name = "monthly_base_rate", nullable = false, precision = 9, scale = 6)
    private BigDecimal monthlyBaseRate;

    @Column(name = "monthly_spread", nullable = false, precision = 9, scale = 6)
    private BigDecimal monthlySpread;

    @Column(name = "present_value_brl", nullable = false, precision = 19, scale = 2)
    private BigDecimal presentValueBrl;

    @Column(name = "discount_brl", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountBrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", nullable = false, length = 3)
    private CurrencyCode paymentCurrency;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "exchange_rate_id")
    private Long exchangeRateId;

    @Column(name = "exchange_rate_brl_per_unit", precision = 18, scale = 8)
    private BigDecimal exchangeRateBrlPerUnit;

    @Column(name = "settled_at", nullable = false)
    private Instant settledAt;

    @Builder
    public Settlement(
            Receivable receivable,
            String idempotencyKey,
            String requestFingerprint,
            int termInMonths,
            Long baseRateId,
            BigDecimal monthlyBaseRate,
            BigDecimal monthlySpread,
            BigDecimal presentValueBrl,
            BigDecimal discountBrl,
            CurrencyCode paymentCurrency,
            BigDecimal paymentAmount,
            Long exchangeRateId,
            BigDecimal exchangeRateBrlPerUnit,
            Instant settledAt
    ) {
        this.receivable = receivable;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.termInMonths = termInMonths;
        this.baseRateId = baseRateId;
        this.monthlyBaseRate = monthlyBaseRate;
        this.monthlySpread = monthlySpread;
        this.presentValueBrl = presentValueBrl;
        this.discountBrl = discountBrl;
        this.paymentCurrency = paymentCurrency;
        this.paymentAmount = paymentAmount;
        this.exchangeRateId = exchangeRateId;
        this.exchangeRateBrlPerUnit = exchangeRateBrlPerUnit;
        this.settledAt = settledAt;
    }
}
