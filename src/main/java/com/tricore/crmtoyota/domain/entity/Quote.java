package com.tricore.crmtoyota.domain.entity;

import com.tricore.crmtoyota.domain.enums.QuoteStatus;
import com.tricore.crmtoyota.domain.enums.RejectionReason;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_number", nullable = false, unique = true, length = 25)
    private String quoteNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "mileage_at_quote", nullable = false)
    private Integer mileageAtQuote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 30)
    private RejectionReason rejectionReason;

    @Column(name = "rejection_notes", length = 300)
    private String rejectionNotes;

    @Column(name = "gross_total_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossTotalIgv;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "taxable_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxableSubtotal;

    @Column(name = "igv_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal igvAmount;

    @Column(name = "final_total_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalTotalIgv;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(length = 300)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuoteItem> items = new ArrayList<>();

    public void replaceItems(List<QuoteItem> newItems) {
        this.items.clear();
        for (QuoteItem item : newItems) {
            item.setQuote(this);
            this.items.add(item);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Integer getMileageAtQuote() { return mileageAtQuote; }
    public void setMileageAtQuote(Integer mileageAtQuote) { this.mileageAtQuote = mileageAtQuote; }
    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }
    public RejectionReason getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(RejectionReason rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getRejectionNotes() { return rejectionNotes; }
    public void setRejectionNotes(String rejectionNotes) { this.rejectionNotes = rejectionNotes; }
    public BigDecimal getGrossTotalIgv() { return grossTotalIgv; }
    public void setGrossTotalIgv(BigDecimal grossTotalIgv) { this.grossTotalIgv = grossTotalIgv; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxableSubtotal() { return taxableSubtotal; }
    public void setTaxableSubtotal(BigDecimal taxableSubtotal) { this.taxableSubtotal = taxableSubtotal; }
    public BigDecimal getIgvAmount() { return igvAmount; }
    public void setIgvAmount(BigDecimal igvAmount) { this.igvAmount = igvAmount; }
    public BigDecimal getFinalTotalIgv() { return finalTotalIgv; }
    public void setFinalTotalIgv(BigDecimal finalTotalIgv) { this.finalTotalIgv = finalTotalIgv; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(OffsetDateTime closedAt) { this.closedAt = closedAt; }
    public List<QuoteItem> getItems() { return items; }
    public void setItems(List<QuoteItem> items) { this.items = items; }
}
