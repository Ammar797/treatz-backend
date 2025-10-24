package com.treatz.restaurantservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.RESTAURANT_NOT_FOUND.getCode())
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MenuItemNotBelongsToRestaurantException.class)
    public ResponseEntity<ErrorResponse> handleMenuItemNotBelongsToRestaurant(
            MenuItemNotBelongsToRestaurantException ex,
            HttpServletRequest request) {
        log.warn("Menu item validation failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.MENU_ITEM_NOT_BELONGS_TO_RESTAURANT.getCode())
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INVALID_INPUT.getCode())
                .message("Validation failed for one or more fields")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INVALID_INPUT.getCode())
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.ACCESS_DENIED.getCode())
                .message("You do not have permission to access this resource")
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity constraint violation";
        String code = ErrorCode.INVALID_INPUT.getCode();

        // Check for duplicate email constraint
        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            message = "A restaurant with this email already exists";
            code = ErrorCode.DUPLICATE_RESTAURANT_EMAIL.getCode();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(code)
                .message(message)
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());

        String message = "Malformed JSON request";
        String errorMsg = ex.getMessage();

        // Check for unrecognized field (typo in field name)
        if (errorMsg != null && errorMsg.contains("Unrecognized field")) {
            // Extract field name from error message
            // Format: "JSON parse error: Unrecognized field \"fieldName\""
            int start = errorMsg.indexOf("\"");
            int end = errorMsg.indexOf("\"", start + 1);
            if (start != -1 && end != -1) {
                String fieldName = errorMsg.substring(start + 1, end);
                message = "Unknown field '" + fieldName + "'. Please check your request body for typos in field names.";
            }
        }
        // Check for enum conversion errors
        else if (errorMsg != null && errorMsg.contains("MenuCategory")) {
            message = "Invalid category. Allowed values: STARTER, MAIN_COURSE, DESSERT, BEVERAGE, SIDE_DISH, APPETIZER";
        }
        // Check for type mismatch (e.g., string instead of number)
        else if (errorMsg != null && (errorMsg.contains("Cannot deserialize") || errorMsg.contains("not a valid"))) {
            message = "Invalid data type in request body. Please check that all fields have the correct type (e.g., numbers for price, boolean for available).";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INVALID_INPUT.getCode())
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INVALID_INPUT.getCode())
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("An unexpected error occurred. Please try again later.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}