package com.docesforg.bura.server.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.docesforg.bura.server.account.UserAccountEntity;
import com.docesforg.bura.server.account.UserAccountRepository;
import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock UserAccountRepository userAccountRepository;
    @Mock FavoriteCityRepository favoriteCityRepository;
    @Mock RadioSignalTestRepository radioSignalTestRepository;
    @Mock SupportMessageRepository supportMessageRepository;

    @Test
    void updateRoleUpdatesAccountWhenRoleIsValid() {
        long accountId = 15L;
        UserAccountEntity account = new UserAccountEntity();
        ReflectionTestUtils.setField(account, "id", accountId);
        account.setEmail("user@example.com");
        account.setDisplayName("User");
        account.setRole("USER");

        when(userAccountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userAccountRepository.save(account)).thenReturn(account);

        AdminService service = new AdminService(userAccountRepository, favoriteCityRepository, radioSignalTestRepository, supportMessageRepository);
        AdminService.AccountAdminView response = service.updateRole(accountId, " admin ");

        assertEquals("ADMIN", account.getRole());
        assertEquals("ADMIN", response.role());
        assertEquals(accountId, response.id());
    }

    @Test
    void updateRoleReturnsBadRequestForUnsupportedRole() {
        AdminService service = new AdminService(userAccountRepository, favoriteCityRepository, radioSignalTestRepository, supportMessageRepository);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateRole(1L, "manager"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateRoleReturnsNotFoundWhenAccountMissing() {
        when(userAccountRepository.findById(101L)).thenReturn(Optional.empty());
        AdminService service = new AdminService(userAccountRepository, favoriteCityRepository, radioSignalTestRepository, supportMessageRepository);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateRole(101L, "USER"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
