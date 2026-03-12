package com.devbookmark.dashboard;

import com.devbookmark.dashboard.dto.DashboardResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return dashboardService.getDashboard(me);
    }
}