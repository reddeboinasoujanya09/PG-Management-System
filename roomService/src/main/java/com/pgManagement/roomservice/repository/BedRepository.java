package com.pgManagement.roomservice.repository;

import com.pgManagement.roomservice.entity.Bed;
import com.pgManagement.roomservice.entity.BedStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BedRepository extends JpaRepository<Bed, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b join fetch b.room where b.bedId = :bedId")
    Optional<Bed> findByIdForUpdate(@Param("bedId") String bedId);

    @Query("""
        select b from Bed b
        join b.room r
        where (:pgId is null or r.pgId = :pgId)
          and (:roomId is null or r.roomId = :roomId)
          and (:status is null or b.bedStatus = :status)
        """)
    List<Bed> search(@Param("pgId") UUID pgId,
                     @Param("roomId") UUID roomId,
                     @Param("status") BedStatus status);
}
