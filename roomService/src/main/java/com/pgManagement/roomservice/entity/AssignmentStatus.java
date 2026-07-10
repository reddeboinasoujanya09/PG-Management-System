package com.pgManagement.roomservice.entity;

public enum AssignmentStatus {
    PENDING_VERIFICATION,   // joined, OTP not yet verified
    ACTIVE,                 // verified, currently staying
    VACATED                 // has left, assignment closed
}
