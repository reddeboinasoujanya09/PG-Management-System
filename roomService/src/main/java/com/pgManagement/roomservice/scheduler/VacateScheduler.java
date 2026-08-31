package com.pgManagement.roomservice.scheduler;

import com.pgManagement.roomservice.entity.AssignmentStatus;
import com.pgManagement.roomservice.entity.RoomAssignment;
import com.pgManagement.roomservice.entity.VacateRequestStatus;
import com.pgManagement.roomservice.repository.RoomAssignmentRepository;
import com.pgManagement.roomservice.repository.VacateRequestRepository;
import com.pgManagement.roomservice.service.BedService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacateScheduler {

    private final VacateRequestRepository vacateRequestRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final BedService bedService;

    @Scheduled(cron = "${room.vacate.reminder.cron:0 0 9 * * *}")
    @Transactional(readOnly = true)
    public void sendTwoDayReminder() {
        LocalDate target = LocalDate.now().plusDays(2);
        var due = vacateRequestRepository.findByStatusAndRequestedVacateDate(
                VacateRequestStatus.TO_BE_VACATED, target);

        // Placeholder logging until notification integration is wired.
        due.forEach(vr -> log.info("Vacate reminder: tenantId={}, bedId={}, vacateDate={}",
                vr.getTenantId(), vr.getBed().getBedId(), vr.getRequestedVacateDate()));
    }

    @Scheduled(cron = "${room.vacate.auto.cron:0 30 0 * * *}")
    @Transactional
    public void autoVacateOnDueDate() {
        LocalDate today = LocalDate.now();
        for (RoomAssignment assignment : assignmentRepository.findByStatusAndVacatingDateLessThanEqual(
                AssignmentStatus.ACTIVE, today)) {
            bedService.completeVacate(assignment.getBed().getBedId(), assignment.getTenantId());
        }
    }
}

