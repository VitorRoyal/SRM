package com.srm.creditengine.core.assignor.repository;

import com.srm.creditengine.core.assignor.entity.Assignor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignorRepository extends JpaRepository<Assignor, Long> {

    List<Assignor> findAllByOrderByNameAsc();
}
