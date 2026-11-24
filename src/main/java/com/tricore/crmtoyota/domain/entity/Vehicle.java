package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "license_plate", nullable = false, unique = true, length = 15)
    private String licensePlate;

    @Column(name = "special_plate", nullable = false)
    private boolean specialPlate;

    @Column(length = 30)
    private String vin;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "trim_version", nullable = false, length = 60)
    private String trimVersion;

    @Column(name = "current_mileage_km", nullable = false)
    private Integer currentMileageKm;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public boolean isSpecialPlate() { return specialPlate; }
    public void setSpecialPlate(boolean specialPlate) { this.specialPlate = specialPlate; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getManufactureYear() { return manufactureYear; }
    public void setManufactureYear(Integer manufactureYear) { this.manufactureYear = manufactureYear; }
    public String getTrimVersion() { return trimVersion; }
    public void setTrimVersion(String trimVersion) { this.trimVersion = trimVersion; }
    public Integer getCurrentMileageKm() { return currentMileageKm; }
    public void setCurrentMileageKm(Integer currentMileageKm) { this.currentMileageKm = currentMileageKm; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
