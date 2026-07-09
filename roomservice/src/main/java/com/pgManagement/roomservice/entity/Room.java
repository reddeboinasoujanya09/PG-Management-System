package com.pgManagement.roomservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"pg_id", "room_number"})
        })
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID roomId;

    @Column(nullable = false)
    private UUID pgId;

    @Column(nullable = false)
    private String roomNumber;

    private Short floor;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    //TODO: sync roomStatus and bedstatus

    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

    private BigDecimal monthlyRent;

    @ElementCollection
    @CollectionTable(name = "room_amenities",
            joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    @OneToMany(
            mappedBy = "room",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Bed> beds = new ArrayList<>();
}

