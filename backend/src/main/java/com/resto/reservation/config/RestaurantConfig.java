package com.resto.reservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "restaurant")
@Configuration
public class RestaurantConfig {
    private List<ZoneConfig> zones;

    public List<ZoneConfig> getZones() {
        return zones;
    }

    public void setZones(List<ZoneConfig> zones) {
        this.zones = zones;
    }
}