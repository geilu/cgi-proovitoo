package com.resto.reservation;

import com.resto.reservation.controller.ReservationController;
import com.resto.reservation.entity.Reservation;
import com.resto.reservation.repository.ReservationRepository;
import com.resto.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTests {

    @Mock
    private ReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepo;
    @InjectMocks
    private ReservationController reservationController;

    @Test
    void overlappingReservationsThrowsException() {
        Reservation newReservation = new Reservation();
        when(reservationService.overlapsExistingReservation(newReservation)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationController.addReservation(newReservation);
        });

        assertEquals("Reservation overlaps another", exception.getMessage());

        verify(reservationRepo, never()).save(any(Reservation.class)); // also make sure save() was never called
    }

    @Test
    void noOverlapReservationSaves() {
        Reservation newReservation = new Reservation();

        when(reservationService.overlapsExistingReservation(newReservation)).thenReturn(false);
        when(reservationRepo.save(any(Reservation.class))).thenReturn(newReservation);

        Reservation result = reservationController.addReservation(newReservation);

        assertNotNull(result);
        verify(reservationRepo, times(1)).save(newReservation);
    }
}
