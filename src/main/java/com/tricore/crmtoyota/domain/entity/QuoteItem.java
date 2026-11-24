package com.tricore.crmtoyota.domain.entity;

import com.tricore.crmtoyota.domain.enums.QuoteItemType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "quote_items")
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 15)
    private QuoteItemType itemType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "part_id")
    private Part part;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "labor_rate_id")
    private LaborRate laborRate;

    @Column(name = "item_code", nullable = false, length = 40)
    private String itemCode;

    @Column(nullable = false, length = 180)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceIgv;

    @Column(name = "line_total_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotalIgv;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Quote getQuote() { return quote; }
    public void setQuote(Quote quote) { this.quote = quote; }
    public QuoteItemType getItemType() { return itemType; }
    public void setItemType(QuoteItemType itemType) { this.itemType = itemType; }
    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }
    public LaborRate getLaborRate() { return laborRate; }
    public void setLaborRate(LaborRate laborRate) { this.laborRate = laborRate; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPriceIgv() { return unitPriceIgv; }
    public void setUnitPriceIgv(BigDecimal unitPriceIgv) { this.unitPriceIgv = unitPriceIgv; }
    public BigDecimal getLineTotalIgv() { return lineTotalIgv; }
    public void setLineTotalIgv(BigDecimal lineTotalIgv) { this.lineTotalIgv = lineTotalIgv; }
}
