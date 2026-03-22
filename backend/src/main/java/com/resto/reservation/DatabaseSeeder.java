package com.resto.reservation;

import com.resto.reservation.config.RestaurantConfig;
import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.entity.User;
import com.resto.reservation.enums.ReservationStatus;
import com.resto.reservation.repository.ReservationRepository;
import com.resto.reservation.repository.RestaurantTableRepository;
import com.resto.reservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Value("${app.seed-data:false}") // only seed when enabled (e.g disabled for tests. configure in the respective application.properties)
    private boolean seedData;

    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());
    private final Random r = new Random();

    private final RestaurantTableRepository tableRepo;
    private final ReservationRepository reservationRepo;
    private final UserRepository userRepo;

    private final RestaurantConfig restaurantConfig;

    record ReservationSlot(Long tableId, ZonedDateTime startTime, ZonedDateTime endTime) {}

    public DatabaseSeeder(RestaurantTableRepository tableRepo, ReservationRepository reservationRepo, UserRepository userRepo, RestaurantConfig restaurantConfig) {
        this.tableRepo = tableRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
        this.restaurantConfig = restaurantConfig;
    }

    @Override
    public void run(String... args) {
        if (!seedData) return;
        if (tableRepo.count() == 0) { // dont add these if table isn't empty
            RestaurantTable table1 = new RestaurantTable();
            table1.setTableNumber(2);
            table1.setCapacity(4);
            table1.setX(1);
            table1.setY(1);

            RestaurantTable table2 = new RestaurantTable();
            table2.setTableNumber(1);
            table2.setCapacity(6);
            table2.setX(2);
            table2.setY(2);

            tableRepo.save(table1);
            tableRepo.save(table2);
        }

        reservationRepo.deleteAll(); // clear reservations

        List<RestaurantTable> tables = tableRepo.findAll();

        generateRandomReservations(5, r, tables);

        // + one ongoing reservation
        Reservation reservation = new Reservation();

        User user = new User();
        user.setFirstName("Name");
        user.setLastName("LastName");
        userRepo.save(user);
        reservation.setUser(user);

        reservation.setGuestCount(4);

        reservation.setStartTime(LocalDateTime.now().minusHours(1L).atZone(ZoneOffset.UTC));
        reservation.setEndTime(LocalDateTime.now().plusHours(1L).atZone(ZoneOffset.UTC));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setRestaurantTable(tables.get(r.nextInt(tables.size())));

        reservationRepo.save(reservation);

        LOGGER.log(Level.INFO, "Dummy data added to database");
    }

    private void generateRandomReservations(int count, Random r, List<RestaurantTable> tables) {
        userRepo.deleteAll();
        User user = new User();
        user.setFirstName("Aadu");
        user.setLastName("Beedu");
        userRepo.save(user);

        LocalDate date = LocalDate.now();

        long durationSeconds = TimeUnit.MINUTES.toSeconds(120);

        int openSeconds = restaurantConfig.getOpenTimeToday().toLocalTime().toSecondOfDay();
        int closeSeconds = (int) (restaurantConfig.getCloseTimeToday().toLocalTime().toSecondOfDay() - durationSeconds); // take off 2 hours so reservation doesnt go over close time

        ZoneId timezone = restaurantConfig.getZoneId();
        List<ReservationSlot> madeReservations = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Reservation reservation = new Reservation();
            reservation.setUser(user);

            int randomSeconds = r.nextInt(openSeconds, (closeSeconds+1));
            LocalTime startLocalTime = LocalTime.ofSecondOfDay(randomSeconds);
            ZonedDateTime startTime = ZonedDateTime.of(date, startLocalTime, timezone);
            reservation.setStartTime(startTime);

            LocalTime localEndTime = LocalTime.ofSecondOfDay(randomSeconds + durationSeconds);
            ZonedDateTime endTime = ZonedDateTime.of(date, localEndTime, timezone);
            reservation.setEndTime(endTime);

            RestaurantTable reservationTable = tables.get(r.nextInt(tables.size()));
            reservation.setRestaurantTable(reservationTable);
            reservation.setGuestCount(r.nextInt(2, reservationTable.getCapacity()+1));

            boolean overlaps = madeReservations.stream()
                    .filter(slot -> slot.tableId().equals(reservationTable.getId()))
                    .anyMatch(slot -> overlapping(startTime, endTime, slot));
            if (overlaps) {
                i--;
                continue;
            }

            reservation.setStatus(endTime.isBefore(ZonedDateTime.now())
                    ? ReservationStatus.COMPLETED
                    : ReservationStatus.CONFIRMED); // if the generated reservations end time is before now set it as completed

            Reservation savedReservation = reservationRepo.save(reservation);

            ReservationSlot reservationSlot = new ReservationSlot(savedReservation.getRestaurantTable().getId(), savedReservation.getStartTime(), savedReservation.getEndTime()); // using reservationslot record to avoid making overlapping reservations
            madeReservations.add(reservationSlot);
        }
    }

    private boolean overlapping(ZonedDateTime newStartTime, ZonedDateTime newEndTime, ReservationSlot existing) {
        return newStartTime.isBefore(existing.endTime) && newEndTime.isAfter(existing.startTime);
    }
}
