package com.docesforg.bura.server.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.docesforg.bura.server.favorite.FavoriteCityEntity;
import com.docesforg.bura.server.favorite.FavoriteCityRepository;
import com.docesforg.bura.server.signal.RadioSignalTestEntity;
import com.docesforg.bura.server.signal.RadioSignalTestRepository;
import com.docesforg.bura.server.support.SupportMessageRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserStatsControllerTest {
    @Mock FavoriteCityRepository favoriteCityRepository;
    @Mock RadioSignalTestRepository radioSignalTestRepository;
    @Mock SupportMessageRepository supportMessageRepository;

    @Test
    void returnsAggregatedStatsWithSupportRequest() {
        long accountId = 7L;
        when(favoriteCityRepository.findAllByAccountId(accountId))
                .thenReturn(List.of(new FavoriteCityEntity(), new FavoriteCityEntity()));
        when(radioSignalTestRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(new RadioSignalTestEntity()));
        when(supportMessageRepository.existsByAccountId(accountId)).thenReturn(true);

        var service = new UserStatsService(favoriteCityRepository, radioSignalTestRepository, supportMessageRepository);
        var response = service.get(accountId);

        assertEquals(2, response.favorites());
        assertEquals(1, response.radioTests());
        assertEquals(1L, response.supportRequests());
    }

    @Test
    void returnsZeroSupportRequestsWhenConversationMissing() {
        long accountId = 9L;
        when(favoriteCityRepository.findAllByAccountId(accountId)).thenReturn(List.of());
        when(radioSignalTestRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId)).thenReturn(List.of());
        when(supportMessageRepository.existsByAccountId(accountId)).thenReturn(false);

        var service = new UserStatsService(favoriteCityRepository, radioSignalTestRepository, supportMessageRepository);
        var response = service.get(accountId);

        assertEquals(0, response.favorites());
        assertEquals(0, response.radioTests());
        assertEquals(0L, response.supportRequests());
    }
}
