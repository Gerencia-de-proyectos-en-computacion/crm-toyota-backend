package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.entity.Customer;
import com.tricore.crmtoyota.domain.entity.Vehicle;
import com.tricore.crmtoyota.domain.enums.DocumentType;
import com.tricore.crmtoyota.dto.CustomerVehicleDtos.*;
import com.tricore.crmtoyota.exception.BusinessRuleException;
import com.tricore.crmtoyota.exception.DuplicateResourceException;
import com.tricore.crmtoyota.exception.ResourceNotFoundException;
import com.tricore.crmtoyota.repository.CustomerRepository;
import com.tricore.crmtoyota.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CustomerVehicleService {

    private static final String CONSENT_VERSION = "LEY-29733-v1.0";
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern RUC_PATTERN = Pattern.compile("^(10|20)\\d{9}$");
    private static final Pattern MTC_PLATE_PATTERN = Pattern.compile("^[A-Z0-9]{3}-\\d{3}$");

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final CatalogInventoryService catalogService;

    public CustomerVehicleService(
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            CatalogInventoryService catalogService
    ) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.catalogService = catalogService;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers(String query) {
        List<Customer> rows = (query == null || query.isBlank())
                ? customerRepository.findAll()
                : customerRepository.search(query.trim());
        return rows.stream().map(this::toCustomerResponse).toList();
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest req) {
        validateConsentAndDocument(req);
        String doc = req.documentNumber().trim();
        if (customerRepository.existsByDocumentNumber(doc)) {
            throw new DuplicateResourceException("Ya existe un cliente registrado con " + req.documentType() + " " + doc);
        }
        Customer c = new Customer();
        c.setDocumentType(req.documentType());
        c.setDocumentNumber(doc);
        c.setFullName(req.fullName().trim());
        c.setPhone(req.phone().trim());
        c.setEmail(req.email() != null && !req.email().isBlank() ? req.email().trim() : null);
        c.setAddress(req.address() != null && !req.address().isBlank() ? req.address().trim() : null);
        c.setDataConsentAccepted(true);
        c.setDataConsentAt(OffsetDateTime.now());
        c.setConsentVersion(CONSENT_VERSION);
        c.setCreatedAt(OffsetDateTime.now());
        return toCustomerResponse(customerRepository.save(c));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest req) {
        validateConsentAndDocument(req);
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        String doc = req.documentNumber().trim();
        if (!c.getDocumentNumber().equals(doc) && customerRepository.existsByDocumentNumber(doc)) {
            throw new DuplicateResourceException("Ya existe otro cliente con documento " + doc);
        }
        c.setDocumentType(req.documentType());
        c.setDocumentNumber(doc);
        c.setFullName(req.fullName().trim());
        c.setPhone(req.phone().trim());
        c.setEmail(req.email() != null && !req.email().isBlank() ? req.email().trim() : null);
        c.setAddress(req.address() != null && !req.address().isBlank() ? req.address().trim() : null);
        return toCustomerResponse(customerRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> searchVehiclesForPos(String query) {
        List<Vehicle> vehicles = (query == null || query.isBlank())
                ? vehicleRepository.findAll()
                : vehicleRepository.quickPosLookup(query.trim());
        return vehicles.stream().map(this::toVehicleResponse).toList();
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest req) {
        Customer owner = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente propietario no encontrado: " + req.customerId()));
        String plate = normalizeAndValidatePlate(req.licensePlate(), req.specialPlate());
        if (vehicleRepository.existsByLicensePlateIgnoreCase(plate)) {
            throw new DuplicateResourceException("Ya existe un vehículo registrado con la placa " + plate);
        }
        Vehicle v = new Vehicle();
        v.setCustomer(owner);
        v.setLicensePlate(plate);
        v.setSpecialPlate(req.specialPlate());
        v.setVin(req.vin() != null && !req.vin().isBlank() ? req.vin().trim().toUpperCase() : null);
        v.setModel(req.model().trim());
        v.setManufactureYear(req.manufactureYear());
        v.setTrimVersion(req.trimVersion().trim());
        v.setCurrentMileageKm(req.currentMileageKm());
        v.setCreatedAt(OffsetDateTime.now());
        return toVehicleResponse(vehicleRepository.save(v));
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleRequest req) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + id));
        Customer owner = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente propietario no encontrado: " + req.customerId()));
        String plate = normalizeAndValidatePlate(req.licensePlate(), req.specialPlate());
        if (!v.getLicensePlate().equalsIgnoreCase(plate) && vehicleRepository.existsByLicensePlateIgnoreCase(plate)) {
            throw new DuplicateResourceException("Ya existe otro vehículo con la placa " + plate);
        }
        if (req.currentMileageKm() < v.getCurrentMileageKm() && !req.allowMileageOverride()) {
            throw new BusinessRuleException(
                    "El nuevo kilometraje (" + req.currentMileageKm() + " km) es menor al último registrado ("
                            + v.getCurrentMileageKm() + " km). Confirme corrección manual si fue error de digitación."
            );
        }
        v.setCustomer(owner);
        v.setLicensePlate(plate);
        v.setSpecialPlate(req.specialPlate());
        v.setVin(req.vin() != null && !req.vin().isBlank() ? req.vin().trim().toUpperCase() : null);
        v.setModel(req.model().trim());
        v.setManufactureYear(req.manufactureYear());
        v.setTrimVersion(req.trimVersion().trim());
        v.setCurrentMileageKm(req.currentMileageKm());
        return toVehicleResponse(vehicleRepository.save(v));
    }

    private void validateConsentAndDocument(CustomerRequest req) {
        if (!Boolean.TRUE.equals(req.dataConsentAccepted())) {
            throw new BusinessRuleException("Es obligatorio aceptar el consentimiento de datos personales (Ley N.° 29733).");
        }
        String doc = req.documentNumber().trim();
        if (req.documentType() == DocumentType.DNI && !DNI_PATTERN.matcher(doc).matches()) {
            throw new BusinessRuleException("El DNI peruano debe contener exactamente 8 dígitos numéricos.");
        }
        if (req.documentType() == DocumentType.RUC && !RUC_PATTERN.matcher(doc).matches()) {
            throw new BusinessRuleException("El RUC peruano debe contener 11 dígitos e iniciar con 10 o 20.");
        }
    }

    private String normalizeAndValidatePlate(String rawPlate, boolean specialPlate) {
        String plate = rawPlate.trim().toUpperCase();
        if (!specialPlate && !MTC_PLATE_PATTERN.matcher(plate).matches()) {
            throw new BusinessRuleException("Formato de placa MTC inválido (use ABC-123 o active 'Placa especial/en trámite').");
        }
        return plate;
    }

    private CustomerResponse toCustomerResponse(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getDocumentType(),
                c.getDocumentNumber(),
                c.getFullName(),
                c.getPhone(),
                c.getEmail(),
                c.getAddress(),
                c.isDataConsentAccepted(),
                c.getDataConsentAt(),
                c.getConsentVersion()
        );
    }

    private VehicleResponse toVehicleResponse(Vehicle v) {
        Customer c = v.getCustomer();
        var suggestedKit = catalogService.suggestKitForVehicle(v.getModel(), v.getCurrentMileageKm()).orElse(null);
        return new VehicleResponse(
                v.getId(),
                c.getId(),
                c.getFullName(),
                c.getDocumentType().name(),
                c.getDocumentNumber(),
                c.getPhone(),
                v.getLicensePlate(),
                v.isSpecialPlate(),
                v.getVin(),
                v.getModel(),
                v.getManufactureYear(),
                v.getTrimVersion(),
                v.getCurrentMileageKm(),
                suggestedKit
        );
    }
}
