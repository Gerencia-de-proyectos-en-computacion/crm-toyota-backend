package com.tricore.crmtoyota.domain.entity;

import com.tricore.crmtoyota.domain.enums.PartBrand;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parts")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(name = "oem_reference_code", nullable = false, length = 40)
    private String oemReferenceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PartBrand brand;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(name = "compatible_summary", nullable = false, length = 140)
    private String compatibleSummary;

    @Column(name = "sale_price_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal salePriceIgv;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PartCompatibility> compatibilities = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getOemReferenceCode() { return oemReferenceCode; }
    public void setOemReferenceCode(String oemReferenceCode) { this.oemReferenceCode = oemReferenceCode; }
    public PartBrand getBrand() { return brand; }
    public void setBrand(PartBrand brand) { this.brand = brand; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCompatibleSummary() { return compatibleSummary; }
    public void setCompatibleSummary(String compatibleSummary) { this.compatibleSummary = compatibleSummary; }
    public BigDecimal getSalePriceIgv() { return salePriceIgv; }
    public void setSalePriceIgv(BigDecimal salePriceIgv) { this.salePriceIgv = salePriceIgv; }
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<PartCompatibility> getCompatibilities() { return compatibilities; }
    public void setCompatibilities(List<PartCompatibility> compatibilities) { this.compatibilities = compatibilities; }
}
