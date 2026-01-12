package ru.ifmo.highload.price.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import ru.ifmo.highload.price.dto.error.HttpErrorResponse;
import ru.ifmo.highload.price.impl.exceptions.BadRequestException;
import ru.ifmo.highload.price.impl.exceptions.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleResourceNotFoundException(ResourceNotFoundException ex, org.springframework.web.server.ServerWebExchange exchange) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleBadRequestException(BadRequestException ex, org.springframework.web.server.ServerWebExchange exchange) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleValidationException(WebExchangeBindException ex, org.springframework.web.server.ServerWebExchange exchange) {
        String error = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce("", (acc, val) -> val + ", " + acc)
                .replaceFirst(", $", "");

        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleTypeMismatchException(ServerWebInputException ex, org.springframework.web.server.ServerWebExchange exchange) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleConstraintViolationException(ConstraintViolationException ex, org.springframework.web.server.ServerWebExchange exchange) {
        String error = ex.getConstraintViolations()
                .stream()
                .map((ConstraintViolation<?> cv) -> cv.getPropertyPath() + " " + cv.getMessage())
                .reduce("", (acc, val) -> val + ", " + acc)
                .replaceFirst(", $", "");

        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }
}

