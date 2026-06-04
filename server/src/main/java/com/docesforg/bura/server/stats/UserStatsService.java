package com.docesforg.bura.server.stats;

import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class UserStatsService {
    private final FavoriteCityRepository favoriteCityRepository;
    private final RadioSignalTestRepository radioSignalTestRepository;
    private final SupportMessageRepository supportMessageRepository;

    public UserStatsService(
            FavoriteCityRepository favoriteCityRepository,
            RadioSignalTestRepository radioSignalTestRepository,
            SupportMessageRepository supportMessageRepository
    ) {
        this.favoriteCityRepository = favoriteCityRepository;
        this.radioSignalTestRepository = radioSignalTestRepository;
        this.supportMessageRepository = supportMessageRepository;
    }

    public record StatsResponse(int favorites, int radioTests, long supportRequests) {}

    public StatsResponse get(long accountId) {
        int favorites = favoriteCityRepository.findAllByAccountId(accountId).size();
        int radioTests = radioSignalTestRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId).size();
        long supportRequests = supportMessageRepository.existsByAccountId(accountId) ? 1 : 0;
        return new StatsResponse(favorites, radioTests, supportRequests);
    }
}
