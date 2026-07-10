package com.pgManagement.roomservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

// Status of this assignment record
// PENDING_VERIFICATION → ACTIVE → VACATED

@Data
@Entity
@Table(name = "room_assignments")
public class RoomAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    private Bed bed;

    @Column(nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantType tenantType;          // PERMANENT or TEMPORARY

    @Column(nullable = false)
    private LocalDate joiningDate;

    // Permanent: set to 10 years from joiningDate. Temporary: tenant-specified.
    @Column(nullable = false)
    private LocalDate vacatingDate;

    private BigDecimal advancePaid;         // advance collected at join time

    @Column(nullable = false)
    private boolean isVerified = false;     // true once OTP/mail verification done

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING_VERIFICATION;

    private String assignedBy;

    private String notes;
}
