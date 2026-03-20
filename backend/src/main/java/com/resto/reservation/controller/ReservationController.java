package com.resto.reservation.controller;

import com.resto.reservation.entity.Reservation;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.entity.responseobjects.ReservationResponse;
import com.resto.reservation.exceptions.RestaurantException;
import com.resto.reservation.repository.ReservationRepository;
import com.resto.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@Tag(name = "Reservations", description = "Reservation management endpoints")
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger LOGGER = Logger.getLogger(ReservationController.class.getName());

    private final ReservationRepository reservationRepo;
    private final ReservationService reservationService;

    public ReservationController(ReservationRepository reservationRepo, ReservationService reservationService) {
        this.reservationRepo = reservationRepo;
        this.reservationService = reservationService;
    }

    @Operation(
            summary = "Get reservation times of a specific table",
            description = "Returns a list of reserved time slots associated with the given table",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved reservation times for the table",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(
                                            implementation = ReservationResponse.class
                                    ))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or missing table parameter",
                            content = @Content
                    )
            }
    )
    @GetMapping("/oftable")
    public ResponseEntity<List<ReservationResponse>> getTableReservations(
            @Parameter(
                    description = "The restaurant table to retrieve reservations for",
                    required = true,
                    schema = @Schema(
                            implementation = RestaurantTable.class
                    )
            )
            @RequestParam("table") RestaurantTable table) {
        return ResponseEntity.ok(reservationRepo.getTimesByRestaurantTable(table));
    }

    @Operation(
            summary = "Create a new reservation",
            description = "Saves a new reservation if it does not overlap with any existing reservation for the same table",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The reservation object to create",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Reservation.class
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Reservation successfully created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            implementation = Reservation.class
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Reservation overlaps an existing reservation for the same table",
                            content = @Content
                    )
            }
    )
    @PostMapping()
    public ResponseEntity<Reservation> addReservation(@RequestBody Reservation newReservation) {
        if (reservationService.overlapsExistingReservation(newReservation)) {
            throw new RestaurantException("Reservation overlaps another");
        }
        Reservation savedReservation = reservationRepo.save(newReservation);
        LOGGER.log(Level.INFO, String.format("Reservation with id %s made", savedReservation.getId()));
        return ResponseEntity.ok(savedReservation);
    }
}
