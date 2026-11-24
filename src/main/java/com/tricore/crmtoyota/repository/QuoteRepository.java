package com.tricore.crmtoyota.repository;

import com.tricore.crmtoyota.domain.entity.Quote;
import com.tricore.crmtoyota.domain.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Optional<Quote> findByQuoteNumberIgnoreCase(String quoteNumber);
    List<Quote> findAllByOrderByIdDesc();
    List<Quote> findByStatusOrderByIdDesc(QuoteStatus status);

    @Query(value = "SELECT nextval('quote_number_seq')", nativeQuery = true)
    Long nextQuoteSequence();
}
