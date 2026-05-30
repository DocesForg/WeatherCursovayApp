package com.docesforg.bura.server.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void createAndParseToken() {
        JwtService service = new JwtService("12345678901234567890123456789012", 3600);

        String token = service.createToken(7L, "test@example.com", "USER");

        var claims = service.parseToken(token);
        assertEquals("test@example.com", claims.getSubject());
        assertEquals(7L, claims.get("uid", Long.class));
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void parseRejectsInvalidToken() {
        JwtService service = new JwtService("12345678901234567890123456789012", 3600);

        assertThrows(IllegalArgumentException.class, () -> service.parseToken("broken-token"));
    }
}
