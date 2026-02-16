package ru.ifmo.highload.file.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.ifmo.highload.file.dto.error.HttpErrorResponse;
import ru.ifmo.highload.file.impl.exceptions.BadRequestException;
import ru.ifmo.highload.file.impl.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    HttpErrorResponse handleResourceNotFoundException(HttpServletRequest req, ResourceNotFoundException ex) {
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
    HttpErrorResponse handleBadRequestException(HttpServletRequest req, BadRequestException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    HttpErrorResponse handleAccessDeniedException(HttpServletRequest req, AccessDeniedException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Доступ запрещён");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
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

        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    HttpErrorResponse handleTypeMismatchException(HttpServletRequest req, MethodArgumentTypeMismatchException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        if (ex.getRequiredType() == null) {
            response.setError("Parameter "
                    + ex.getParameter().getParameterName()
                    + " is not of required type");
        } else {
            response.setError("Parameter "
                    + ex.getParameter().getParameterName()
                    + " must be of type "
                    + ex.getRequiredType().getSimpleName());
        }

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    @ResponseBody
    HttpErrorResponse handleInvalidDataAccessApiUsageException(HttpServletRequest req, InvalidDataAccessApiUsageException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Invalid sort value");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    HttpErrorResponse handleHttpMessageNotReadableException(HttpServletRequest req, HttpMessageNotReadableException ex) {
        HttpErrorResponse response = new HttpErrorResponse();

        if (ex.getRootCause() instanceof DateTimeParseException) {
            response.setError(((DateTimeParseException) ex.getRootCause()).getParsedString()
                    + " is not a valid date");
        } else if (ex.getRootCause() instanceof InvalidFormatException) {
            response.setError(((InvalidFormatException) ex.getRootCause()).getValue()
                    + " is not a valid "
                    + ((InvalidFormatException) ex.getRootCause()).getTargetType().getSimpleName());
        } else {
            response.setError(ex.getMessage());
        }
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    HttpErrorResponse handleConstraintViolationException(HttpServletRequest req, ConstraintViolationException ex) {
        String error = ex.getConstraintViolations()
                .stream()
                .map((ConstraintViolation<?> cv) -> cv.getPropertyPath() + " " + cv.getMessage())
                .reduce("", (acc, val) -> val + ", " + acc)
                .replaceFirst(", $", "");

        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(error);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseBody
    HttpErrorResponse handlePropertyReferenceException(HttpServletRequest req, PropertyReferenceException ex) {
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError(ex.getPropertyName() + " is not a valid property");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    @ResponseBody
    HttpErrorResponse handleIllegalOrNullPointerException(HttpServletRequest req, RuntimeException ex) {
        log.debug("Ошибка валидации запроса: {}", ex.getMessage());
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Некорректный запрос. Проверьте передаваемые параметры.");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    HttpErrorResponse handleGenericException(HttpServletRequest req, Exception ex) {
        log.error("Необработанное исключение при запросе {}: ", req.getRequestURI(), ex);
        HttpErrorResponse response = new HttpErrorResponse();
        response.setError("Некорректный запрос. Запрос не может быть обработан.");
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setPath(req.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());
        return response;
    }
}
