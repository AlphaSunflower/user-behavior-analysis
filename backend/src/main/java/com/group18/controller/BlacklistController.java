package com.group18.controller;

import com.group18.model.ApiResponse;
import com.group18.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    private final StatsService statsService;

    public BlacklistController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 查询所有黑名单 */
    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(statsService.getBlacklist());
    }

    /** 添加黑名单 */
    @PostMapping
    public ApiResponse<?> add(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String userName = (String) body.getOrDefault("userName", "");
        String reason = (String) body.getOrDefault("reason", "");
        return statsService.addBlacklist(userId, userName, reason);
    }

    /** 移除黑名单 */
    @DeleteMapping("/{userId}")
    public ApiResponse<?> remove(@PathVariable Long userId) {
        return statsService.removeBlacklist(userId);
    }
}
