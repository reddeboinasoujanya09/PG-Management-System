package com.pgManagement.roomservice.service;

import com.pgManagement.roomservice.dto.bed.AssignBedRequest;
import com.pgManagement.roomservice.dto.bed.AssignmentResponse;
import com.pgManagement.roomservice.dto.bed.BedDetailsResponse;
import com.pgManagement.roomservice.dto.bed.BedStatusUpdateRequest;
import com.pgManagement.roomservice.dto.bed.ExtendAssignmentRequest;
import com.pgManagement.roomservice.dto.bed.VacateActionRequest;
import com.pgManagement.roomservice.dto.bed.VacateRequestCreateRequest;
import com.pgManagement.roomservice.dto.bed.VacateRequestResponse;
import com.pgManagement.roomservice.entity.AssignmentStatus;
import com.pgManagement.roomservice.entity.Bed;
import com.pgManagement.roomservice.entity.BedStatus;
import com.pgManagement.roomservice.entity.Room;
import com.pgManagement.roomservice.entity.RoomAssignment;
import com.pgManagement.roomservice.entity.RoomStatus;
import com.pgManagement.roomservice.entity.TenantType;
import com.pgManagement.roomservice.entity.VacateRequest;
import com.pgManagement.roomservice.entity.VacateRequestStatus;
import com.pgManagement.roomservice.repository.BedRepository;
import com.pgManagement.roomservice.repository.RoomAssignmentRepository;
import com.pgManagement.roomservice.repository.RoomRepository;
import com.pgManagement.roomservice.repository.VacateRequestRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BedService {

    private static final String BED_NOT_FOUND = "Bed not found: ";

    private static final Set<AssignmentStatus> OPEN_ASSIGNMENT_STATUSES =
            EnumSet.of(AssignmentStatus.PENDING_VERIFICATION, AssignmentStatus.ACTIVE);

    private static final Set<VacateRequestStatus> OPEN_VACATE_STATUSES =
            EnumSet.of(VacateRequestStatus.CREATED, VacateRequestStatus.UNDER_REVIEW, VacateRequestStatus.TO_BE_VACATED);

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final VacateRequestRepository vacateRequestRepository;

    public BedService(BedRepository bedRepository,
                      RoomRepository roomRepository,
                      RoomAssignmentRepository assignmentRepository,
                      VacateRequestRepository vacateRequestRepository) {
        this.bedRepository = bedRepository;
        this.roomRepository = roomRepository;
        this.assignmentRepository = assignmentRepository;
        this.vacateRequestRepository = vacateRequestRepository;
    }

    @Transactional(readOnly = true)
    public BedDetailsResponse getBed(String bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new IllegalArgumentException(BED_NOT_FOUND + bedId));
        return toBedResponse(bed);
    }

    @Transactional(readOnly = true)
    public List<BedDetailsResponse> searchBeds(java.util.UUID pgId, java.util.UUID roomId, BedStatus status) {
        return bedRepository.search(pgId, roomId, status).stream().map(this::toBedResponse).toList();
    }

    @Transactional
    public BedDetailsResponse updateBedStatus(String bedId, BedStatusUpdateRequest request) {
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new IllegalArgumentException(BED_NOT_FOUND + bedId));

        BedStatus newStatus = request.newStatus();

        // Derived statuses should not be set manually.
        if (newStatus == BedStatus.OCCUPIED || newStatus == BedStatus.TO_BE_VACANT) {
            throw new IllegalArgumentException("Use assignment/vacate flows for OCCUPIED/TO_BE_VACANT");
        }

        boolean hasOpenAssignment = assignmentRepository.existsByBed_BedIdAndStatusIn(
                bedId, OPEN_ASSIGNMENT_STATUSES);

        if (hasOpenAssignment) {
            throw new IllegalStateException("Cannot manually change status while assignment is active/pending");
        }

        bed.setBedStatus(newStatus);
        recalculateRoomStatus(bed.getRoom());
        return toBedResponse(bed);
    }

    @Transactional
    public AssignmentResponse assignTenant(String bedId, AssignBedRequest request) {
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new IllegalArgumentException(BED_NOT_FOUND + bedId));

        if (bed.getBedStatus() != BedStatus.AVAILABLE) {
            throw new IllegalStateException("Bed is not available for assignment");
        }

        if (assignmentRepository.existsByBed_BedIdAndStatusIn(bedId, OPEN_ASSIGNMENT_STATUSES)) {
            throw new IllegalStateException("Bed already has active/pending assignment");
        }

        if (assignmentRepository.existsByTenantIdAndStatusIn(request.tenantId(), OPEN_ASSIGNMENT_STATUSES)) {
            throw new IllegalStateException("Tenant already has active/pending bed assignment");
        }

        LocalDate joiningDate = request.joiningDate() == null ? LocalDate.now() : request.joiningDate();
        LocalDate vacatingDate = request.vacatingDate();

        if (request.tenantType() == TenantType.TEMPORARY) {
            if (vacatingDate == null || !vacatingDate.isAfter(joiningDate)) {
                throw new IllegalArgumentException("Temporary tenant must have vacatingDate after joiningDate");
            }
        } else {
            if (vacatingDate == null) {
                vacatingDate = joiningDate.plusYears(10);
            }
        }

        RoomAssignment assignment = new RoomAssignment();
        assignment.setBed(bed);
        assignment.setTenantId(request.tenantId());
        assignment.setTenantType(request.tenantType());
        assignment.setJoiningDate(joiningDate);
        assignment.setVacatingDate(vacatingDate);
        assignment.setAdvancePaid(request.advancePaid());
        assignment.setAssignedBy(request.assignedBy());
        assignment.setNotes(request.notes());
        assignment.setStatus(AssignmentStatus.PENDING_VERIFICATION);
        assignment.setVerified(false);

        assignmentRepository.save(assignment);

        bed.setBedStatus(BedStatus.OCCUPIED);
        recalculateRoomStatus(bed.getRoom());

        return toAssignmentResponse(assignment);
    }

    @Transactional
    public AssignmentResponse verifyAssignment(String bedId, java.util.UUID assignmentId) {
        RoomAssignment assignment = assignmentRepository.findByIdForUpdate(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        if (!assignment.getBed().getBedId().equals(bedId)) {
            throw new IllegalArgumentException("Assignment does not belong to bed: " + bedId);
        }

        if (assignment.getStatus() != AssignmentStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Only PENDING_VERIFICATION assignments can be verified");
        }

        assignment.setVerified(true);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        Bed bed = assignment.getBed();
        if (bed.getBedStatus() != BedStatus.OCCUPIED) {
            bed.setBedStatus(BedStatus.OCCUPIED);
        }
        recalculateRoomStatus(bed.getRoom());

        return toAssignmentResponse(assignment);
    }

    @Transactional
    public VacateRequestResponse createVacateRequest(String bedId, VacateRequestCreateRequest request) {
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new IllegalArgumentException(BED_NOT_FOUND + bedId));

        RoomAssignment assignment = assignmentRepository.findFirstByBed_BedIdAndStatusInOrderByJoiningDateDesc(
                        bedId, Set.of(AssignmentStatus.ACTIVE))
                .orElseThrow(() -> new IllegalStateException("No ACTIVE assignment found for this bed"));

        if (!assignment.getTenantId().equals(request.tenantId())) {
            throw new IllegalArgumentException("tenantId does not match current bed assignment");
        }

        if (request.requestedVacateDate() == null || request.requestedVacateDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("requestedVacateDate must be today or future");
        }

        boolean hasOpenRequest = vacateRequestRepository.existsByAssignment_AssignmentIdAndStatusIn(
                assignment.getAssignmentId(), OPEN_VACATE_STATUSES);
        if (hasOpenRequest) {
            throw new IllegalStateException("An open vacate request already exists for this assignment");
        }

        VacateRequest vr = new VacateRequest();
        vr.setBed(bed);
        vr.setAssignment(assignment);
        vr.setTenantId(request.tenantId());
        vr.setRequestedVacateDate(request.requestedVacateDate());
        vr.setDescription(request.description());
        vr.setStatus(VacateRequestStatus.CREATED);
        vr.setCreatedAt(LocalDateTime.now());

        vacateRequestRepository.save(vr);
        return toVacateResponse(vr);
    }

    @Transactional
    public VacateRequestResponse reviewVacateRequest(String bedId, java.util.UUID requestId, VacateActionRequest actionReq) {
        VacateRequest vr = vacateRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Vacate request not found: " + requestId));

        if (!vr.getBed().getBedId().equals(bedId)) {
            throw new IllegalArgumentException("Vacate request does not belong to bed: " + bedId);
        }

        VacateRequestStatus action = actionReq.action();
        if (action == null) {
            throw new IllegalArgumentException("action is required");
        }

        switch (vr.getStatus()) {
            case CREATED -> {
                if (action != VacateRequestStatus.UNDER_REVIEW) {
                    throw new IllegalStateException("Allowed transition: CREATED -> UNDER_REVIEW");
                }
                vr.setStatus(VacateRequestStatus.UNDER_REVIEW);
            }
            case UNDER_REVIEW -> {
                if (action == VacateRequestStatus.TO_BE_VACATED) {
                    vr.setStatus(VacateRequestStatus.TO_BE_VACATED);
                    vr.getBed().setBedStatus(BedStatus.TO_BE_VACANT);
                    vr.getAssignment().setVacatingDate(vr.getRequestedVacateDate());
                } else if (action == VacateRequestStatus.DENIED) {
                    vr.setStatus(VacateRequestStatus.DENIED);
                } else {
                    throw new IllegalStateException("Allowed transitions: UNDER_REVIEW -> TO_BE_VACATED | DENIED");
                }
            }
            case TO_BE_VACATED -> {
                if (action != VacateRequestStatus.CANCELLED) {
                    throw new IllegalStateException("Allowed transition: TO_BE_VACATED -> CANCELLED");
                }
                boolean rebooked = assignmentRepository.existsByBed_BedIdAndStatusInAndJoiningDateGreaterThanEqual(
                        bedId, OPEN_ASSIGNMENT_STATUSES, vr.getRequestedVacateDate());
                if (rebooked) {
                    throw new IllegalStateException("Cannot cancel; bed is already rebooked for or after vacate date");
                }
                vr.setStatus(VacateRequestStatus.CANCELLED);
                vr.getBed().setBedStatus(BedStatus.OCCUPIED);
            }
            case DENIED, CANCELLED -> throw new IllegalStateException("Cannot modify terminal vacate request state");
        }

        recalculateRoomStatus(vr.getBed().getRoom());
        return toVacateResponse(vr);
    }

    @Transactional
    public BedDetailsResponse completeVacate(String bedId, String tenantId) {
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new IllegalArgumentException(BED_NOT_FOUND + bedId));

        RoomAssignment assignment = assignmentRepository.findFirstByBed_BedIdAndStatusInOrderByJoiningDateDesc(
                        bedId, Set.of(AssignmentStatus.ACTIVE))
                .orElseThrow(() -> new IllegalStateException("No ACTIVE assignment to vacate"));

        if (tenantId != null && !tenantId.isBlank() && !assignment.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("tenantId does not match active assignment");
        }

        assignment.setStatus(AssignmentStatus.VACATED);
        bed.setBedStatus(BedStatus.AVAILABLE);

        recalculateRoomStatus(bed.getRoom());
        return toBedResponse(bed);
    }

    @Transactional
    public AssignmentResponse extendAssignment(String bedId, java.util.UUID assignmentId,
                                               ExtendAssignmentRequest request) {
        RoomAssignment assignment = assignmentRepository.findByIdForUpdate(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        if (!assignment.getBed().getBedId().equals(bedId)) {
            throw new IllegalArgumentException("Assignment does not belong to bed: " + bedId);
        }
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE assignments can be extended");
        }
        if (assignment.getTenantType() != TenantType.TEMPORARY) {
            throw new IllegalStateException("Only TEMPORARY assignments can be extended");
        }
        if (!request.newVacatingDate().isAfter(assignment.getVacatingDate())) {
            throw new IllegalArgumentException("newVacatingDate must be after current vacatingDate");
        }
        if (!request.newVacatingDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("newVacatingDate must be in the future");
        }

        assignment.setVacatingDate(request.newVacatingDate());
        recalculateRoomStatus(assignment.getBed().getRoom());
        return toAssignmentResponse(assignment);
    }

    @Transactional
    public AssignmentResponse convertToPermanent(String bedId, java.util.UUID assignmentId) {
        RoomAssignment assignment = assignmentRepository.findByIdForUpdate(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));

        if (!assignment.getBed().getBedId().equals(bedId)) {
            throw new IllegalArgumentException("Assignment does not belong to bed: " + bedId);
        }
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE assignments can be converted");
        }
        if (assignment.getTenantType() == TenantType.PERMANENT) {
            throw new IllegalStateException("Assignment is already permanent");
        }

        assignment.setTenantType(TenantType.PERMANENT);
        assignment.setVacatingDate(assignment.getJoiningDate().plusYears(10));
        recalculateRoomStatus(assignment.getBed().getRoom());
        return toAssignmentResponse(assignment);
    }

    private void recalculateRoomStatus(Room room) {
        List<Bed> beds = room.getBeds();
        if (beds == null || beds.isEmpty()) {
            room.setRoomStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);
            return;
        }

        boolean hasAvailable = beds.stream().anyMatch(b -> b.getBedStatus() == BedStatus.AVAILABLE);
        boolean hasToBeVacant = beds.stream().anyMatch(b -> b.getBedStatus() == BedStatus.TO_BE_VACANT);
        boolean hasOccupied = beds.stream().anyMatch(b -> b.getBedStatus() == BedStatus.OCCUPIED);
        boolean allMaintenance = beds.stream().allMatch(b -> b.getBedStatus() == BedStatus.MAINTENANCE);

        if (hasAvailable) {
            room.setRoomStatus(RoomStatus.AVAILABLE);
        } else if (hasToBeVacant) {
            room.setRoomStatus(RoomStatus.TO_BE_VACANT);
        } else if (hasOccupied) {
            List<RoomAssignment> openAssignments =
                    assignmentRepository.findByBed_Room_RoomIdAndStatusIn(room.getRoomId(), OPEN_ASSIGNMENT_STATUSES);
            boolean hasTemporary = openAssignments.stream().anyMatch(a -> a.getTenantType() == TenantType.TEMPORARY);
            room.setRoomStatus(hasTemporary ? RoomStatus.BOOKED_TEMP : RoomStatus.BOOKED);
        } else if (allMaintenance) {
            room.setRoomStatus(RoomStatus.MAINTENANCE);
        } else {
            room.setRoomStatus(RoomStatus.AVAILABLE);
        }

        roomRepository.save(room);
    }

    private BedDetailsResponse toBedResponse(Bed bed) {
        RoomAssignment current = assignmentRepository
                .findFirstByBed_BedIdAndStatusInOrderByJoiningDateDesc(bed.getBedId(), OPEN_ASSIGNMENT_STATUSES)
                .orElse(null);

        return new BedDetailsResponse(
                bed.getBedId(),
                bed.getBedLabel(),
                bed.getBedStatus(),
                bed.getRoom().getRoomId(),
                bed.getRoom().getPgId(),
                current == null ? null : current.getAssignmentId(),
                current == null ? null : current.getTenantId(),
                current == null ? null : current.getStatus());
    }

    private AssignmentResponse toAssignmentResponse(RoomAssignment assignment) {
        return new AssignmentResponse(
                assignment.getAssignmentId(),
                assignment.getBed().getBedId(),
                assignment.getTenantId(),
                assignment.getTenantType(),
                assignment.getJoiningDate(),
                assignment.getVacatingDate(),
                assignment.isVerified(),
                assignment.getStatus());
    }

    private VacateRequestResponse toVacateResponse(VacateRequest vr) {
        return new VacateRequestResponse(
                vr.getRequestId(),
                vr.getBed().getBedId(),
                vr.getAssignment().getAssignmentId(),
                vr.getTenantId(),
                vr.getRequestedVacateDate(),
                vr.getStatus(),
                vr.getCreatedAt(),
                vr.getDescription());
    }
}
