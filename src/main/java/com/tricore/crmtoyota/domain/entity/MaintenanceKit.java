package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maintenance_kits")
public class MaintenanceKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "interval_km", nullable = false)
    private Integer intervalKm;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "labor_rate_id", nullable = false)
    private LaborRate laborRate;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MaintenanceKitItem> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getIntervalKm() { return intervalKm; }
    public void setIntervalKm(Integer intervalKm) { this.intervalKm = intervalKm; }
    public LaborRate getLaborRate() { return laborRate; }
    public void setLaborRate(LaborRate laborRate) { this.laborRate = laborRate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<MaintenanceKitItem> getItems() { return items; }
    public void setItems(List<MaintenanceKitItem> items) { this.items = items; }
}
