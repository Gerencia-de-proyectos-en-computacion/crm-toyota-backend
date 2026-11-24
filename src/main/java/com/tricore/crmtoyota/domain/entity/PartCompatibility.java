package com.tricore.crmtoyota.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "part_compatibilities")
public class PartCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "year_from", nullable = false)
    private Integer yearFrom;

    @Column(name = "year_to", nullable = false)
    private Integer yearTo;

    @Column(name = "trim_version", nullable = false, length = 60)
    private String trimVersion = "ALL";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Part getPart() { return part; }
    public void setPart(Part part) { this.part = part; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYearFrom() { return yearFrom; }
    public void setYearFrom(Integer yearFrom) { this.yearFrom = yearFrom; }
    public Integer getYearTo() { return yearTo; }
    public void setYearTo(Integer yearTo) { this.yearTo = yearTo; }
    public String getTrimVersion() { return trimVersion; }
    public void setTrimVersion(String trimVersion) { this.trimVersion = trimVersion; }
}
