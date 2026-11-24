package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ml_model_runs")
public class MlModelRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_version", nullable = false, length = 60)
    private String modelVersion;

    @Column(name = "horizon_label", nullable = false, length = 60)
    private String horizonLabel;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal r2;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal wape;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal rmse;

    @Column(name = "mape_nonzero", nullable = false, precision = 6, scale = 4)
    private BigDecimal mapeNonzero;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal adi;

    @Column(name = "execution_source", nullable = false, length = 30)
    private String executionSource;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getHorizonLabel() { return horizonLabel; }
    public void setHorizonLabel(String horizonLabel) { this.horizonLabel = horizonLabel; }
    public BigDecimal getR2() { return r2; }
    public void setR2(BigDecimal r2) { this.r2 = r2; }
    public BigDecimal getWape() { return wape; }
    public void setWape(BigDecimal wape) { this.wape = wape; }
    public BigDecimal getRmse() { return rmse; }
    public void setRmse(BigDecimal rmse) { this.rmse = rmse; }
    public BigDecimal getMapeNonzero() { return mapeNonzero; }
    public void setMapeNonzero(BigDecimal mapeNonzero) { this.mapeNonzero = mapeNonzero; }
    public BigDecimal getAdi() { return adi; }
    public void setAdi(BigDecimal adi) { this.adi = adi; }
    public String getExecutionSource() { return executionSource; }
    public void setExecutionSource(String executionSource) { this.executionSource = executionSource; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
