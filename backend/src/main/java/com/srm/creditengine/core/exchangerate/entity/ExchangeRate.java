package com.srm.creditengine.core.exchangerate.entity;

import com.srm.creditengine.core.exchangerate.exchangeRateEnum.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Immutable
@Table(name = "exchange_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private CurrencyCode currency;

    @Column(name = "brl_per_unit", nullable = false, precision = 18, scale = 8)
    private BigDecimal brlPerUnit;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ExchangeRate(CurrencyCode currency, BigDecimal brlPerUnit, Instant effectiveAt, Instant createdAt) {
        this.currency = currency;
        this.brlPerUnit = brlPerUnit;
        this.effectiveAt = effectiveAt;
        this.createdAt = createdAt;
    }
}
