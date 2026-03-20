package com.resto.reservation.service;

import com.resto.reservation.config.ZoneConfig;
import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.enums.UserPreferences;
import com.resto.reservation.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                        .collect(Collectors.toList());
            }
        }

        if (userPreferences != null && !userPreferences.isEmpty()) {
            // add up all tables within preferred zones, then determine what tables match with other conditions
            filteredTables = applyUserPreferenceFilter(filteredTables, userPreferences);
        }

        return filteredTables;
    }

    public RestaurantTable getRecommendedTable(List<RestaurantTable> filteredTables, List<UserPreferences> userPreferences) {
        List<RestaurantTable> tableList = filteredTables;

        if (userPreferences != null && !userPreferences.isEmpty()) {
            List<RestaurantTable> preferredTables = applyUserPreferenceFilter(filteredTables, userPreferences);
            if (!preferredTables.isEmpty()) {
                tableList = preferredTables;
            }
        }

        return tableList.stream()
                .min(Comparator.comparingInt(RestaurantTable::getCapacity))
                .orElse(null);
    }

    public List<RestaurantTable> applyUserPreferenceFilter(List<RestaurantTable> filteredTables, List<UserPreferences> userPreferences) {
        List<RestaurantTable> preferredTables = new ArrayList<>();
        for (UserPreferences preference : userPreferences) {
            ZoneConfig.Bounds zoneBounds = zoneService.getZoneBounds(preference.name());
            preferredTables.addAll(restaurantTableRepo.getByPositionIn(zoneBounds.getxMin(), zoneBounds.getyMin(), zoneBounds.getxMax(), zoneBounds.getyMax()));
        }

        if (filteredTables.isEmpty()) {
            return new ArrayList<>(preferredTables);
        } else {
            filteredTables = filteredTables.stream()
                    .filter(preferredTables::contains)
                    .collect(Collectors.toList());
        }

        return filteredTables;
    }
}
