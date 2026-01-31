package ru.ifmo.highload.order.security;

/**
 * Хранит JWT токен текущего запроса для передачи в Feign-вызовы к другим сервисам.
 */
public final class JwtContextHolder {

    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    private JwtContextHolder() {
    }

    public static void setToken(String token) {
        TOKEN_HOLDER.set(token);
    }

    public static String getToken() {
        return TOKEN_HOLDER.get();
    }

    public static void clear() {
        TOKEN_HOLDER.remove();
    }
}
