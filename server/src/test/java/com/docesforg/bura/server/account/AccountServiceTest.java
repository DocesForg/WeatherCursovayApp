package com.docesforg.bura.server.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.docesforg.bura.server.account.AccountDtos.LoginRequest;
import com.docesforg.bura.server.account.AccountDtos.RegisterRequest;
import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.security.JwtService;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private UserAccountRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private FavoriteCityRepository favoriteCityRepository;

    @Mock
    private RadioSignalTestRepository radioSignalTestRepository;

    @Mock
    private SupportMessageRepository supportMessageRepository;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(
                repository,
                passwordEncoder,
                jwtService,
                favoriteCityRepository,
                radioSignalTestRepository,
                supportMessageRepository,
                "admin@bura.app"
        );
    }

    @Test
    void registerHashesPasswordAndAssignsUserRole() {
        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        UserAccountEntity saved = new UserAccountEntity();
        saved.setEmail("new@example.com");
        saved.setDisplayName("New User");
        saved.setPasswordHash("hashed");
        saved.setRole("USER");
        when(repository.save(any(UserAccountEntity.class))).thenReturn(saved);
        when(jwtService.createToken(any(), any(), any())).thenReturn("token");

        var response = service.register(new RegisterRequest("new@example.com", "New User", "secret"));

        verify(repository).save(any(UserAccountEntity.class));
        assertEquals("USER", response.account().role());
        assertEquals("token", response.token());
    }

    @Test
    void loginRejectsInvalidPassword() {
        UserAccountEntity account = new UserAccountEntity();
        account.setEmail("user@example.com");
        account.setPasswordHash("hashed");
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.login(new LoginRequest("user@example.com", "wrong"))
        );

        assertTrue(ex.getReason().contains("Invalid password"));
    }

    @Test
    void registerAssignsAdminRoleForConfiguredEmail() {
        when(repository.existsByEmail("admin@bura.app")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        UserAccountEntity saved = new UserAccountEntity();
        saved.setEmail("admin@bura.app");
        saved.setDisplayName("Admin");
        saved.setPasswordHash("hashed");
        saved.setRole("ADMIN");
        when(repository.save(any(UserAccountEntity.class))).thenReturn(saved);
        when(jwtService.createToken(any(), any(), any())).thenReturn("token");

        var response = service.register(new RegisterRequest("admin@bura.app", "Admin", "secret"));

        assertEquals("ADMIN", response.account().role());
    }

    @Test
    void deleteRejectsAdminAccountWithoutExplicitConfirm() {
        UserAccountEntity admin = new UserAccountEntity();
        admin.setRole("ADMIN");
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.delete(1L, false));

        assertEquals(409, ex.getStatusCode().value());
        verifyNoInteractions(favoriteCityRepository, radioSignalTestRepository, supportMessageRepository);
    }

    @Test
    void deleteRemovesUserRelatedDataBeforeAccount() {
        UserAccountEntity user = new UserAccountEntity();
        user.setRole("USER");
        when(repository.findById(7L)).thenReturn(Optional.of(user));

        service.delete(7L, false);

        InOrder order = inOrder(favoriteCityRepository, radioSignalTestRepository, supportMessageRepository, repository);
        order.verify(favoriteCityRepository).deleteAllByAccountId(7L);
        order.verify(radioSignalTestRepository).deleteAllByAccountId(7L);
        order.verify(supportMessageRepository).deleteAllByAccountId(7L);
        order.verify(repository).delete(user);
    }

    @Test
    void deleteAllowsAdminAccountWhenExplicitlyConfirmed() {
        UserAccountEntity admin = new UserAccountEntity();
        admin.setRole("ADMIN");
        when(repository.findById(1L)).thenReturn(Optional.of(admin));

        service.delete(1L, true);

        InOrder order = inOrder(favoriteCityRepository, radioSignalTestRepository, supportMessageRepository, repository);
        order.verify(favoriteCityRepository).deleteAllByAccountId(1L);
        order.verify(radioSignalTestRepository).deleteAllByAccountId(1L);
        order.verify(supportMessageRepository).deleteAllByAccountId(1L);
        order.verify(repository).delete(admin);
    }
}
