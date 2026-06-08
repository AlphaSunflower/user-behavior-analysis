package com.group18.mapper;

import com.group18.model.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StatsMapper {

    @Select("SELECT * FROM region_stats ORDER BY visit_count DESC")
    List<RegionStats> getRegionStats();

    @Select("SELECT * FROM online_count WHERE id = 1")
    OnlineCount getOnlineCount();

    @Select("SELECT * FROM behavior_distribution ORDER BY count DESC")
    List<BehaviorDistribution> getBehaviorDistribution();

    @Select("SELECT * FROM blacklist ORDER BY create_time DESC")
    List<BlacklistEntry> getBlacklist();

    @Insert("INSERT INTO blacklist (user_id, user_name, reason, create_time) VALUES (#{userId}, #{userName}, #{reason}, NOW())")
    int addBlacklist(BlacklistEntry entry);

    @Delete("DELETE FROM blacklist WHERE user_id = #{userId}")
    int removeBlacklist(@Param("userId") Long userId);

    @Select("SELECT * FROM blacklist WHERE user_id = #{userId}")
    BlacklistEntry getBlacklistByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM device_stats ORDER BY count DESC")
    List<DeviceStats> getDeviceStats();

    @Select("SELECT * FROM source_stats ORDER BY count DESC")
    List<SourceStats> getSourceStats();

    @Select("SELECT * FROM user_level_stats ORDER BY count DESC")
    List<UserLevelStats> getUserLevelStats();

    @Select("SELECT * FROM user_level_online_stats ORDER BY online_count DESC")
    List<UserLevelOnline> getUserLevelOnline();

    @Select("SELECT COUNT(*) FROM users")
    Long getUserCount();
}
