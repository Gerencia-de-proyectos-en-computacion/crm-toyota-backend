package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "maintenance_kit_items")
public class MaintenanceKitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kit_id", nullable = false)
    private MaintenanceKit kit;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false)
    private Integer quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MaintenanceKit getKit() { return kit; }
    public void setKit(MaintenanceKit kit) { this.kit = kit; }
    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
