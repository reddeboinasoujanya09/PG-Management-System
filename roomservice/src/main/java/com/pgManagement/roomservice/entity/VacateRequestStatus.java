package com.pgManagement.roomservice.entity;

public enum VacateRequestStatus {
    CREATED,        // tenant submitted request
    UNDER_REVIEW,   // owner notified, reviewing
    TO_BE_VACATED,  // owner approved — bed status also flips to TO_BE_VACANT on this day
    DENIED,         // owner rejected
    CANCELLED       // tenant cancelled (only allowed if bed not yet re-booked)
}
