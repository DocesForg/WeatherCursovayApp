package com.docesforg.bura.server.favorite;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteCityRepository extends JpaRepository<FavoriteCityEntity, Long> {
    List<FavoriteCityEntity> findAllByAccountId(long accountId);
    List<FavoriteCityEntity> findAllByAccountIdAndCityNameContainingIgnoreCase(long accountId, String cityName);
    Optional<FavoriteCityEntity> findByIdAndAccountId(long id, long accountId);
    void deleteAllByAccountId(long accountId);
}
