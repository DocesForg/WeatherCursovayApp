package com.docesforg.bura.server.favorite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FavoriteCityControllerTest {
    @Mock FavoriteCityRepository repository;

    @Test
    void listReturnsMappedDtos() {
        FavoriteCityEntity first = entity(1L, 10L, "Kyiv", 50.45, 30.52);
        FavoriteCityEntity second = entity(2L, 10L, "Lviv", 49.84, 24.03);
        when(repository.findAllByAccountId(10L)).thenReturn(List.of(first, second));

        FavoriteCityController controller = new FavoriteCityController(repository);
        List<FavoriteCityController.FavoriteCityResponse> result = controller.list(10L);

        assertEquals(2, result.size());
        assertEquals("Kyiv", result.get(0).cityName());
        assertEquals(24.03, result.get(1).longitude());
    }

    @Test
    void searchUsesRepositoryFilterAndMapsResult() {
        FavoriteCityEntity match = entity(3L, 12L, "New York", 40.71, -74.0);
        when(repository.findAllByAccountIdAndCityNameContainingIgnoreCase(12L, "new")).thenReturn(List.of(match));

        FavoriteCityController controller = new FavoriteCityController(repository);
        List<FavoriteCityController.FavoriteCityResponse> result = controller.search(12L, "new");

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).id());
        assertEquals("New York", result.get(0).cityName());
    }

    @Test
    void createBuildsEntityAndReturnsSavedDto() {
        when(repository.save(any(FavoriteCityEntity.class))).thenAnswer(invocation -> {
            FavoriteCityEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 99L);
            return saved;
        });

        FavoriteCityController controller = new FavoriteCityController(repository);
        FavoriteCityController.FavoriteCityResponse response = controller.create(
                77L,
                new FavoriteCityController.FavoriteCityRequest("Odesa", 46.48, 30.73)
        );

        assertEquals(99L, response.id());
        assertEquals("Odesa", response.cityName());
        assertEquals(46.48, response.latitude());
        assertEquals(30.73, response.longitude());
    }

    @Test
    void updateChangesExistingFavorite() {
        FavoriteCityEntity existing = entity(15L, 8L, "Old", 10.0, 11.0);
        when(repository.findByIdAndAccountId(15L, 8L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        FavoriteCityController controller = new FavoriteCityController(repository);
        FavoriteCityController.FavoriteCityResponse response = controller.update(
                8L,
                15L,
                new FavoriteCityController.FavoriteCityRequest("Dnipro", 48.46, 35.04)
        );

        assertEquals("Dnipro", existing.getCityName());
        assertEquals(48.46, existing.getLatitude());
        assertEquals(35.04, existing.getLongitude());
        assertEquals("Dnipro", response.cityName());
    }

    @Test
    void updateThrowsNotFoundWhenFavoriteMissing() {
        when(repository.findByIdAndAccountId(15L, 8L)).thenReturn(Optional.empty());
        FavoriteCityController controller = new FavoriteCityController(repository);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.update(8L, 15L, new FavoriteCityController.FavoriteCityRequest("Dnipro", 48.46, 35.04))
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteRemovesExistingFavorite() {
        FavoriteCityEntity existing = entity(21L, 2L, "Paris", 48.85, 2.35);
        when(repository.findByIdAndAccountId(21L, 2L)).thenReturn(Optional.of(existing));

        FavoriteCityController controller = new FavoriteCityController(repository);
        controller.delete(2L, 21L);

        verify(repository).delete(existing);
    }

    private FavoriteCityEntity entity(Long id, long accountId, String cityName, double lat, double lon) {
        FavoriteCityEntity entity = new FavoriteCityEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setAccountId(accountId);
        entity.setCityName(cityName);
        entity.setLatitude(lat);
        entity.setLongitude(lon);
        return entity;
    }
}
