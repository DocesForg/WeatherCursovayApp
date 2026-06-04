package com.docesforg.bura.server.signal;

import com.docesforg.bura.server.signal.RadioSignalService.RadioSignalResponse;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/accounts/{accountId}/radio-tests")
public class RadioSignalController {
    private final RadioSignalService radioSignalService;

    public RadioSignalController(RadioSignalService radioSignalService) {
        this.radioSignalService = radioSignalService;
    }

    public record RadioSignalRequest(
            @NotBlank String cityA,
            @NotBlank String cityB,
            double latitudeA,
            double longitudeA,
            double latitudeB,
            double longitudeB,
            double frequencyMhz
    ) {
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping
    public List<RadioSignalResponse> history(@PathVariable long accountId) {
        return radioSignalService.history(accountId);
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @PostMapping
    public RadioSignalResponse calculate(@PathVariable long accountId, @RequestBody RadioSignalRequest request) {
        return radioSignalService.calculate(
                accountId,
                request.cityA(),
                request.cityB(),
                request.latitudeA(),
                request.longitudeA(),
                request.latitudeB(),
                request.longitudeB(),
                request.frequencyMhz()
        );
    }
}
