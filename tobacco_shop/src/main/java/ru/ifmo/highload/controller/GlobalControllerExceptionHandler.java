package ru.ifmo.highload.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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
    @ResponseBody HttpErrorResponse
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
    @ResponseBody HttpErrorResponse
    handleBadRequestException(HttpServletRequest req, BadRequestException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
