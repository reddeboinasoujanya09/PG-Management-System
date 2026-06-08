package com.pgManagement.tenantService.repository;

import com.pgManagement.tenantService.entity.Tenant;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepo extends JpaRepository<Tenant, String> {
    boolean existsByTenantEmail(String tenantEmail);
    boolean existsByTenantEmailAndTenantIdNot(String tenantEmail, String tenantId);
    List<Tenant> findByTenantNameContainingIgnoreCase(String tenantName);
    boolean existsById(String tenantId);
}
