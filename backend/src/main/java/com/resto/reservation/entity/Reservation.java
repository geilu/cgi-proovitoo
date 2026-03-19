package com.resto.reservation.entity;


import com.resto.reservation.entity.enums.ReservationStatus;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(name="guest_count", nullable = false)
    private Integer guestCount;

    @Column(name="start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name="end_time", nullable = false)
    private ZonedDateTime endTime;

    @ManyToOne
    @JoinColumn(name="restaurant_table", nullable = false)
    private RestaurantTable restaurantTable;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;



    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public void setRestaurantTable(RestaurantTable restaurantTable) {
        this.restaurantTable = restaurantTable;
    }

    public RestaurantTable getRestaurantTable() {
        return restaurantTable;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
