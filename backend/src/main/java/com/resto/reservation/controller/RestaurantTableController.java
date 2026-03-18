package com.resto.reservation.controller;

import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.repository.RestaurantTableRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableRepository tableRepo;

    public RestaurantTableController(RestaurantTableRepository tableRepo) {
        this.tableRepo = tableRepo;
    }

    @GetMapping("/availableat")
    public ResponseEntity<List<RestaurantTable>> getAvailableTablesDuring(@RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        ZonedDateTime utc = time.withZoneSameInstant(ZoneOffset.UTC);
        return ResponseEntity.ok(tableRepo.findAvailableTablesAtTime(utc));
    }
}
