package com.pgManagement.roomservice.controller;

import com.pgManagement.roomservice.dto.bed.AssignBedRequest;
import com.pgManagement.roomservice.dto.bed.AssignmentResponse;
import com.pgManagement.roomservice.dto.bed.BedDetailsResponse;
import com.pgManagement.roomservice.dto.bed.BedStatusUpdateRequest;
import com.pgManagement.roomservice.dto.bed.ExtendAssignmentRequest;
import com.pgManagement.roomservice.dto.bed.VacateActionRequest;
import com.pgManagement.roomservice.dto.bed.VacateRequestCreateRequest;
import com.pgManagement.roomservice.dto.bed.VacateRequestResponse;
import com.pgManagement.roomservice.entity.BedStatus;
import com.pgManagement.roomservice.service.BedService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/beds")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping("/{bedId}")
    public ResponseEntity<BedDetailsResponse> getBed(@PathVariable String bedId) {
        return ResponseEntity.ok(bedService.getBed(bedId));
    }

    @GetMapping
    public ResponseEntity<List<BedDetailsResponse>> searchBeds(
            @RequestParam(required = false) UUID pgId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) BedStatus status) {
        return ResponseEntity.ok(bedService.searchBeds(pgId, roomId, status));
    }

    @PatchMapping("/{bedId}/status")
    public ResponseEntity<BedDetailsResponse> updateBedStatus(
            @PathVariable String bedId,
            @Valid @RequestBody BedStatusUpdateRequest request) {
        return ResponseEntity.ok(bedService.updateBedStatus(bedId, request));
    }

    @PostMapping("/{bedId}/assignments")
    public ResponseEntity<AssignmentResponse> assignBed(
            @PathVariable String bedId,
            @Valid @RequestBody AssignBedRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bedService.assignTenant(bedId, request));
    }

    @PatchMapping("/{bedId}/assignments/{assignmentId}/verify")
    public ResponseEntity<AssignmentResponse> verifyAssignment(
            @PathVariable String bedId,
            @PathVariable UUID assignmentId) {
        return ResponseEntity.ok(bedService.verifyAssignment(bedId, assignmentId));
    }

    @PatchMapping("/{bedId}/assignments/{assignmentId}/extend")
    public ResponseEntity<AssignmentResponse> extendAssignment(
            @PathVariable String bedId,
            @PathVariable UUID assignmentId,
            @Valid @RequestBody ExtendAssignmentRequest request) {
        return ResponseEntity.ok(bedService.extendAssignment(bedId, assignmentId, request));
    }

    @PatchMapping("/{bedId}/assignments/{assignmentId}/convert-to-permanent")
    public ResponseEntity<AssignmentResponse> convertToPermanent(
            @PathVariable String bedId,
            @PathVariable UUID assignmentId) {
        return ResponseEntity.ok(bedService.convertToPermanent(bedId, assignmentId));
    }

    @PostMapping("/{bedId}/vacate-requests")
    public ResponseEntity<VacateRequestResponse> createVacateRequest(
            @PathVariable String bedId,
            @Valid @RequestBody VacateRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bedService.createVacateRequest(bedId, request));
    }

    @PatchMapping("/{bedId}/vacate-requests/{requestId}")
    public ResponseEntity<VacateRequestResponse> reviewVacateRequest(
            @PathVariable String bedId,
            @PathVariable UUID requestId,
            @Valid @RequestBody VacateActionRequest request) {
        return ResponseEntity.ok(bedService.reviewVacateRequest(bedId, requestId, request));
    }

    @PostMapping("/{bedId}/vacate")
    public ResponseEntity<BedDetailsResponse> completeVacate(
            @PathVariable String bedId,
            @RequestParam(required = false) String tenantId) {
        return ResponseEntity.ok(bedService.completeVacate(bedId, tenantId));
    }
}
