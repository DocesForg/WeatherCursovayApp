package com.docesforg.bura.server.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AccountDtos {
    private AccountDtos() {
    }

    public record RegisterRequest(
            @Email String email,
            @NotBlank String displayName,
            @NotBlank String password
    ) {
    }

    public record LoginRequest(
            @Email String email,
            @NotBlank String password
    ) {
    }

    public record UpdateNameRequest(@NotBlank String displayName) {
    }

    public record UpdatePasswordRequest(@NotBlank String password) {
    }

    public record AccountResponse(Long id, String email, String displayName, String role) {
    }

    public record AuthResponse(String token, AccountResponse account) {
    }
}
