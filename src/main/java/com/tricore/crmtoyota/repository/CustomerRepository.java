package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY c.id DESC")
    List<Customer> search(@Param("q") String query);
}
