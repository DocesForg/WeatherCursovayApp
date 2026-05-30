package com.docesforg.bura.server.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AccountAccessEvaluatorTest {

    private final AccountAccessEvaluator evaluator = new AccountAccessEvaluator();

    @Test
    void returnsFalseWhenAuthenticationMissing() {
        assertFalse(evaluator.canAccess(5L, null));
    }

    @Test
    void returnsFalseWhenPrincipalMissing() {
        var auth = new UsernamePasswordAuthenticationToken(null, "n/a", List.of());
        assertFalse(evaluator.canAccess(5L, auth));
    }

    @Test
    void allowsAdminRegardlessOfAccountId() {
        var auth = new UsernamePasswordAuthenticationToken(
                2L,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        assertTrue(evaluator.canAccess(999L, auth));
    }

    @Test
    void allowsOnlyMatchingAccountForRegularUser() {
        var auth = new UsernamePasswordAuthenticationToken(
                42L,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        assertTrue(evaluator.canAccess(42L, auth));
        assertFalse(evaluator.canAccess(43L, auth));
    }
}
