package com.pgManagement.tenantService.controller;

import com.pgManagement.tenantService.dto.TenantDTO;
import com.pgManagement.tenantService.dto.TenantUpdateDTO;
import com.pgManagement.tenantService.dto.TenantResponseDTO;
import com.pgManagement.tenantService.service.TenantService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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

    @PostMapping
    public ResponseEntity<TenantResponseDTO> createTenant(@Valid @RequestBody TenantDTO tenant) {
        TenantResponseDTO created = tenantService.createTenant(tenant);
        URI location = URI.create("/api/v1/tenants/" + created.getTenantId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponseDTO> getById(@PathVariable String tenantId) {
        return ResponseEntity.ok(tenantService.getById(tenantId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TenantResponseDTO>> search(@RequestParam String name) {

        return ResponseEntity.ok(tenantService.getByName(name));
    }

    @GetMapping()
    public ResponseEntity<Page<TenantResponseDTO>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

            Pageable pageable = PageRequest.of(page, size, Sort.by("tenantId").descending());
            return ResponseEntity.ok(tenantService.getAll(pageable));
        }

    @PutMapping("/{tenantId}")
    public ResponseEntity<Void> updateById(
            @PathVariable String tenantId,
            @Valid @RequestBody TenantUpdateDTO tenantDTO) {
        tenantService.updateTenant(tenantId, tenantDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deleteById(@PathVariable String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
        throw new IllegalArgumentException("Tenant ID cannot be null or blank");
    }

        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
