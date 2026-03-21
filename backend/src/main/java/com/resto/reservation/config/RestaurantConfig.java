package com.resto.reservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@ConfigurationProperties(prefix = "restaurant")
@Configuration
public class RestaurantConfig {
    private List<ZoneConfig> zones;
    private String openTime;
    private String closeTime;
    private String timezone;

    public List<ZoneConfig> getZones() {
        return zones;
    }

    public void setZones(List<ZoneConfig> zones) {
        this.zones = zones;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }



    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }

    public ZonedDateTime getOpenTimeToday() {
        return toZonedToday(openTime);
    }

    public ZonedDateTime getCloseTimeToday() {
        return toZonedToday(closeTime);
    }

    private ZonedDateTime toZonedToday(String time) {
        LocalTime localTime = LocalTime.parse(time);
        return ZonedDateTime.of(LocalDate.now(getZoneId()), localTime, getZoneId());
    }
}