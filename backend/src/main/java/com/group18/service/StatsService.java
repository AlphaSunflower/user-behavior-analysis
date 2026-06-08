package com.group18.service;

import com.group18.mapper.StatsMapper;
import com.group18.model.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {

    private final StatsMapper statsMapper;
    private final KafkaProducer<String, String> kafkaProducer;

    @Value("${kafka.blacklist-topic}")
    private String blacklistTopic;

    public StatsService(StatsMapper statsMapper, KafkaProducer<String, String> kafkaProducer) {
        this.statsMapper = statsMapper;
        this.kafkaProducer = kafkaProducer;
    }

    public List<RegionStats> getRegionStats() {
        return statsMapper.getRegionStats();
    }

    public OnlineCount getOnlineCount() {
        return statsMapper.getOnlineCount();
    }

    public List<BehaviorDistribution> getBehaviorDistribution() {
        return statsMapper.getBehaviorDistribution();
    }

    /** 一次返回所有统计数据 */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("regions", statsMapper.getRegionStats());
        overview.put("online", statsMapper.getOnlineCount());
        overview.put("behaviors", statsMapper.getBehaviorDistribution());
        overview.put("blacklist", statsMapper.getBlacklist());
        return overview;
    }

    // ---- 黑名单管理 ----

    public List<BlacklistEntry> getBlacklist() {
        return statsMapper.getBlacklist();
    }

    public ApiResponse<?> addBlacklist(Long userId, String userName, String reason) {
        // 1. 写入 MySQL
        BlacklistEntry entry = new BlacklistEntry();
        entry.setUserId(userId);
        entry.setUserName(userName != null ? userName : "");
        entry.setReason(reason != null ? reason : "");
        statsMapper.addBlacklist(entry);

        // 2. 发送 Kafka 指令（Spark Streaming 实时消费）
        String json = String.format(
            "{\"type\":\"add\",\"userId\":%d,\"reason\":\"%s\",\"timestamp\":\"%s\"}",
            userId,
            reason != null ? reason : "",
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        kafkaProducer.send(new ProducerRecord<>(blacklistTopic, json));

        return ApiResponse.success(null);
    }

    public ApiResponse<?> removeBlacklist(Long userId) {
        statsMapper.removeBlacklist(userId);

        // 发送 Kafka 移除指令
        String json = String.format(
            "{\"type\":\"remove\",\"userId\":%d,\"timestamp\":\"%s\"}",
            userId,
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        kafkaProducer.send(new ProducerRecord<>(blacklistTopic, json));

        return ApiResponse.success(null);
    }
}
