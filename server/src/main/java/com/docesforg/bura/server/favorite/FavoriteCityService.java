package com.docesforg.bura.server.favorite;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteCityService {
    private final FavoriteCityRepository repository;

    public FavoriteCityService(FavoriteCityRepository repository) {
        this.repository = repository;
    }

    public record FavoriteCityResponse(Long id, String cityName, double latitude, double longitude) {
    }

    public List<FavoriteCityResponse> list(long accountId) {
        return repository.findAllByAccountId(accountId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<FavoriteCityResponse> search(long accountId, String city) {
        return repository.findAllByAccountIdAndCityNameContainingIgnoreCase(accountId, city)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public FavoriteCityResponse create(long accountId, String cityName, double latitude, double longitude) {
        FavoriteCityEntity entity = new FavoriteCityEntity();
        entity.setAccountId(accountId);
        entity.setCityName(cityName);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        return toDto(repository.save(entity));
    }

    public FavoriteCityResponse update(long accountId, long favoriteId, String cityName, double latitude, double longitude) {
        FavoriteCityEntity entity = findFavorite(accountId, favoriteId);
        entity.setCityName(cityName);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        return toDto(repository.save(entity));
    }

    public void delete(long accountId, long favoriteId) {
        repository.delete(findFavorite(accountId, favoriteId));
    }

    private FavoriteCityEntity findFavorite(long accountId, long favoriteId) {
        return repository.findByIdAndAccountId(favoriteId, accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite city not found"));
    }

    private FavoriteCityResponse toDto(FavoriteCityEntity entity) {
        return new FavoriteCityResponse(entity.getId(), entity.getCityName(), entity.getLatitude(), entity.getLongitude());
    }
}
