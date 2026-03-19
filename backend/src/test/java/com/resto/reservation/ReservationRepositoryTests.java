package com.resto.reservation;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.entity.User;
import com.resto.reservation.entity.enums.ReservationStatus;
import com.resto.reservation.repository.ReservationRepository;
import com.resto.reservation.repository.RestaurantTableRepository;
import com.resto.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryTests {

    @Autowired
    private ReservationRepository reservationRepo;
    @Autowired
    private RestaurantTableRepository restaurantTableRepo;
    @Autowired
    private UserRepository userRepo;

    private RestaurantTable testTable;

    @BeforeEach
    void setUp() {
        testTable = new RestaurantTable();
        testTable.setY(1);
        testTable.setX(1);
        testTable.setCapacity(3);
        testTable.setTableNumber(10);
        restaurantTableRepo.save(testTable);

        User testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        userRepo.save(testUser);

        Reservation existingReservation = new Reservation();
        existingReservation.setRestaurantTable(testTable);
        existingReservation.setStartTime(ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 12, 0), ZoneId.of("UTC")));
        existingReservation.setEndTime(ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 14, 0), ZoneId.of("UTC")));
        existingReservation.setUser(testUser);
        existingReservation.setGuestCount(2);
        existingReservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepo.save(existingReservation);
    }

    @Test
    void partialOverlapReturnsTrue() {
        int overlaps = reservationRepo.countOverlappingReservations(
                testTable,
                ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 13, 0), ZoneId.of("UTC")),
                ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 15, 0), ZoneId.of("UTC"))
        );
        assertTrue(overlaps > 0, "Should detect a parrtial overlap");
    }

    @Test
    void backToBackReturnsFalse() {
        int overlaps = reservationRepo.countOverlappingReservations(
                testTable,
                ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 14, 0), ZoneId.of("UTC")),
                ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 16, 0), ZoneId.of("UTC"))
        );

        assertEquals(0, overlaps, "Back-to-back reservations shouldn't overlap");
    }

    @Test
    void differentTableReturnsFalse() {
        RestaurantTable table2 = new RestaurantTable();
        table2.setCapacity(3);
        table2.setTableNumber(20);
        table2.setX(5);
        table2.setY(5);
        restaurantTableRepo.save(table2);

        int overlaps = reservationRepo.countOverlappingReservations(
                table2,
                ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 13, 0), ZoneId.of("UTC")),
                ZonedDateTime.of(LocalDateTime.of(2026, 3, 19, 15, 0), ZoneId.of("UTC"))
        );

        assertEquals(0, overlaps, "should not overlap if it's a different table");
    }
}
