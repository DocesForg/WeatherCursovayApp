package com.docesforg.bura.server.favorite;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/accounts/{accountId}/favorites")
public class FavoriteCityController {
    private final FavoriteCityRepository repository;

    public FavoriteCityController(FavoriteCityRepository repository) {
        this.repository = repository;
    }

    public record FavoriteCityRequest(@NotBlank String cityName, double latitude, double longitude) {
    }

    public record FavoriteCityResponse(Long id, String cityName, double latitude, double longitude) {
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping
    public List<FavoriteCityResponse> list(@PathVariable long accountId) {
        return repository.findAllByAccountId(accountId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping("/search")
    public List<FavoriteCityResponse> search(@PathVariable long accountId, @RequestParam String city) {
        return repository.findAllByAccountIdAndCityNameContainingIgnoreCase(accountId, city)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteCityResponse create(@PathVariable long accountId, @Valid @RequestBody FavoriteCityRequest request) {
        FavoriteCityEntity entity = new FavoriteCityEntity();
        entity.setAccountId(accountId);
        entity.setCityName(request.cityName());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        return toDto(repository.save(entity));
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PutMapping("/{favoriteId}")
    public FavoriteCityResponse update(
            @PathVariable long accountId,
            @PathVariable long favoriteId,
            @Valid @RequestBody FavoriteCityRequest request
    ) {
        FavoriteCityEntity entity = repository.findByIdAndAccountId(favoriteId, accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite city not found"));
        entity.setCityName(request.cityName());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        return toDto(repository.save(entity));
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @DeleteMapping("/{favoriteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long accountId, @PathVariable long favoriteId) {
        FavoriteCityEntity entity = repository.findByIdAndAccountId(favoriteId, accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite city not found"));
        repository.delete(entity);
    }

    private FavoriteCityResponse toDto(FavoriteCityEntity entity) {
        return new FavoriteCityResponse(entity.getId(), entity.getCityName(), entity.getLatitude(), entity.getLongitude());
    }
}
