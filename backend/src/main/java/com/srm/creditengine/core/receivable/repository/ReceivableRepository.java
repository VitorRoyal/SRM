package com.srm.creditengine.core.receivable.repository;

import com.srm.creditengine.core.receivable.entity.Receivable;
import com.srm.creditengine.core.receivable.receivableEnum.ReceivableType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableRepository extends JpaRepository<Receivable, Long> {

    boolean existsByAssignorIdAndTypeAndDocumentNumber(Long assignorId, ReceivableType type, String documentNumber);
}
