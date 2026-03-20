package com.resto.reservation.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RestaurantException extends RuntimeException {
    public RestaurantException(String message) {
        super(message);
    }
}
