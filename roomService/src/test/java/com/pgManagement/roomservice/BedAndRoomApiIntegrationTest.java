package com.pgManagement.roomservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgManagement.roomservice.dto.CreateRoomRequest;
import com.pgManagement.roomservice.dto.CreateRoomResponse;
import com.pgManagement.roomservice.entity.RoomType;
import com.pgManagement.roomservice.service.RoomService;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BedAndRoomApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomService roomService;

    @Test
    void shouldFilterRoomsByFloorAndAvailability() throws Exception {
        UUID pgId = UUID.randomUUID();
        CreateRoomResponse room = createRoom(pgId, (short) 2, RoomType.DOUBLE);

        mockMvc.perform(get("/api/v1/rooms")
                        .param("pgId", pgId.toString())
                        .param("floor", "2")
                        .param("minAvailableBeds", "1")
                        .param("tenantType", "TEMPORARY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value(room.getRoomId().toString()))
                .andExpect(jsonPath("$[0].floor").value(2));
    }

    @Test
    void shouldRejectRoomTypeChangeWhenOpenAssignmentExists() throws Exception {
        UUID pgId = UUID.randomUUID();
        CreateRoomResponse room = createRoom(pgId, (short) 1, RoomType.DOUBLE);
        String bedId = room.getBeds().get(0).getBedId();

        String assignmentId = createTemporaryAssignmentAndVerify(bedId, "tenant-guard-1");
        // Ensure setup worked.
        if (assignmentId == null || assignmentId.isBlank()) {
            throw new IllegalStateException("Failed to create assignment for guard test");
        }

        mockMvc.perform(put("/api/v1/rooms/{roomId}/type", room.getRoomId())
                        .param("newType", "TRIPLE"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Invalid Operation"));
    }

    @Test
    void shouldEnforceVacateTransitionFromCreatedToUnderReviewFirst() throws Exception {
        UUID pgId = UUID.randomUUID();
        CreateRoomResponse room = createRoom(pgId, (short) 3, RoomType.DOUBLE);
        String bedId = room.getBeds().get(0).getBedId();

        createTemporaryAssignmentAndVerify(bedId, "tenant-vacate-1");

        String vacateRequestBody = objectMapper.writeValueAsString(new VacateCreateBody(
                "tenant-vacate-1", LocalDate.now().plusDays(5).toString(), "moving out"));

        MvcResult vacateCreateResult = mockMvc.perform(post("/api/v1/beds/{bedId}/vacate-requests", bedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vacateRequestBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode vacateJson = objectMapper.readTree(vacateCreateResult.getResponse().getContentAsString());
        String requestId = vacateJson.get("requestId").asText();

        mockMvc.perform(patch("/api/v1/beds/{bedId}/vacate-requests/{requestId}", bedId, UUID.fromString(requestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"TO_BE_VACATED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Allowed transition: CREATED -> UNDER_REVIEW"));
    }

    @Test
    void shouldExtendTemporaryAssignment() throws Exception {
        UUID pgId = UUID.randomUUID();
        CreateRoomResponse room = createRoom(pgId, (short) 4, RoomType.DOUBLE);
        String bedId = room.getBeds().get(0).getBedId();

        String assignmentId = createTemporaryAssignmentAndVerify(bedId, "tenant-extend-1");
        String newDate = LocalDate.now().plusDays(25).toString();

        mockMvc.perform(patch("/api/v1/beds/{bedId}/assignments/{assignmentId}/extend", bedId, UUID.fromString(assignmentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newVacatingDate\":\"" + newDate + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacatingDate").value(newDate))
                .andExpect(jsonPath("$.tenantType").value("TEMPORARY"));
    }

    @Test
    void shouldConvertTemporaryAssignmentToPermanent() throws Exception {
        UUID pgId = UUID.randomUUID();
        CreateRoomResponse room = createRoom(pgId, (short) 5, RoomType.DOUBLE);
        String bedId = room.getBeds().get(0).getBedId();

        String assignmentId = createTemporaryAssignmentAndVerify(bedId, "tenant-convert-1");

        mockMvc.perform(patch("/api/v1/beds/{bedId}/assignments/{assignmentId}/convert-to-permanent", bedId,
                        UUID.fromString(assignmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantType").value("PERMANENT"));
    }

    private CreateRoomResponse createRoom(UUID pgId, short floor, RoomType roomType) {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setPgId(pgId);
        request.setRoomNumber("R" + floor + "-" + UUID.randomUUID().toString().substring(0, 8));
        request.setFloor(floor);
        request.setRoomType(roomType);
        request.setMonthlyRent(BigDecimal.valueOf(10000));
        request.setAmenities(new ArrayList<>(List.of("wifi")));
        return roomService.createRoom(request);
    }

    private String createTemporaryAssignmentAndVerify(String bedId, String tenantId) throws Exception {
        LocalDate joiningDate = LocalDate.now();
        LocalDate vacatingDate = joiningDate.plusDays(10);

        String assignBody = objectMapper.writeValueAsString(new AssignBody(
                tenantId,
                "TEMPORARY",
                joiningDate.toString(),
                vacatingDate.toString(),
                5000,
                "owner-1"));

        MvcResult assignResult = mockMvc.perform(post("/api/v1/beds/{bedId}/assignments", bedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode assignJson = objectMapper.readTree(assignResult.getResponse().getContentAsString());
        String assignmentId = assignJson.get("assignmentId").asText();

        mockMvc.perform(patch("/api/v1/beds/{bedId}/assignments/{assignmentId}/verify", bedId,
                        UUID.fromString(assignmentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        return assignmentId;
    }

    private record AssignBody(
            String tenantId,
            String tenantType,
            String joiningDate,
            String vacatingDate,
            int advancePaid,
            String assignedBy) {
    }

    private record VacateCreateBody(
            String tenantId,
            String requestedVacateDate,
            String description) {
    }
}

