package ru.ifmo.highload.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.ifmo.highload.dto.error.HttpErrorResponse;
import ru.ifmo.highload.impl.exceptions.BadRequestException;
import ru.ifmo.highload.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

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
        response.setTimestamp(ZonedDateTime.now());
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
        response.setTimestamp(ZonedDateTime.now());
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
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    @ResponseBody
    HttpErrorResponse
    handleInvalidDataAccessApiUsageException(HttpServletRequest req, InvalidDataAccessApiUsageException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Невалидное значение sort");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    HttpErrorResponse
    handleInvalidDataAccessApiUsageException(HttpServletRequest req, HttpMessageNotReadableException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        // TODO: WHAT THE FUCK DO WE DO HERE?????????
//        if (ex.getRootCause() instanceof DateTimeParseException) {
//            response.setError("Неверный формат даты");
//        } else {
//            response.setError("Неправильно сформирован JSON");
//        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }
}
