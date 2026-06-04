package com.docesforg.bura.server.admin;

import com.docesforg.bura.server.account.AccountRole;
import com.docesforg.bura.server.account.UserAccountEntity;
import com.docesforg.bura.server.account.UserAccountRepository;
import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {
    private final UserAccountRepository userAccountRepository;
    private final FavoriteCityRepository favoriteCityRepository;
    private final RadioSignalTestRepository radioSignalTestRepository;
    private final SupportMessageRepository supportMessageRepository;

    public AdminService(
            UserAccountRepository userAccountRepository,
            FavoriteCityRepository favoriteCityRepository,
            RadioSignalTestRepository radioSignalTestRepository,
            SupportMessageRepository supportMessageRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.favoriteCityRepository = favoriteCityRepository;
        this.radioSignalTestRepository = radioSignalTestRepository;
        this.supportMessageRepository = supportMessageRepository;
    }

    public record DashboardResponse(long users, long admins, long favorites, long radioTests, long supportRequests) {
    }

    public record AccountAdminView(Long id, String email, String displayName, String role) {
    }

    public DashboardResponse dashboard() {
        return new DashboardResponse(
                userAccountRepository.count(),
                userAccountRepository.countByRole(AccountRole.ADMIN.name()),
                favoriteCityRepository.count(),
                radioSignalTestRepository.count(),
                supportMessageRepository.countDistinctAccountId()
        );
    }

    public List<AccountAdminView> accounts() {
        return userAccountRepository.findAll()
                .stream()
                .map(this::toAccountAdminView)
                .toList();
    }

    public AccountAdminView updateRole(long accountId, String requestedRole) {
        AccountRole role = parseRole(requestedRole);
        UserAccountEntity account = userAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        account.setRole(role.name());
        return toAccountAdminView(userAccountRepository.save(account));
    }

    private AccountRole parseRole(String requestedRole) {
        try {
            return AccountRole.valueOf(requestedRole.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supported roles: USER, ADMIN");
        }
    }

    private AccountAdminView toAccountAdminView(UserAccountEntity account) {
        return new AccountAdminView(account.getId(), account.getEmail(), account.getDisplayName(), account.getRole());
    }
}
