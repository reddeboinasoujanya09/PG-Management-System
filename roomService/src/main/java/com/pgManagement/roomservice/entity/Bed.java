package com.pgManagement.roomservice.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(
        name = "beds",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"room_id","bed_label"})
        }
)
@Data
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bedId;

    @Column(nullable = false)
    private String bedLabel;

    @Enumerated(EnumType.STRING)
    private BedStatus bedStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToMany(
            mappedBy = "bed",
            cascade = CascadeType.ALL)
    private List<RoomAssignment> assignments = new ArrayList<>();

    @OneToMany(
            mappedBy = "bed",
            cascade = CascadeType.ALL)
    private List<VacateRequest> vacateRequests = new ArrayList<>();
}