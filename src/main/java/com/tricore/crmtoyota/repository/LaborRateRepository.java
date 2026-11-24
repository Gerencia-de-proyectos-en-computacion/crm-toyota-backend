package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.LaborRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LaborRateRepository extends JpaRepository<LaborRate, Long> {
    Optional<LaborRate> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<LaborRate> findByActiveTrueOrderByIdAsc();
}
