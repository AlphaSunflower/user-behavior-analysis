# CLAUDE.md

对话以中文的形式回答我。
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**UserBehaviorAnalysis** — 基于 Kafka + Spark Streaming 的用户实时行为日志分析与可视化系统。
广东理工学院《大数据技术应用综合实践一》课程项目，组号 18。

详细实现方案见：[实现方案.md](./实现方案.md)

---

## 环境路径

### 开发机（Windows）

| 组件 | 路径 |
|------|------|
| JDK 8 | `E:\Development\jdk1.8.0_442` |
| JDK 17 | `E:\jdk17`（JAVA_HOME） |
| SBT | `E:\Development\sbt-1.10.11` |
| Maven | `E:\Development\apache-maven-3.9.15` |
| Scala（PATH） | `D:\Program Files (x86)\scala\bin` |
| Node.js | `D:\Users\Nodejs` |
| MySQL 8.0 | `D:\mysql-8.0.31-winx64` |
| Hadoop 3.1.0 | `E:\Development\hadoop-3.1.0`（HADOOP_HOME） |

### 服务器（Linux, 192.168.100.140）

| 组件 | 版本 |
|------|------|
| Kafka | 2.6.0，端口 9092 |
| Spark | 3.4.0（YARN 模式） |
| Hadoop | 3.3.5 |
| MySQL | 5.7.44 |
| Nginx | 1.26.1 |

### 数据库

| 环境 | 连接信息 |
|------|----------|
| 开发 MySQL | `localhost:3306`，root / 123456，库 `user_behavior_db` |
| 服务器 MySQL | `192.168.100.140:3306`，root / 123456，库 `user_behavior_db` |
| Kafka | `192.168.100.140:9092` |

---

## 项目结构（4 模块单体代码库）

```
group-18/
├── kafka-producer/                 # 模块一：Kafka 生产者，独立 SBT 项目（可与 kafka-spark 同时运行）
│   ├── build.sbt                   # 仅依赖 kafka-clients 2.6.0
│   └── src/main/scala/producer/
│       └── KafkaProducerApp.scala  # 独立的生产者，所有配置和数据池内联
│
├── kafka-spark/                    # 模块二：Spark Streaming，独立 SBT 项目
│   ├── build.sbt
│   ├── project/
│   │   ├── plugins.sbt             # sbt-assembly 2.1.5
│   │   └── build.properties        # sbt 1.9.0
│   └── src/main/scala/
│       ├── streaming/UserBehaviorStreaming.scala   # Spark Streaming 主程序：消费、清洗、计算、写入 MySQL
│       ├── model/UserBehaviorLog.scala             # 日志 case class
│       ├── model/BlacklistCommand.scala            # 黑名单指令 case class
│       ├── util/KafkaConfig.scala                  # Kafka 连接配置（192.168.100.140:9092）
│       ├── util/MySQLConnectionPool.scala          # MySQL 连接池（开发/生产切换）
│       └── util/DateUtils.scala                   # 时间工具
│
├── backend/                        # 模块二：SpringBoot 3 + JDK 17 + Maven
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/group18/
│       │   ├── BackendApplication.java
│       │   ├── config/CorsConfig.java
│       │   ├── config/KafkaProducerConfig.java
│       │   ├── controller/StatsController.java     # /api/stats/*
│       │   ├── controller/BlacklistController.java # /api/blacklist CRUD
│       │   ├── service/StatsService.java
│       │   ├── mapper/StatsMapper.java             # MyBatis 注解方式
│       │   └── model/
│       │       ├── RegionStats.java
│       │       ├── OnlineCount.java
│       │       ├── BehaviorDistribution.java
│       │       ├── BlacklistEntry.java
│       │       └── ApiResponse.java
│       └── resources/application.yml
│
├── frontend/                       # 模块三：Vue 3 + ECharts + Vite
│   ├── package.json
│   ├── vite.config.js              # /api → :8080 代理
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js
│       ├── api/stats.js            # axios 封装
│       ├── views/Dashboard.vue     # 主仪表盘
│       └── components/
│           ├── OnlineCount.vue     # 实时在线人次（数字动画）
│           ├── RegionBarChart.vue  # 地区流量柱状图（ECharts）
│           ├── BehaviorPieChart.vue # 行为分布饼图（ECharts）
│           └── BlacklistPanel.vue  # 黑名单管理面板
│
├── docs/sql/schema.sql             # 建库建表脚本（已执行）
├── .gitignore
└── 实现方案.md
```

---

## 构建与运行

### kafka-producer 模块（独立 SBT 项目，可与 kafka-spark 同时运行）

```bash
cd kafka-producer

# 编译
sbt compile

# 运行 Kafka Producer（模拟日志生成，发送到 192.168.100.140:9092）
sbt "runMain producer.KafkaProducerApp"
```

### kafka-spark 模块（Spark Streaming，独立 SBT 项目）

```bash
cd kafka-spark

# 编译
sbt compile

# 运行 Spark Streaming（本地模式，消费 Kafka + 写入本地 MySQL）
sbt "runMain streaming.UserBehaviorStreaming"

# 打包 fat JAR（部署到服务器）
sbt assembly
# 输出：target/scala-2.12/UserBehaviorAnalysis-assembly-1.0.jar

# 部署到服务器后提交
spark-submit --class streaming.UserBehaviorStreaming --master yarn --deploy-mode cluster \
  target/scala-2.12/UserBehaviorAnalysis-assembly-1.0.jar
```

### backend 模块（SpringBoot/Maven）

```bash
cd backend

# 编译
mvn compile

# 启动（端口 8080）
mvn spring-boot:run

# 打包
mvn package -DskipTests
```

### frontend 模块（Vue 3/Vite）

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（端口 5173，自动代理 /api → :8080）
npm run dev

# 生产构建（输出 dist/）
npm run build
```

---

## 全链路启动顺序

```bash
# 打开 4 个终端，各自 cd 到不同目录（不再有 SBT 锁冲突）

# 终端 1：Spark Streaming（先启动，消费 latest offset）
cd kafka-spark && sbt "runMain streaming.UserBehaviorStreaming"

# 终端 2：Kafka Producer（等 Streaming 就绪后启动）
cd kafka-producer && sbt "runMain producer.KafkaProducerApp"

# 终端 3：SpringBoot API
cd backend && mvn spring-boot:run

# 终端 4：Vue 前端
cd frontend && npm run dev
# 浏览器打开 http://localhost:5173
```

---

## 关键实现细节

- **Spark Streaming 批次间隔**：10 秒
- **Kafka 消费策略**：`latest`（生产）/ `earliest`（测试）
- **数据清洗规则**：空值、字段缺失（≠6）、行为类型不在枚举范围、黑名单用户 → 全部丢弃
- **1 分钟在线人次**：`reduceByKeyAndWindow`，窗口 60s，滑动 10s
- **MySQL 写入**：`INSERT ... ON DUPLICATE KEY UPDATE`，地区/行为分布累加，在线人次替换
- **黑名单同步**：SpringBoot → Kafka（`user_blacklist` topic）→ Spark Streaming 消费 → 更新内存 Set
- **JDK 17 兼容**：`build.sbt` 中 `javaOptions` 已配置 `--add-opens` 开放 Spark 所需的 JDK 内部模块
- **本地开发**：`build.sbt` 中 spark-streaming 设为 `compile`，部署到服务器时改回 `provided`
- **WinUtils**：`E:\Development\hadoop-3.1.0\bin\winutils.exe` 已就位，HADOOP_HOME 已设置

## Kafka Topic

| Topic | 分区 | 用途 |
|-------|------|------|
| `user_behavior` | 3 | 用户行为日志（Tab 分隔 6 字段） |
| `user_blacklist` | 1 | 黑名单指令（JSON：add/remove） |

## REST API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/overview` | 全部统计（regions + online + behaviors + blacklist） |
| GET | `/api/stats/regions` | 地区流量 |
| GET | `/api/stats/online` | 实时在线 |
| GET | `/api/stats/behaviors` | 行为分布 |
| GET | `/api/blacklist` | 黑名单列表 |
| POST | `/api/blacklist` | 添加黑名单（写入 MySQL + 发送 Kafka） |
| DELETE | `/api/blacklist/{userId}` | 移除黑名单 |
