package com.srm.creditengine.core.settlement.repository;

import com.srm.creditengine.core.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("""
            select settlement from Settlement settlement
            join fetch settlement.receivable receivable
            join fetch receivable.assignor
            where settlement.idempotencyKey = :idempotencyKey
            """)
    Optional<Settlement> findByIdempotencyKeyWithReceivable(@Param("idempotencyKey") String idempotencyKey);
}
