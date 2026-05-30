package com.docesforg.bura.server.account;

import com.docesforg.bura.server.account.AccountDtos.AccountResponse;
import com.docesforg.bura.server.account.AccountDtos.AuthResponse;
import com.docesforg.bura.server.account.AccountDtos.LoginRequest;
import com.docesforg.bura.server.account.AccountDtos.RegisterRequest;
import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.security.JwtService;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FavoriteCityRepository favoriteCityRepository;
    private final RadioSignalTestRepository radioSignalTestRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final Set<String> adminEmails;

    public AccountService(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            FavoriteCityRepository favoriteCityRepository,
            RadioSignalTestRepository radioSignalTestRepository,
            SupportMessageRepository supportMessageRepository,
            @Value("${app.admin.emails:}") String adminEmails
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.favoriteCityRepository = favoriteCityRepository;
        this.radioSignalTestRepository = radioSignalTestRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .map(it -> it.toLowerCase(Locale.ROOT))
                .filter(it -> !it.isBlank())
                .collect(Collectors.toSet());
    }

    public AuthResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        UserAccountEntity account = new UserAccountEntity();
        account.setEmail(request.email());
        account.setDisplayName(request.displayName());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(resolveRole(request.email()).name());
        UserAccountEntity saved = repository.save(account);
        return toAuth(saved);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccountEntity account = repository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        AccountRole expectedRole = resolveRole(account.getEmail());
        if (!expectedRole.name().equals(account.getRole())) {
            account.setRole(expectedRole.name());
            account = repository.save(account);
        }
        return toAuth(account);
    }

    public AccountResponse get(long accountId) {
        UserAccountEntity account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return toDto(account);
    }

    public AccountResponse updateName(long accountId, String displayName) {
        UserAccountEntity account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setDisplayName(displayName);
        return toDto(repository.save(account));
    }

    public void updatePassword(long accountId, String password) {
        UserAccountEntity account = repository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setPasswordHash(passwordEncoder.encode(password));
        repository.save(account);
    }

    @Transactional
    public void delete(long accountId, boolean allowAdminDelete) {
        UserAccountEntity account = repository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (AccountRole.ADMIN.name().equals(account.getRole()) && !allowAdminDelete) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Admin deletion requires allowAdminDelete=true"
            );
        }
        favoriteCityRepository.deleteAllByAccountId(accountId);
        radioSignalTestRepository.deleteAllByAccountId(accountId);
        supportMessageRepository.deleteAllByAccountId(accountId);
        repository.delete(account);
    }

    private AuthResponse toAuth(UserAccountEntity account) {
        return new AuthResponse(
                jwtService.createToken(account.getId(), account.getEmail(), account.getRole()),
                toDto(account)
        );
    }

    private AccountResponse toDto(UserAccountEntity account) {
        return new AccountResponse(account.getId(), account.getEmail(), account.getDisplayName(), account.getRole());
    }

    private AccountRole resolveRole(String email) {
        return adminEmails.contains(email.toLowerCase(Locale.ROOT))
                ? AccountRole.ADMIN
                : AccountRole.USER;
    }
}
