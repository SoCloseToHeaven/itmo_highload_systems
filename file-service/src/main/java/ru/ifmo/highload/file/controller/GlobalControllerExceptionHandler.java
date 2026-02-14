package ru.ifmo.highload.file.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.ifmo.highload.file.dto.error.HttpErrorResponse;
import ru.ifmo.highload.file.impl.exceptions.BadRequestException;
import ru.ifmo.highload.file.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    HttpErrorResponse handleResourceNotFoundException(HttpServletRequest req, ResourceNotFoundException ex) {
        return buildError(req.getRequestURI(), ex.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    HttpErrorResponse handleBadRequestException(HttpServletRequest req, BadRequestException ex) {
        return buildError(req.getRequestURI(), ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    HttpErrorResponse handleAccessDeniedException(HttpServletRequest req, AccessDeniedException ex) {
        return buildError(req.getRequestURI(), "Access denied", HttpStatus.FORBIDDEN.value());
    }

    private HttpErrorResponse buildError(String path, String error, int status) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setPath(path);
        response.setError(error);
        response.setStatus(status);
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }
}
