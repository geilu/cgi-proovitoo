package com.resto.reservation.repository;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.entity.responseobjects.ReservationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT new com.resto.reservation.entity.responseobjects.ReservationResponse(r.id, r.startTime, r.endTime) FROM Reservation r
                        WHERE r.restaurantTable = :restaurantTable
        """)
    List<ReservationResponse> getTimesByRestaurantTable(@Param("restaurantTable") RestaurantTable restaurantTable);

    @Query("""
        SELECT COUNT(r) FROM Reservation r
                WHERE r.restaurantTable = :restaurantTable
                AND r.startTime < :newEndTime AND r.endTime > :newStartTime
        """)
    int countOverlappingReservations(@Param("restaurantTable") RestaurantTable restaurantTable,
                                                            @Param("newStartTime") ZonedDateTime newStartTime,
                                                            @Param("newEndTime") ZonedDateTime newEndTime);
}
