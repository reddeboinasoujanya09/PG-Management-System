package com.pgManagement.tenantService.repository;

import com.pgManagement.tenantService.dto.Tenant;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepo extends CrudRepository<Tenant, String> {
    boolean existsByTenantEmail(String tenantEmail);
    List<Tenant> findByTenantNameContainingIgnoreCase(String tenantName);
}
