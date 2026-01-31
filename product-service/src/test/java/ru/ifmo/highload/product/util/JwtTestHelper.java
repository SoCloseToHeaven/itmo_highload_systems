package ru.ifmo.highload.product.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public final class JwtTestHelper {

    private static final String ROLES_CLAIM = "roles";
    private static final String USER_ID_CLAIM = "userId";
    private static final long EXPIRATION_MS = 3600000L;

    private JwtTestHelper() {
    }

    public static String token(String secret, Long userId, String username, String... roles) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .subject(username)
                .claim(USER_ID_CLAIM, userId)
                .claim(ROLES_CLAIM, List.of(roles))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
