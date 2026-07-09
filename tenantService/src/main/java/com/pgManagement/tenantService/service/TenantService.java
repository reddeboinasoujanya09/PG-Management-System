package com.pgManagement.tenantService.service;

import static com.pgManagement.tenantService.entity.TenantType.PERMANENT;

import com.pgManagement.tenantService.dto.TenantDTO;
import com.pgManagement.tenantService.dto.TenantResponseDTO;
import com.pgManagement.tenantService.entity.TenantStatus;
import com.pgManagement.tenantService.dto.TenantUpdateDTO;
import com.pgManagement.tenantService.entity.Tenant;
import com.pgManagement.tenantService.exception.DuplicateEmailException;
import com.pgManagement.tenantService.exception.TemporaryDateRequiredException;
import com.pgManagement.tenantService.exception.TenantNotFoundException;
import com.pgManagement.tenantService.exception.TenantUpsertFailureException;
import com.pgManagement.tenantService.repository.TenantRepo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepo tenantRepo;

    public TenantService(TenantRepo tenantRepo) {
        this.tenantRepo = tenantRepo;
    }

    Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Transactional(readOnly = true)
    public TenantResponseDTO getById(String tenantId) {
        logger.info("getById with id={}", tenantId);
        Tenant tenant= tenantRepo.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with id: " + tenantId));
        TenantResponseDTO tenantResponseDTO = new TenantResponseDTO();
        tenantResponseDTO.setTenantId(tenant.getTenantId());
        tenantResponseDTO.setTenantName(tenant.getTenantName());
        tenantResponseDTO.setTenantType(tenant.getTenantType());
        tenantResponseDTO.setTenantEmail(tenant.getTenantEmail());
        tenantResponseDTO.setVacateDate(tenant.getVacateDate());
        return tenantResponseDTO;
    }

    @Transactional(readOnly = true)
    public List<TenantResponseDTO> getByName(String tenantName) {
        logger.info("getByName with name={}", tenantName);
        if (tenantName == null || tenantName.isBlank()) {
            return List.of();
        }
        List<Tenant> tenant= tenantRepo.findByTenantNameContainingIgnoreCase(tenantName.trim());
        List<TenantResponseDTO> tenantResponseDTOList = new ArrayList<>();
        for(Tenant t:tenant) {
            TenantResponseDTO tenantResponseDTO = new TenantResponseDTO();
            tenantResponseDTO.setTenantId(t.getTenantId());
            tenantResponseDTO.setTenantName(t.getTenantName());
            tenantResponseDTO.setTenantType(t.getTenantType());
            tenantResponseDTO.setTenantEmail(t.getTenantEmail());
            tenantResponseDTO.setVacateDate(t.getVacateDate());
            tenantResponseDTOList.add(tenantResponseDTO);
        }
        return tenantResponseDTOList;
    }

    @Transactional
    public TenantResponseDTO createTenant(TenantDTO dto) {
        logger.info("createTenant with dto={}", dto);
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


        if (dto.getTenantType() == PERMANENT) {
            tenant.setVacateDate(null);
        } else {
            validateVacateDate(dto.getVacateDate());
            tenant.setVacateDate(dto.getVacateDate());
        }

        // TODO: integrate with room-service to allocate room/bed before confirming tenant
        try {
            tenantRepo.save(tenant);
            TenantResponseDTO responseDTO = new TenantResponseDTO();
            responseDTO.setTenantId(tenant.getTenantId());
            responseDTO.setTenantName(tenant.getTenantName());
            responseDTO.setTenantEmail(tenant.getTenantEmail());
            responseDTO.setTenantType(tenant.getTenantType());
            responseDTO.setVacateDate(tenant.getVacateDate());
            return responseDTO;
        }
        catch (Exception e) {
            throw new TenantUpsertFailureException("Tenant creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void updateTenant(String tenantId, TenantUpdateDTO dto) {
        logger.info("updateTenant with dto={}", dto);
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

        if (dto.getTenantType() == PERMANENT) {
            tenant.setVacateDate(null);
        } else {
            validateVacateDate(dto.getVacateDate());
            tenant.setVacateDate(dto.getVacateDate());
        }
    }

    @Transactional
    public void deleteTenant(String tenantId) {
        logger.info("deleteTenant with tenantId={}", tenantId);
        if (!tenantRepo.existsById(tenantId)) {
            throw new TenantNotFoundException("Tenant not found with id: " + tenantId);
        }
        tenantRepo.deleteById(tenantId);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponseDTO> getAll(Pageable pageable) {
        logger.info("getAll with pageable={}", pageable);
        return tenantRepo.findAll(pageable)
                .map(this::convertToResponseDTO);
    }
    private void validateVacateDate(Timestamp vacateDate) {
        logger.info("validateVacateDate with vacateDate={}", vacateDate);
        if (vacateDate == null) {
            throw new TemporaryDateRequiredException("Vacate date is required for temporary tenants");
        }
        if (vacateDate.before(Timestamp.from(Instant.now()))) {
            throw new TemporaryDateRequiredException("Vacate date must be a future date");
        }
    }

    private TenantResponseDTO convertToResponseDTO(Tenant tenant) {
        logger.info("convertToResponseDTO with tenant={}", tenant);
        TenantResponseDTO dto = new TenantResponseDTO();
        dto.setTenantId(tenant.getTenantId());
        dto.setTenantName(tenant.getTenantName());
        dto.setTenantEmail(tenant.getTenantEmail());
        dto.setTenantType(tenant.getTenantType());
        dto.setVacateDate(tenant.getVacateDate());
        return dto;
    }
}
