package ru.ifmo.highload.order.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CurrentUser {

    private final Long userId;
    private final String username;
    private final List<String> roles;
}
