package com.pgManagement.tenantService.entity;

import com.pgManagement.tenantService.dto.TenantStatus;
import com.pgManagement.tenantService.dto.TenantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
public class Tenant {

    @Id
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    @Column(name = "tenant_email", unique = true, nullable = false)
    private String tenantEmail;

    @Column(name = "tenant_phone_number")
    private String tenantPhoneNumber;

    @Column(name = "tenant_address")
    private String tenantAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_status", nullable = false)
    private TenantStatus tenantStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", nullable = false)
    private TenantType tenantType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "vacate_date")
    private Timestamp vacateDate;
}
