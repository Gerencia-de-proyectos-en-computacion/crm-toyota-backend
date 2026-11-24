package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.DemandForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DemandForecastRepository extends JpaRepository<DemandForecast, Long> {
    Optional<DemandForecast> findByPartId(Long partId);
}
