package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.MaintenanceKit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceKitRepository extends JpaRepository<MaintenanceKit, Long> {
    List<MaintenanceKit> findByModelIgnoreCaseAndActiveTrueOrderByIntervalKmAsc(String model);
    List<MaintenanceKit> findByActiveTrueOrderByModelAscIntervalKmAsc();
}
