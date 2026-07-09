package com.pgManagement.roomservice.entity;

public enum RoomStatus {
    AVAILABLE,        // at least one bed free
    BOOKED,           // permanent tenant
    BOOKED_TEMP,      // temporary tenant
    TO_BE_VACANT,     // vacate approved, still occupied
    MAINTENANCE
}

