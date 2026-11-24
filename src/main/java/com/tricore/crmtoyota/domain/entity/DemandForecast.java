package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "demand_forecasts")
public class DemandForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "part_id", nullable = false, unique = true)
    private Part part;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private MlModelRun run;

    @Column(name = "predicted_demand", nullable = false, precision = 10, scale = 2)
    private BigDecimal predictedDemand;

    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;

    @Column(name = "suggested_order_qty", nullable = false)
    private Integer suggestedOrderQty;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal adi;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }
    public MlModelRun getRun() { return run; }
    public void setRun(MlModelRun run) { this.run = run; }
    public BigDecimal getPredictedDemand() { return predictedDemand; }
    public void setPredictedDemand(BigDecimal predictedDemand) { this.predictedDemand = predictedDemand; }
    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    public Integer getSuggestedOrderQty() { return suggestedOrderQty; }
    public void setSuggestedOrderQty(Integer suggestedOrderQty) { this.suggestedOrderQty = suggestedOrderQty; }
    public BigDecimal getAdi() { return adi; }
    public void setAdi(BigDecimal adi) { this.adi = adi; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
