package com.resto.reservation.controller;

import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.repository.ReservationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepo;

    public ReservationController(ReservationRepository reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    @GetMapping("/oftable")
    public ResponseEntity<List<Object>> getTableReservations(@RequestParam("table") RestaurantTable table) {
        return ResponseEntity.ok(reservationRepo.getTimesByRestaurantTable(table));
    }
}
