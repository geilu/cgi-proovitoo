package com.resto.reservation.service;

import com.resto.reservation.config.RestaurantConfig;
import com.resto.reservation.config.ZoneConfig;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ZoneService {

    private final RestaurantConfig config;

    public ZoneService(RestaurantConfig config) {
        this.config = config;
    }

    public Optional<ZoneConfig> resolveZone(int x, int y) {
        return config.getZones().stream()
                .filter(zone -> zone.contains(x, y))
                .findFirst();
    }

    public ZoneConfig.Bounds getZoneBounds(String zoneName) {
        return config.getZones().stream()
                .filter(zone -> zone.getName().equals(zoneName))
                .map(ZoneConfig::getBounds)
                .findFirst()
                .orElse(null);
    }
}
