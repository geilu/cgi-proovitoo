package com.resto.reservation.entity.responseobjects;

import java.time.ZonedDateTime;

public class ReservationResponse {
    private Long id;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    public ReservationResponse(Long id, ZonedDateTime startTime, ZonedDateTime endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }
}
