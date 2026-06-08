package com.pgManagement.tenantService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class TenantDTO {

    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Tenant email is required")
    private String tenantEmail;

    @NotBlank(message = "Tenant phone number is required")
    private String tenantPhoneNumber;

    private String tenantAddress;

    @NotNull(message = "Tenant type is required")
    private TenantType tenantType;

    private Timestamp vacateDate;
}
