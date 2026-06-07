package com.pgManagement.tenantService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class TenantUpdateDTO {
    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Tenant email is required")
    private String tenantEmail;
    private String tenantPhoneNumber;
    private String tenantAddress;
    private TenantType tenantType;
    private Timestamp vacateDate;
    private TenantStatus tenantStatus;
}
