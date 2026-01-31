package ru.ifmo.highload.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.ifmo.highload.auth.dto.error.HttpErrorResponse;
import ru.ifmo.highload.auth.impl.exceptions.BadRequestException;
import ru.ifmo.highload.auth.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    HttpErrorResponse handleResourceNotFoundException(HttpServletRequest req, ResourceNotFoundException ex) {
        return buildError(req.getRequestURI(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    HttpErrorResponse handleBadRequestException(HttpServletRequest req, BadRequestException ex) {
        return buildError(req.getRequestURI(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    HttpErrorResponse handleAccessDenied(HttpServletRequest req, AccessDeniedException ex) {
        return buildError(req.getRequestURI(), "Доступ запрещён", HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    HttpErrorResponse handleValidationException(HttpServletRequest req, MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce("", (acc, val) -> val + ", " + acc)
                .replaceFirst(", $", "");
        return buildError(req.getRequestURI(), error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    HttpErrorResponse handleTypeMismatch(HttpServletRequest req, MethodArgumentTypeMismatchException ex) {
        String error = "Параметр " + ex.getName() + " имеет некорректный тип";
        return buildError(req.getRequestURI(), error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    HttpErrorResponse handleMessageNotReadable(HttpServletRequest req, HttpMessageNotReadableException ex) {
        return buildError(req.getRequestURI(), ex.getMessage() != null ? ex.getMessage() : "Некорректное тело запроса", HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    HttpErrorResponse handleConstraintViolation(HttpServletRequest req, ConstraintViolationException ex) {
        String error = ex.getConstraintViolations()
                .stream()
                .map((ConstraintViolation<?> cv) -> cv.getPropertyPath() + " " + cv.getMessage())
                .reduce("", (acc, val) -> val + ", " + acc)
                .replaceFirst(", $", "");
        return buildError(req.getRequestURI(), error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    HttpErrorResponse handleGeneric(HttpServletRequest req, Exception ex) {
        ex.printStackTrace();
        return buildError(req.getRequestURI(), "Внутренняя ошибка сервера", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static HttpErrorResponse buildError(String path, String error, HttpStatus status) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setPath(path);
        response.setError(error);
        response.setStatus(status.value());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }
}
