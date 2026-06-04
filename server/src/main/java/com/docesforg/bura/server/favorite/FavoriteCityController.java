package com.docesforg.bura.server.favorite;

import com.docesforg.bura.server.favorite.FavoriteCityService.FavoriteCityResponse;
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

@RestController
@RequestMapping("/api/accounts/{accountId}/favorites")
public class FavoriteCityController {
    private final FavoriteCityService favoriteCityService;

    public FavoriteCityController(FavoriteCityService favoriteCityService) {
        this.favoriteCityService = favoriteCityService;
    }

    public record FavoriteCityRequest(@NotBlank String cityName, double latitude, double longitude) {
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping
    public List<FavoriteCityResponse> list(@PathVariable long accountId) {
        return favoriteCityService.list(accountId);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping("/search")
    public List<FavoriteCityResponse> search(@PathVariable long accountId, @RequestParam String city) {
        return favoriteCityService.search(accountId, city);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteCityResponse create(@PathVariable long accountId, @Valid @RequestBody FavoriteCityRequest request) {
        return favoriteCityService.create(accountId, request.cityName(), request.latitude(), request.longitude());
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PutMapping("/{favoriteId}")
    public FavoriteCityResponse update(
            @PathVariable long accountId,
            @PathVariable long favoriteId,
            @Valid @RequestBody FavoriteCityRequest request
    ) {
        return favoriteCityService.update(accountId, favoriteId, request.cityName(), request.latitude(), request.longitude());
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @DeleteMapping("/{favoriteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long accountId, @PathVariable long favoriteId) {
        favoriteCityService.delete(accountId, favoriteId);
    }
}
