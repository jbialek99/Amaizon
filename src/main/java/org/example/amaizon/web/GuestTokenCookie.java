package org.example.amaizon.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public final class GuestTokenCookie {

    public static final String COOKIE_NAME = "AMAIZON_GUEST";
    private static final int MAX_AGE = 60 * 60 * 24 * 30;


    private GuestTokenCookie() {
    }

    public static UUID get(HttpServletRequest req) {
        return read(req).orElse(null);
    }

    public static UUID getOrCreate(HttpServletRequest req, HttpServletResponse res) {
        UUID existing = get(req);
        if (existing != null) return existing;

        UUID created = UUID.randomUUID();
        Cookie c = new Cookie(COOKIE_NAME, created.toString());
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge(MAX_AGE);
        res.addCookie(c);
        return created;
    }

    public static void clear(HttpServletResponse res) {
        Cookie c = new Cookie(COOKIE_NAME, "");
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge(0);
        res.addCookie(c);
    }

    public static Optional<UUID> read(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if (cookies == null || cookies.length == 0)
            return Optional.empty();

        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .flatMap(c -> parseUUID(c.getValue()));

    }

    private static Optional<UUID> parseUUID(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
