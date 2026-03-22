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
        if (tableRepo.count() == 0) { // add tables on first load
            List<RestaurantTable> tables = List.of(
                    // x coordinate: column, y coordinate: row. point 1,1 is bottom left corner
                    // terrace area
                    createTable(1, 4, 1, 1),
                    createTable(2, 4, 2, 1),
                    createTable(3, 6, 3, 1),
                    createTable(4, 2, 4, 1),

                    createTable(5, 6, 1, 2),
                    createTable(6, 4, 2, 2),
                    createTable(7, 4, 3, 2),
                    createTable(8, 2, 4, 2),
                    // children area
                    createTable(9, 4, 1, 3),
                    createTable(10, 4, 2, 3),
                    createTable(11, 4, 3, 3),

                    createTable(12, 6, 1, 4),
                    createTable(14, 8, 2, 4),
                    createTable(15, 6, 3, 4),

                    createTable(19, 6, 1, 5),
                    createTable(20, 8, 2, 5),
                    createTable(21, 6, 3, 5),

                    createTable(25, 4, 1, 6),
                    createTable(26, 4, 2, 6),
                    createTable(27, 4, 3, 6),
                    // quiet area
                    createTable(13, 4, 4, 3),

                    createTable(16, 4, 4, 4),
                    createTable(17, 2, 5, 4),
                    createTable(18, 2, 6, 4),

                    createTable(22, 4, 4, 5),
                    createTable(23, 2, 5, 5),
                    createTable(24, 2, 6, 5),

                    createTable(28, 4, 4, 6),
                    createTable(29, 2, 5, 6),
                    createTable(30, 2, 6, 6)
            );

            tableRepo.saveAll(tables);
        }

        reservationRepo.deleteAll(); // clear reservations and users
        userRepo.deleteAll();

        List<RestaurantTable> tables = tableRepo.findAll();
        // reservations for three upcoming days
        generateRandomReservations(20, r, tables, 0);
        generateRandomReservations(10, r, tables, 1);
        generateRandomReservations(5, r, tables, 2);

        LOGGER.log(Level.INFO, "Dummy data added to database");
    }

    private void generateRandomReservations(int count, Random r, List<RestaurantTable> tables, long daysAhead) {
        User user = new User();
        user.setFirstName("Aadu");
        user.setLastName("Beedu");
        userRepo.save(user);

        LocalDate date = LocalDate.now().plusDays(daysAhead);

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

    private RestaurantTable createTable(int tableNumber, int capacity, int x, int y) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setX(x);
        table.setY(y);
        return table;
    }
}
