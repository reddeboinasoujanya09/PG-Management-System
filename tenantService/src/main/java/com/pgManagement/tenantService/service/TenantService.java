package com.pgManagement.tenantService.service;

import static com.pgManagement.tenantService.dto.TenantType.PERMANENT;

import com.pgManagement.tenantService.ExceptionHandler.DuplicateEmailException;
import com.pgManagement.tenantService.ExceptionHandler.TemparoryDateRequiredException;
import com.pgManagement.tenantService.ExceptionHandler.TenantNotFoundException;
import com.pgManagement.tenantService.dto.Tenant;
import com.pgManagement.tenantService.dto.TenantDTO;
import com.pgManagement.tenantService.dto.TenantStatus;
import com.pgManagement.tenantService.dto.TenantUpdateDTO;
import com.pgManagement.tenantService.repository.TenantRepo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    private final TenantRepo tenantRepo;

    public TenantService(TenantRepo tenantRepo) {
        this.tenantRepo = tenantRepo;
    }

    public Tenant getById(String tenantId) {
        return tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));       }

    public List<Tenant> getByName(String tenantName) {
        if (tenantName == null || tenantName.isBlank()) {
            return List.of();
        }
        String query = tenantName.trim().toLowerCase(Locale.ROOT);
        return tenantRepo.findByTenantNameContainingIgnoreCase(query);
    }

    public Tenant createTenant(TenantDTO tenant) {
        if (tenantRepo.existsByTenantEmail(tenant.getTenantEmail())) {
            throw new DuplicateEmailException("Tenant email already exists");
        }
        Tenant newTenant = new Tenant();
        newTenant.setTenantId("tenant-" + UUID.randomUUID());
        newTenant.setTenantName(tenant.getTenantName());
        newTenant.setTenantEmail(tenant.getTenantEmail());
        newTenant.setTenantPhoneNumber(tenant.getTenantPhoneNumber());
        newTenant.setTenantAddress(tenant.getTenantAddress());
        newTenant.setTenantStatus(TenantStatus.TO_BE_RESERVED);
        newTenant.setTenantType(tenant.getTenantType());
        newTenant.setCreatedAt(Timestamp.from(Instant.now()));
        if(newTenant.getTenantType()==PERMANENT)
        {
            newTenant.setVacateDate(null);
        }
        else{
            if(tenant.getVacateDate()==null)
            {
                throw new TemparoryDateRequiredException("Vacate date is required for non-permanent tenants");
            }
            if (tenant.getVacateDate().before(Timestamp.from(Instant.now()))) {
                throw new TemparoryDateRequiredException("Vacate date must be a future date");
            }
            newTenant.setVacateDate(tenant.getVacateDate());
        }
        //TODO: Tenant room and bed allocation before creating tenant
        return tenantRepo.save(newTenant);
    }

    public void updateTenant(String tenantId, TenantUpdateDTO dto) {
        Tenant tenant = tenantRepo.findById(tenantId).orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));

        tenant.setTenantName(dto.getTenantName());
        tenant.setTenantPhoneNumber(dto.getTenantPhoneNumber());
        tenant.setTenantAddress(dto.getTenantAddress());
        tenant.setTenantType(dto.getTenantType());
        tenant.setTenantStatus(dto.getTenantStatus());
        tenant.setUpdatedAt(Timestamp.from(Instant.now()));
        tenant.setTenantEmail(dto.getTenantEmail());
        tenant.setVacateDate(dto.getVacateDate());
        validateEmail(tenant, dto.getTenantEmail());
        tenantRepo.save(tenant);
    }

    public void deleteTenant(String tenantId) {
        tenantRepo.findById(tenantId).orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));
        tenantRepo.deleteById(tenantId);
    }

    private void validateEmail(Tenant tenant, String tenantEmail) {
        //verify if email is being updated and if the new email already exists in the database and not the same tenants one
        if (tenantRepo.existsByTenantEmail(tenant.getTenantEmail()) && !tenantEmail.equals(tenant.getTenantEmail())) {
                throw new DuplicateEmailException("Tenant email already exists");
            }

    }

}
