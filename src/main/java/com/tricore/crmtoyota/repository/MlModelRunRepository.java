package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.MlModelRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MlModelRunRepository extends JpaRepository<MlModelRun, Long> {
    Optional<MlModelRun> findTopByOrderByCalculatedAtDesc();
}
