package com.pgManagement.tenantService.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class TenantResponseDTO {
    private String tenantId;

    private String tenantName;

    private String tenantEmail;

    private TenantType tenantType;

    private Timestamp vacateDate;
}
