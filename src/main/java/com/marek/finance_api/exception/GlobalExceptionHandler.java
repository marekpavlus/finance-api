package com.marek.finance_api.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;


import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<String> handleResourceNotFound(
      ResourceNotFoundException exception)
  {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(exception.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(
      IllegalArgumentException exception)
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      MethodArgumentNotValidException exception)
  {
    Map<String, String> errors = new LinkedHashMap<>();

    exception.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", 400);
    response.put("message", "Validation failed");
    response.put("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<String> handleOptimisticLocking(
      OptimisticLockingFailureException exception)
  {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body("Resource was modified by another operation. Please refresh and try again.");
  }

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<String> handleResourceConflict(
      ResourceConflictException exception)
  {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(exception.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataIntegrityViolation(
      DataIntegrityViolationException exception)
  {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body("Operation conflicts with existing data.");
  }
}