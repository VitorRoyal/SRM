package com.srm.creditengine.core.settlement.service;

import com.srm.creditengine.core.assignor.entity.Assignor;
import com.srm.creditengine.core.assignor.service.AssignorService;
import com.srm.creditengine.core.pricing.dto.PricingQuote;
import com.srm.creditengine.core.pricing.dto.PricingResult;
import com.srm.creditengine.core.pricing.service.PricingQuoteService;
import com.srm.creditengine.core.receivable.entity.Receivable;
import com.srm.creditengine.core.receivable.repository.ReceivableRepository;
import com.srm.creditengine.core.settlement.dto.SettlementRequestDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResponseDTO;
import com.srm.creditengine.core.settlement.dto.SettlementResult;
import com.srm.creditengine.core.settlement.entity.Settlement;
import com.srm.creditengine.core.settlement.exception.IdempotencyKeyReuseException;
import com.srm.creditengine.core.settlement.mapper.SettlementMapper;
import com.srm.creditengine.core.settlement.repository.SettlementRepository;
import com.srm.creditengine.shared.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class SettlementService {

    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementRepository settlementRepository;
    private final ReceivableRepository receivableRepository;
    private final AssignorService assignorService;
    private final PricingQuoteService pricingQuoteService;
    private final SettlementMapper settlementMapper;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public SettlementService(
            SettlementRepository settlementRepository,
            ReceivableRepository receivableRepository,
            AssignorService assignorService,
            PricingQuoteService pricingQuoteService,
            SettlementMapper settlementMapper,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        this.settlementRepository = settlementRepository;
        this.receivableRepository = receivableRepository;
        this.assignorService = assignorService;
        this.pricingQuoteService = pricingQuoteService;
        this.settlementMapper = settlementMapper;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    public SettlementResult settle(String idempotencyKey, SettlementRequestDTO settlementRequestDTO) {
        String requestFingerprint = fingerprint(settlementRequestDTO);

        Optional<Settlement> existingSettlement = settlementRepository.findByIdempotencyKeyWithReceivable(idempotencyKey);
        if (existingSettlement.isPresent()) {
            return replay(existingSettlement.get(), requestFingerprint);
        }

        try {
            SettlementResponseDTO settlementResponseDTO = transactionTemplate.execute(
                    transactionStatus -> createSettlement(idempotencyKey, requestFingerprint, settlementRequestDTO)
            );
            logger.info("Settlement {} created for idempotency key {}", settlementResponseDTO.id(), idempotencyKey);
            return new SettlementResult(settlementResponseDTO, false);
        } catch (DataIntegrityViolationException | ConflictException exception) {
            return resolveConcurrentAttempt(idempotencyKey, requestFingerprint, exception);
        }
    }

    private SettlementResponseDTO createSettlement(
            String idempotencyKey,
            String requestFingerprint,
            SettlementRequestDTO settlementRequestDTO
    ) {
        Assignor assignor = assignorService.getById(settlementRequestDTO.assignorId());
        String documentNumber = settlementRequestDTO.documentNumber().trim();

        boolean receivableAlreadySettled = receivableRepository.existsByAssignorIdAndTypeAndDocumentNumber(
                assignor.getId(),
                settlementRequestDTO.receivableType(),
                documentNumber
        );
        if (receivableAlreadySettled) {
            throw new ConflictException("Receivable " + documentNumber + " has already been settled for this assignor");
        }

        Instant settledAt = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        PricingQuote pricingQuote = pricingQuoteService.quote(
                settlementRequestDTO.receivableType(),
                settlementRequestDTO.faceValue(),
                settlementRequestDTO.dueDate(),
                settlementRequestDTO.paymentCurrency(),
                settledAt
        );
        PricingResult pricingResult = pricingQuote.pricingResult();

        Receivable receivable = receivableRepository.save(new Receivable(
                assignor,
                documentNumber,
                settlementRequestDTO.receivableType(),
                settlementRequestDTO.faceValue(),
                settlementRequestDTO.dueDate(),
                settledAt
        ));

        Long exchangeRateId = null;
        if (pricingQuote.exchangeRate() != null) {
            exchangeRateId = pricingQuote.exchangeRate().getId();
        }

        Settlement settlement = settlementRepository.saveAndFlush(Settlement.builder()
                .receivable(receivable)
                .idempotencyKey(idempotencyKey)
                .requestFingerprint(requestFingerprint)
                .termInMonths(pricingQuote.termInMonths())
                .baseRateId(pricingQuote.baseRate().getId())
                .monthlyBaseRate(pricingQuote.baseRate().getMonthlyRate())
                .monthlySpread(pricingResult.monthlySpread())
                .presentValueBrl(pricingResult.presentValueBrl())
                .discountBrl(pricingResult.discountBrl())
                .paymentCurrency(pricingResult.paymentCurrency())
                .paymentAmount(pricingResult.paymentAmount())
                .exchangeRateId(exchangeRateId)
                .exchangeRateBrlPerUnit(pricingResult.exchangeRate())
                .settledAt(settledAt)
                .build());
        return settlementMapper.toResponseDTO(settlement);
    }

    private SettlementResult resolveConcurrentAttempt(
            String idempotencyKey,
            String requestFingerprint,
            RuntimeException exception
    ) {
        Optional<Settlement> concurrentSettlement = settlementRepository.findByIdempotencyKeyWithReceivable(idempotencyKey);
        if (concurrentSettlement.isPresent()) {
            return replay(concurrentSettlement.get(), requestFingerprint);
        }
        if (exception instanceof ConflictException conflictException) {
            throw conflictException;
        }
        throw new ConflictException("Receivable has already been settled");
    }

    private SettlementResult replay(Settlement settlement, String requestFingerprint) {
        if (!settlement.getRequestFingerprint().equals(requestFingerprint)) {
            throw new IdempotencyKeyReuseException("Idempotency key was already used with a different request");
        }
        logger.info("Settlement {} replayed for idempotency key {}", settlement.getId(), settlement.getIdempotencyKey());
        return new SettlementResult(settlementMapper.toResponseDTO(settlement), true);
    }

    private String fingerprint(SettlementRequestDTO settlementRequestDTO) {
        String canonicalRequest = String.join("|",
                String.valueOf(settlementRequestDTO.assignorId()),
                settlementRequestDTO.documentNumber().trim(),
                settlementRequestDTO.receivableType().name(),
                settlementRequestDTO.faceValue().stripTrailingZeros().toPlainString(),
                settlementRequestDTO.dueDate().toString(),
                settlementRequestDTO.paymentCurrency().name()
        );
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
