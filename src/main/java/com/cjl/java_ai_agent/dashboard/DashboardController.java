package com.cjl.java_ai_agent.dashboard;

import com.cjl.java_ai_agent.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverview>> overview() {
        return ResponseEntity.ok(ApiResponse.success(service.overview()));
    }
}
