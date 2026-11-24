package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlateIgnoreCase(String licensePlate);
    boolean existsByLicensePlateIgnoreCase(String licensePlate);
    List<Vehicle> findByCustomerIdOrderByIdDesc(Long customerId);

    @Query("SELECT v FROM Vehicle v JOIN FETCH v.customer c WHERE LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY v.id DESC")
    List<Vehicle> quickPosLookup(@Param("q") String query);
}
