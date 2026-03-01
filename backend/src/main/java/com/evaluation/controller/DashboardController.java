package com.evaluation.controller;

import com.evaluation.dto.DashboardDTO;
import com.evaluation.service.DashboardService;
import com.evaluation.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<?> getStats() {
        DashboardDTO stats = dashboardService.getStats();
        return Result.success(stats);
    }
}
