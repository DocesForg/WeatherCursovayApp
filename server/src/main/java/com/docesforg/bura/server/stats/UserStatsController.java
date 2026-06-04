package com.docesforg.bura.server.stats;

import com.docesforg.bura.server.stats.UserStatsService.StatsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/accounts/{accountId}/stats")
public class UserStatsController {
    private final UserStatsService userStatsService;

    public UserStatsController(UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
    }

    @PreAuthorize("@accountAccess.canAccess(#accountId, authentication)")
    @GetMapping
    public StatsResponse get(@PathVariable long accountId) {
        return userStatsService.get(accountId);
    }
}
