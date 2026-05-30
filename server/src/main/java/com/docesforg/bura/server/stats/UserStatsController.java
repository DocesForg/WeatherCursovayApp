package com.docesforg.bura.server.stats;

import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/accounts/{accountId}/stats")
public class UserStatsController {
    private final FavoriteCityRepository favoriteCityRepository;
    private final RadioSignalTestRepository radioSignalTestRepository;
    private final SupportMessageRepository supportMessageRepository;

    public UserStatsController(
            FavoriteCityRepository favoriteCityRepository,
            RadioSignalTestRepository radioSignalTestRepository,
            SupportMessageRepository supportMessageRepository
    ) {
        this.favoriteCityRepository = favoriteCityRepository;
        this.radioSignalTestRepository = radioSignalTestRepository;
        this.supportMessageRepository = supportMessageRepository;
    }

    public record StatsResponse(int favorites, int radioTests, long supportRequests) {}

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping
    public StatsResponse get(@PathVariable long accountId) {
        int favorites = favoriteCityRepository.findAllByAccountId(accountId).size();
        int radioTests = radioSignalTestRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId).size();
        long supportRequests = supportMessageRepository.existsByAccountId(accountId) ? 1 : 0;
        return new StatsResponse(favorites, radioTests, supportRequests);
    }
}
