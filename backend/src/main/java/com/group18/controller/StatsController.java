package com.group18.controller;

import com.group18.model.ApiResponse;
import com.group18.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 一次返回所有统计数据 */
    @GetMapping("/stats/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(statsService.getOverview());
    }

    /** 地区流量统计 */
    @GetMapping("/stats/regions")
    public ApiResponse<?> regions() {
        return ApiResponse.success(statsService.getRegionStats());
    }

    /** 实时在线人次 */
    @GetMapping("/stats/online")
    public ApiResponse<?> online() {
        return ApiResponse.success(statsService.getOnlineCount());
    }

    /** 行为分布统计 */
    @GetMapping("/stats/behaviors")
    public ApiResponse<?> behaviors() {
        return ApiResponse.success(statsService.getBehaviorDistribution());
    }
}
