package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findTop50ByOrderByCreatedAtDesc();
}
