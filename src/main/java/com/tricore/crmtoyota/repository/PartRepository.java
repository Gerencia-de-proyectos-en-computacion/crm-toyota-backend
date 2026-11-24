package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    Optional<Part> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
    List<Part> findByActiveTrueOrderByCategoryAscNameAsc();
    List<Part> findByOemReferenceCodeIgnoreCaseAndIdNotAndActiveTrueAndCurrentStockGreaterThanOrderBySalePriceIgvAsc(
            String oemReferenceCode, Long excludedPartId, int minStockExclusive);
}
