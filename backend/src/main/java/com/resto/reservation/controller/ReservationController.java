package com.resto.reservation.controller;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.repository.ReservationRepository;
import com.resto.reservation.service.ReservationService;
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
    private final ReservationService reservationService;

    public ReservationController(ReservationRepository reservationRepo, ReservationService reservationService) {
        this.reservationRepo = reservationRepo;
        this.reservationService = reservationService;
    }

    @GetMapping("/oftable")
    public ResponseEntity<List<Object>> getTableReservations(@RequestParam("table") RestaurantTable table) {
        return ResponseEntity.ok(reservationRepo.getTimesByRestaurantTable(table));
    }

    @PostMapping()
    public Reservation addReservation(@RequestBody Reservation newReservation) {
        if (reservationService.overlapsExistingReservation(newReservation)) {
            throw new IllegalArgumentException("Reservation overlaps another");
        }
        Reservation savedReservation = reservationRepo.save(newReservation);
        LOGGER.log(Level.INFO, String.format("Reservation with id %s made", savedReservation.getId()));
        return savedReservation;
    }
}
