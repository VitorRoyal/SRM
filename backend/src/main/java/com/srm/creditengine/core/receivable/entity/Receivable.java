package com.srm.creditengine.core.receivable.entity;

import com.srm.creditengine.core.assignor.entity.Assignor;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "receivable")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignor_id", nullable = false)
    private Assignor assignor;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ReceivableType type;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal faceValue;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Receivable(
            Assignor assignor,
            String documentNumber,
            ReceivableType type,
            BigDecimal faceValue,
            LocalDate dueDate,
            Instant createdAt
    ) {
        this.assignor = assignor;
        this.documentNumber = documentNumber;
        this.type = type;
        this.faceValue = faceValue;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }
}
