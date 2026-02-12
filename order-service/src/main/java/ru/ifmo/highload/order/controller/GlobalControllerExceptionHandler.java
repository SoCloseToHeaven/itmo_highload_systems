package ru.ifmo.highload.order.controller;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.ifmo.highload.order.dto.error.HttpErrorResponse;
import ru.ifmo.highload.order.impl.exceptions.BadRequestException;
import ru.ifmo.highload.order.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Slf4j
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

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleAccessDeniedException(AccessDeniedException ex, org.springframework.web.server.ServerWebExchange exchange) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Доступ запрещён");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(response));
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

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleIllegalStateException(IllegalStateException ex, org.springframework.web.server.ServerWebExchange exchange) {
        HttpErrorResponse response = new HttpErrorResponse();
        if (ex.getMessage() != null && ex.getMessage().contains("Pageable")) {
            response.setError("Некорректные параметры пагинации. Используйте параметры page и size.");
        } else {
            response.setError("Некорректный запрос. Проверьте передаваемые параметры.");
        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    public Mono<ResponseEntity<HttpErrorResponse>> handleIllegalOrNullPointerException(RuntimeException ex, org.springframework.web.server.ServerWebExchange exchange) {
        log.debug("Ошибка валидации запроса: {}", ex.getMessage());
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Некорректный запрос. Проверьте передаваемые параметры.");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<HttpErrorResponse>> handleGenericException(Exception ex, org.springframework.web.server.ServerWebExchange exchange) {
        log.error("Необработанное исключение при запросе {}: ", exchange.getRequest().getURI().getPath(), ex);
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Некорректный запрос. Запрос не может быть обработан.");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(exchange.getRequest().getURI().getPath());
        response.setTimestamp(ZonedDateTime.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }
}

