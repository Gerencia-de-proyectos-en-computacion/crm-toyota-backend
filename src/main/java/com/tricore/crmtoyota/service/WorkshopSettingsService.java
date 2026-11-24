package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.entity.WorkshopSettings;
import com.tricore.crmtoyota.dto.WorkshopSettingsDtos.WorkshopSettingsResponse;
import com.tricore.crmtoyota.dto.WorkshopSettingsDtos.WorkshopSettingsUpdateRequest;
import com.tricore.crmtoyota.repository.WorkshopSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class WorkshopSettingsService {

    private static final Long SINGLETON_ID = 1L;
    private final WorkshopSettingsRepository repository;

    public WorkshopSettingsService(WorkshopSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WorkshopSettings getEntity() {
        return repository.findById(SINGLETON_ID).orElseGet(() -> {
            WorkshopSettings fallback = new WorkshopSettings();
            fallback.setId(SINGLETON_ID);
            fallback.setBusinessName("[Nombre del Taller Automotriz]");
            fallback.setSlogan("Servicio Técnico y Repuestos Especializados Toyota");
            fallback.setRuc("20000000001");
            fallback.setAddress("Av. Principal 100, Lima, Perú");
            fallback.setPhone("+51 999 888 777");
            fallback.setEmail("mostrador@tallertecnicotoyota.pe");
            fallback.setCurrencySymbol("S/");
            fallback.setQuoteValidityDays(7);
            fallback.setUpdatedAt(OffsetDateTime.now());
            return fallback;
        });
    }

    @Transactional(readOnly = true)
    public WorkshopSettingsResponse getSettings() {
        return toResponse(getEntity());
    }

    @Transactional
    public WorkshopSettingsResponse updateSettings(WorkshopSettingsUpdateRequest req) {
        WorkshopSettings entity = repository.findById(SINGLETON_ID).orElseGet(() -> {
            WorkshopSettings ws = new WorkshopSettings();
            ws.setId(SINGLETON_ID);
            ws.setCurrencySymbol("S/");
            return ws;
        });
        entity.setBusinessName(req.businessName().trim());
        entity.setSlogan(req.slogan().trim());
        entity.setRuc(req.ruc().trim());
        entity.setAddress(req.address().trim());
        entity.setPhone(req.phone().trim());
        entity.setEmail(req.email() != null ? req.email().trim() : null);
        entity.setQuoteValidityDays(req.quoteValidityDays() != null ? req.quoteValidityDays() : 7);
        entity.setUpdatedAt(OffsetDateTime.now());
        return toResponse(repository.save(entity));
    }

    private WorkshopSettingsResponse toResponse(WorkshopSettings s) {
        return new WorkshopSettingsResponse(
                s.getBusinessName(),
                s.getSlogan(),
                s.getRuc(),
                s.getAddress(),
                s.getPhone(),
                s.getEmail(),
                s.getCurrencySymbol(),
                s.getQuoteValidityDays(),
                s.getUpdatedAt()
        );
    }
}
