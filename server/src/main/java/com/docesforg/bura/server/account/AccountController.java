package com.docesforg.bura.server.account;

import com.docesforg.bura.server.account.AccountDtos.AuthResponse;
import com.docesforg.bura.server.account.AccountDtos.LoginRequest;
import com.docesforg.bura.server.account.AccountDtos.RegisterRequest;
import com.docesforg.bura.server.account.AccountDtos.UpdateNameRequest;
import com.docesforg.bura.server.account.AccountDtos.UpdatePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return accountService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping("/accounts/{accountId}")
    public AccountDtos.AccountResponse get(@PathVariable long accountId) {
        return accountService.get(accountId);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PatchMapping("/accounts/{accountId}/name")
    public AccountDtos.AccountResponse updateName(
            @PathVariable long accountId,
            @Valid @RequestBody UpdateNameRequest request
    ) {
        return accountService.updateName(accountId, request.displayName());
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PatchMapping("/accounts/{accountId}/password")
    public void updatePassword(
            @PathVariable long accountId,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        accountService.updatePassword(accountId, request.password());
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @DeleteMapping("/accounts/{accountId}")
    public void delete(
            @PathVariable long accountId,
            @RequestParam(defaultValue = "false") boolean allowAdminDelete
    ) {
        accountService.delete(accountId, allowAdminDelete);
    }
}
