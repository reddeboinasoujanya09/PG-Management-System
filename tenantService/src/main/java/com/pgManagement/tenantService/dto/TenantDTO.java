package com.pgManagement.tenantService.dto;

import com.pgManagement.tenantService.entity.TenantType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class TenantDTO {

    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Tenant email is required")
    private String tenantEmail;

    // ^ -> start of regex,
    // [6-9] first digit must be between 6 and 9,
    // \d{9} means the next 9 digits can be any digit,
    // $ end of regex
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number format")
    @NotBlank(message = "Tenant phone number is required")
    private String tenantPhoneNumber;

    private String tenantAddress;

    @NotNull(message = "Tenant type is required")
    private TenantType tenantType;

    private Timestamp vacateDate;
}
