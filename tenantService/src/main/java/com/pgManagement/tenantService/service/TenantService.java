package com.pgManagement.tenantService.service;

import static com.pgManagement.tenantService.dto.TenantType.PERMANENT;

import com.pgManagement.tenantService.dto.TenantDTO;
import com.pgManagement.tenantService.dto.TenantStatus;
import com.pgManagement.tenantService.dto.TenantUpdateDTO;
import com.pgManagement.tenantService.entity.Tenant;
import com.pgManagement.tenantService.exception.DuplicateEmailException;
import com.pgManagement.tenantService.exception.TemporaryDateRequiredException;
import com.pgManagement.tenantService.exception.TenantNotFoundException;
import com.pgManagement.tenantService.repository.TenantRepo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepo tenantRepo;

    public TenantService(TenantRepo tenantRepo) {
        this.tenantRepo = tenantRepo;
    }

    @Transactional(readOnly = true)
    public Tenant getById(String tenantId) {
        return tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));
    }

    @Transactional(readOnly = true)
    public List<Tenant> getByName(String tenantName) {
        if (tenantName == null || tenantName.isBlank()) {
            return List.of();
        }
        return tenantRepo.findByTenantNameContainingIgnoreCase(tenantName.trim());
    }

    @Transactional
    public Tenant createTenant(TenantDTO dto) {
        if (tenantRepo.existsByTenantEmail(dto.getTenantEmail())) {
            throw new DuplicateEmailException("Tenant with email already exists: " + dto.getTenantEmail());
        }

        Tenant tenant = new Tenant();
        tenant.setTenantId("tenant-" + UUID.randomUUID());
        tenant.setTenantName(dto.getTenantName());
        tenant.setTenantEmail(dto.getTenantEmail());
        tenant.setTenantPhoneNumber(dto.getTenantPhoneNumber());
        tenant.setTenantAddress(dto.getTenantAddress());
        tenant.setTenantStatus(TenantStatus.TO_BE_RESERVED);
        tenant.setTenantType(dto.getTenantType());
        tenant.setCreatedAt(Timestamp.from(Instant.now()));

        if (dto.getTenantType() == PERMANENT) {
            tenant.setVacateDate(null);
        } else {
            validateVacateDate(dto.getVacateDate());
            tenant.setVacateDate(dto.getVacateDate());
        }

        // TODO: integrate with room-service to allocate room/bed before confirming tenant
        return tenantRepo.save(tenant);
    }

    @Transactional
    public void updateTenant(String tenantId, TenantUpdateDTO dto) {
        Tenant tenant = tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));

        if (!dto.getTenantEmail().equalsIgnoreCase(tenant.getTenantEmail())
                && tenantRepo.existsByTenantEmailAndTenantIdNot(dto.getTenantEmail(), tenantId)) {
            throw new DuplicateEmailException("Tenant with email already exists: " + dto.getTenantEmail());
        }

        tenant.setTenantName(dto.getTenantName());
        tenant.setTenantEmail(dto.getTenantEmail());
        tenant.setTenantPhoneNumber(dto.getTenantPhoneNumber());
        tenant.setTenantAddress(dto.getTenantAddress());
        tenant.setTenantStatus(dto.getTenantStatus());
        tenant.setTenantType(dto.getTenantType());
        tenant.setUpdatedAt(Timestamp.from(Instant.now()));

        if (dto.getTenantType() == PERMANENT) {
            tenant.setVacateDate(null);
        } else {
            validateVacateDate(dto.getVacateDate());
            tenant.setVacateDate(dto.getVacateDate());
        }

        tenantRepo.save(tenant);
    }

    @Transactional
    public void deleteTenant(String tenantId) {
        if(tenantId == null || tenantId.isBlank()) {
            throw new TenantNotFoundException("Please provide the tenant_id (tenant_id is empty) ");
        }
        if (!tenantRepo.existsById(tenantId)) {
            throw new TenantNotFoundException("Tenant not found with id: " + tenantId);
        }
        tenantRepo.deleteById(tenantId);
    }

    private void validateVacateDate(Timestamp vacateDate) {
        if (vacateDate == null) {
            throw new TemporaryDateRequiredException("Vacate date is required for temporary tenants");
        }
        if (vacateDate.before(Timestamp.from(Instant.now()))) {
            throw new TemporaryDateRequiredException("Vacate date must be a future date");
        }
    }
}
