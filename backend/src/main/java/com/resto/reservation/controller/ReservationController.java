package com.resto.reservation.controller;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.repository.ReservationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger LOGGER = Logger.getLogger(ReservationController.class.getName());

    private final ReservationRepository reservationRepo;

    public ReservationController(ReservationRepository reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    @GetMapping("/oftable")
    public ResponseEntity<List<Object>> getTableReservations(@RequestParam("table") RestaurantTable table) {
        return ResponseEntity.ok(reservationRepo.getTimesByRestaurantTable(table));
    }

    @PostMapping()
    public Reservation addReservation(@RequestBody Reservation newReservation) {
        Reservation savedReservation = reservationRepo.save(newReservation);
        LOGGER.log(Level.INFO, String.format("Reservation with id %s made", savedReservation.getId()));
        return savedReservation;
    }
}
