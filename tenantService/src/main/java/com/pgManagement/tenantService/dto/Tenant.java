package com.pgManagement.tenantService.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import lombok.Data;


@Entity
@Table(name = "tenants")
@Data
public class Tenant {
    @Id
    private String tenantId;

    private String tenantName;

    @Column(unique = true, nullable = false)
    private String tenantEmail;

    private String tenantPhoneNumber;
    private String tenantAddress;
    private TenantStatus tenantStatus;
    private TenantType tenantType;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp vacateDate;

public Tenant() {
    /* TODO */
}

    }
