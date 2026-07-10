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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "vacate_requests")
public class VacateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    // links to the specific active assignment this request is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private RoomAssignment assignment;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDate requestedVacateDate;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacateRequestStatus status;     // CREATED → UNDER_REVIEW → TO_BE_VACATED / DENIED / CANCELLED

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
