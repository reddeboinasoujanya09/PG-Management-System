package com.pgManagement.tenantService.controller;

import com.pgManagement.tenantService.dto.TenantDTO;
import com.pgManagement.tenantService.dto.TenantUpdateDTO;
import com.pgManagement.tenantService.dto.TenantResponseDTO;
import com.pgManagement.tenantService.service.TenantService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    private Logger logger = LoggerFactory.getLogger(TenantController.class);

    @PostMapping
    public ResponseEntity<TenantResponseDTO> createTenant(@Valid @RequestBody TenantDTO tenant) {
        logger.info("Received request to create tenant: {}", tenant);
        TenantResponseDTO created = tenantService.createTenant(tenant);
        URI location = URI.create("/api/v1/tenants/" + created.getTenantId());
        logger.info("Received response for create tenant: {}", created);
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponseDTO> getById(@PathVariable String tenantId) {
        logger.info("Received request to get Tenant by ID: {}", tenantId);
        return ResponseEntity.ok(tenantService.getById(tenantId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TenantResponseDTO>> search(@RequestParam String name) {
        logger.info("Received request to search for Tenant by name: {}", name);
        return ResponseEntity.ok(tenantService.getByName(name));
    }

    @GetMapping()
    public ResponseEntity<Page<TenantResponseDTO>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
            logger.info("Received request to get all Tenant in page: {}", page);
            Pageable pageable = PageRequest.of(page, size, Sort.by("tenantId").descending());
            logger.info("Received request to get all Tenant in page: {}", pageable);
            return ResponseEntity.ok(tenantService.getAll(pageable));
        }

    @PutMapping("/{tenantId}")
    public ResponseEntity<Void> updateById(
            @PathVariable String tenantId,
            @Valid @RequestBody TenantUpdateDTO tenantDTO) {
        logger.info("Received request to update Tenant by ID: {}", tenantId);
        tenantService.updateTenant(tenantId, tenantDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deleteById(@PathVariable String tenantId) {
        logger.info("Received request to delete Tenant by ID: {}", tenantId);
        if (tenantId == null || tenantId.isBlank()) {
        throw new IllegalArgumentException("Tenant ID cannot be null or blank");
    }

        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
