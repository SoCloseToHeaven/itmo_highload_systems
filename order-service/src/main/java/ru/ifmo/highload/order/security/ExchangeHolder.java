package ru.ifmo.highload.order.security;

import org.springframework.web.server.ServerWebExchange;

public final class ExchangeHolder {

    private static final ThreadLocal<ServerWebExchange> HOLDER = new ThreadLocal<>();

    private ExchangeHolder() {
    }

    public static void set(ServerWebExchange exchange) {
        HOLDER.set(exchange);
    }

    public static ServerWebExchange get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
