package com.resto.reservation;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.entity.User;
import com.resto.reservation.entity.enums.ReservationStatus;
import com.resto.reservation.repository.ReservationRepository;
import com.resto.reservation.repository.RestaurantTableRepository;
import com.resto.reservation.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());
    private final Random r = new Random();

    private final RestaurantTableRepository tableRepo;
    private final ReservationRepository reservationRepo;
    private final UserRepository userRepo;

    public DatabaseSeeder(RestaurantTableRepository tableRepo, ReservationRepository reservationRepo, UserRepository userRepo) {
        this.tableRepo = tableRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) throws Exception {
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

        generateRandomReservations(3, r, tables);

        // + one ongoing reservation
        Reservation reservation = new Reservation();

        User user = new User();
        user.setFirstName("Name");
        user.setLastName("LastName");
        userRepo.save(user);
        reservation.setUser(user);

        reservation.setGuestCount(4);

        reservation.setStartTime(LocalDateTime.now().minusHours(1L).atZone(ZoneOffset.UTC));
        reservation.setEndTime(LocalDateTime.now().plusMinutes(30L).atZone(ZoneOffset.UTC));

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

        long durationMillis = TimeUnit.MINUTES.toMillis(90);
        int millisInDay = (int) ((24*60*60*1000) - durationMillis);

        for (int i = 0; i < count; i++) {
            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setGuestCount(r.nextInt(1, 5));

            long millis = r.nextInt(millisInDay);
            LocalTime time = new Time(millis).toLocalTime();
            reservation.setStartTime(ZonedDateTime.of(date, time, ZoneOffset.UTC));

            long endMillis = millis + durationMillis;
            LocalTime endTime = new Time(endMillis).toLocalTime();
            reservation.setEndTime(ZonedDateTime.of(date, endTime, ZoneOffset.UTC));

            reservation.setStatus(ReservationStatus.CONFIRMED);

            reservation.setRestaurantTable(tables.get(r.nextInt(tables.size())));

            reservationRepo.save(reservation);
        }
    }
}
