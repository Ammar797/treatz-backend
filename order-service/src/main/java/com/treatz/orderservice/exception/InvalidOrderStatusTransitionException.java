package com.treatz.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// This annotation is very powerful. It tells Spring that whenever this exception
// is thrown and not caught by our GlobalExceptionHandler, it should automatically
// result in a "400 Bad Request" HTTP error.
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}