package ru.ifmo.highload.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.ifmo.highload.dto.error.HttpErrorResponse;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    HttpErrorResponse
    handleResourceNotFoundException(HttpServletRequest req, ResourceNotFoundException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    HttpErrorResponse
    handleBadRequestException(HttpServletRequest req, BadRequestException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    HttpErrorResponse
    handleValidationException(HttpServletRequest req, MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce("", (acc, val) -> val + ", " + acc)
                .replaceAll(", $", "");

        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
