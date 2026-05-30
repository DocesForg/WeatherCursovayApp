package com.docesforg.bura.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountAccess")
public class AccountAccessEvaluator {
    public boolean canAccess(long accountId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(it -> "ROLE_ADMIN".equals(it.getAuthority()));
        if (isAdmin) {
            return true;
        }
        return accountId == ((Long) authentication.getPrincipal());
    }
}
