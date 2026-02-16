package ru.ifmo.highload.file.dto.error;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class HttpErrorResponse {
    private int status;
    private String path;
    private String error;
    private ZonedDateTime timestamp;
}
