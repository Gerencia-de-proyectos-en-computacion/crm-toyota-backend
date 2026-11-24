package com.tricore.crmtoyota.domain.entity;

import com.tricore.crmtoyota.domain.enums.DocumentType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, unique = true, length = 15)
    private String documentNumber;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 25)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(length = 220)
    private String address;

    @Column(name = "data_consent_accepted", nullable = false)
    private boolean dataConsentAccepted;

    @Column(name = "data_consent_at", nullable = false)
    private OffsetDateTime dataConsentAt;

    @Column(name = "consent_version", nullable = false, length = 25)
    private String consentVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isDataConsentAccepted() { return dataConsentAccepted; }
    public void setDataConsentAccepted(boolean dataConsentAccepted) { this.dataConsentAccepted = dataConsentAccepted; }
    public OffsetDateTime getDataConsentAt() { return dataConsentAt; }
    public void setDataConsentAt(OffsetDateTime dataConsentAt) { this.dataConsentAt = dataConsentAt; }
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
