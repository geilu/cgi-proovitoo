package com.resto.reservation.controller;

import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.enums.UserPreferences;
import com.resto.reservation.repository.RestaurantTableRepository;
import com.resto.reservation.service.RestaurantTableService;
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
    private final RestaurantTableService restaurantTableService;

    public RestaurantTableController(RestaurantTableRepository tableRepo, RestaurantTableService restaurantTableService) {
        this.tableRepo = tableRepo;
        this.restaurantTableService = restaurantTableService;
    }

    @GetMapping("/availableat")
    public ResponseEntity<List<RestaurantTable>> getAvailableTablesDuring(@RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time) {
        ZonedDateTime utc = time.withZoneSameInstant(ZoneOffset.UTC);
        return ResponseEntity.ok(tableRepo.findAvailableTablesAtTime(utc));
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<RestaurantTable>> getFilteredTables(@RequestParam(value = "time", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime time,
                                                                                                                @RequestParam(value = "userPreferences", required = false) List<UserPreferences> userPreferences,
                                                                                                                @RequestParam(value = "groupSize", required = false) Integer groupSize) {
        return ResponseEntity.ok(restaurantTableService.filterTables(time, userPreferences, groupSize));
    }
}
