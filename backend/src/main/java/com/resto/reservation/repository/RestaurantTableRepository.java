package com.resto.reservation.repository;

import com.resto.reservation.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    @Query("""
    SELECT t FROM RestaurantTable t
    WHERE t.id NOT IN (
        SELECT r.restaurantTable.id FROM Reservation r
        WHERE r.startTime <= :endTime AND r.endTime > :startTime
    )
    """)
    List<RestaurantTable> findAvailableTablesInRange(@Param("startTime") ZonedDateTime startTime, @Param("endTime") ZonedDateTime endTime);

    List<RestaurantTable> getByCapacityGreaterThanEqual(Integer groupSize);

    @Query("""
    SELECT t FROM RestaurantTable t
        WHERE :minX <= t.x AND t.x <= :maxX
            AND :minY <= t.y AND t.y <= :maxY
    """)
    List<RestaurantTable> getByPositionIn(@Param("minX") int minX, @Param("minY") int minY, @Param("maxX") int maxX, @Param("maxY") int maxY);
}
