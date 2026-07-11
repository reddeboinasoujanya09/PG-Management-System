package com.pgManagement.roomservice.service;

import com.pgManagement.roomservice.repository.RoomRepository;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private RoomRepository roomRepository;
    RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public
}
