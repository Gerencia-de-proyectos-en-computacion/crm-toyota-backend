package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "labor_rates")
public class LaborRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "service_name", nullable = false, length = 160)
    private String serviceName;

    @Column(name = "estimated_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "interval_km")
    private Integer intervalKm;

    @Column(name = "price_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceIgv;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public BigDecimal getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(BigDecimal estimatedHours) { this.estimatedHours = estimatedHours; }
    public Integer getIntervalKm() { return intervalKm; }
    public void setIntervalKm(Integer intervalKm) { this.intervalKm = intervalKm; }
    public BigDecimal getPriceIgv() { return priceIgv; }
    public void setPriceIgv(BigDecimal priceIgv) { this.priceIgv = priceIgv; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
