package com.resto.reservation.repository;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT (r.id, r.startTime, r.endTime) FROM Reservation r
                        WHERE r.restaurantTable = :restaurantTable
            """)
    List<Object> getTimesByRestaurantTable(@Param("restaurantTable") RestaurantTable restaurantTable);
}
