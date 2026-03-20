package com.resto.reservation.service;

import com.resto.reservation.config.ZoneConfig;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.enums.UserPreferences;
import com.resto.reservation.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepo;
    private final ZoneService zoneService;

    public RestaurantTableService(RestaurantTableRepository restaurantTableRepo, ZoneService zoneService) {
        this.restaurantTableRepo = restaurantTableRepo;
        this.zoneService = zoneService;
    }

    public List<RestaurantTable> filterTables(ZonedDateTime time, List<UserPreferences> userPreferences, Integer groupSize) {
        List<RestaurantTable> filteredTables = new ArrayList<>();

        if (groupSize != null) {
            filteredTables = restaurantTableRepo.getByCapacityGreaterThanEqual(groupSize);
        }
        if (time != null) {
            List<RestaurantTable> availableTables = restaurantTableRepo.findAvailableTablesAtTime(time);
            if (filteredTables.isEmpty()) {
                filteredTables.addAll(availableTables);
            } else {
                filteredTables = filteredTables.stream()
                        .filter(availableTables::contains)
                        .toList();
            }
        }

        if (userPreferences != null && !userPreferences.isEmpty()) {
            // add up all tables within preferred zones, then determine what tables match with other conditions
            List<RestaurantTable> preferredTables = new ArrayList<>();
            for (UserPreferences preference : userPreferences) {
                ZoneConfig.Bounds zoneBounds = zoneService.getZoneBounds(preference.name());
                preferredTables.addAll(restaurantTableRepo.getByPositionIn(zoneBounds.getxMin(), zoneBounds.getyMin(), zoneBounds.getxMax(), zoneBounds.getyMax()));
            }

            if (filteredTables.isEmpty()) {
                filteredTables.addAll(preferredTables);
            } else {
                filteredTables = filteredTables.stream()
                        .filter(preferredTables::contains)
                        .toList();
            }
        }

        return filteredTables;
    }
}
