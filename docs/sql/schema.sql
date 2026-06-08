-- ============================================
-- 用户实时行为分析系统 — 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS user_behavior_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE user_behavior_db;

-- 表1：地区访问量统计
CREATE TABLE IF NOT EXISTS region_stats (
    region      VARCHAR(50)  NOT NULL PRIMARY KEY COMMENT '地区名称',
    visit_count BIGINT       NOT NULL DEFAULT 0 COMMENT '累计访问次数',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区实时访问量统计';

-- 表2：实时在线人次（单行表，始终 id=1）
CREATE TABLE IF NOT EXISTS online_count (
    id           INT          NOT NULL PRIMARY KEY DEFAULT 1 COMMENT '固定主键',
    online_users BIGINT       NOT NULL DEFAULT 0 COMMENT '1分钟内在线人次',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1分钟实时在线人次';

-- 表3：用户行为分布统计
CREATE TABLE IF NOT EXISTS behavior_distribution (
    action_type VARCHAR(50)  NOT NULL PRIMARY KEY COMMENT '行为类型(browse/click/favorite/purchase/exit)',
    count       BIGINT       NOT NULL DEFAULT 0 COMMENT '累计次数',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为类型分布统计';

-- 表4：黑名单管理表
CREATE TABLE IF NOT EXISTS blacklist (
    user_id      BIGINT       NOT NULL PRIMARY KEY COMMENT '被拉黑的用户ID',
    user_name    VARCHAR(50)  DEFAULT '' COMMENT '用户姓名',
    reason       VARCHAR(255) DEFAULT '' COMMENT '拉黑原因',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单用户表';

-- 表5：设备类型分布统计
CREATE TABLE IF NOT EXISTS device_stats (
    device_type VARCHAR(50)  NOT NULL PRIMARY KEY COMMENT '设备类型(mobile/desktop/tablet)',
    count       BIGINT       NOT NULL DEFAULT 0 COMMENT '累计次数',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备类型分布统计';

-- 表6：来源渠道分布统计
CREATE TABLE IF NOT EXISTS source_stats (
    source_type VARCHAR(50)  NOT NULL PRIMARY KEY COMMENT '来源渠道(direct/search_engine/social_media/ad)',
    count       BIGINT       NOT NULL DEFAULT 0 COMMENT '累计次数',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来源渠道分布统计';

-- 表7：用户等级分布统计
CREATE TABLE IF NOT EXISTS user_level_stats (
    level_type  VARCHAR(50)  NOT NULL PRIMARY KEY COMMENT '用户等级(new/regular/vip)',
    count       BIGINT       NOT NULL DEFAULT 0 COMMENT '累计次数',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级分布统计';

-- 初始化行为分布数据（12 种行为类型）
INSERT INTO behavior_distribution (action_type, count) VALUES
('浏览',   0), ('点击',   0), ('收藏',   0), ('购买',   0),
('退出',   0), ('搜索',   0), ('分享',   0), ('评论',   0),
('登录',   0), ('注册',   0), ('加购',   0), ('下载',   0)
ON DUPLICATE KEY UPDATE action_type=action_type;

-- 初始化在线人数记录
INSERT INTO online_count (id, online_users) VALUES (1, 0)
ON DUPLICATE KEY UPDATE id=id;
