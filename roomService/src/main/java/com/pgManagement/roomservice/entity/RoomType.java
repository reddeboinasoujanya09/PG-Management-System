package com.pgManagement.roomservice.entity;

public enum RoomType {
    SINGLE(1),
    DOUBLE(2),
    TRIPLE(3),
    QUAD(4),
    SUITE(1),
    DORMITORY(10);

    private final int bedCount;

    RoomType(int bedCount) {
        this.bedCount = bedCount;
    }

    public int getBedCount() {
        return bedCount;
    }
}
