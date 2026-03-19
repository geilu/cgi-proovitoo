package com.resto.reservation.service;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;

    public ReservationService(ReservationRepository reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    // false when no overlaps, true when has overlaps
    public boolean overlapsExistingReservation(Reservation r) {
        RestaurantTable restaurantTable = r.getRestaurantTable();
        int overlappingReservations = reservationRepo.countOverlappingReservations(restaurantTable, r.getStartTime(), r.getEndTime());

        return overlappingReservations > 0;
    }
}
