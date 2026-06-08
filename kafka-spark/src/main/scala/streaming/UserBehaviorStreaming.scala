package streaming

import com.alibaba.fastjson.JSON
import model.{BlacklistCommand, UserBehaviorLog}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import util.{KafkaConfig, MySQLConnectionPool}

/**
 * Spark Streaming 实时计算主程序
 *
 * 功能：
 *   1. 消费 Kafka user_behavior 和 user_blacklist 主题
 *   2. 数据清洗（空值、格式异常、字段缺失）
 *   3. 黑名单实时拦截
 *   4. 四大业务计算：地区流量、1分钟在线人次、行为分布
 *   5. 结果批量写入 MySQL
 *
 * 运行方式（本地）：
 *   sbt "runMain streaming.UserBehaviorStreaming"
 *
 * 运行方式（集群）：
 *   spark-submit --class streaming.UserBehaviorStreaming \
 *     --master yarn --deploy-mode cluster \
 *     target/scala-2.12/UserBehaviorAnalysis-assembly-1.0.jar
 */
object UserBehaviorStreaming {

  // 有效行为类型（12 种）
  private val validActions = Set(
    "浏览", "点击", "收藏", "购买", "退出",
    "搜索", "分享", "评论", "登录", "注册",
    "加购", "下载"
  )

  def main(args: Array[String]): Unit = {
    println("=" * 60)
    println(s"[Streaming] 启动中...")
    println(s"[Streaming] Kafka: ${KafkaConfig.BOOTSTRAP_SERVERS}")
    println(s"[Streaming] 消费 Topic: ${KafkaConfig.TOPIC_USER_BEHAVIOR}")
    println(s"[Streaming] 黑名单 Topic: ${KafkaConfig.TOPIC_BLACKLIST}")
    println("=" * 60)

    // ========== 1. 初始化 SparkConf 和 StreamingContext ==========
    val conf = new SparkConf()
      .setAppName("UserBehaviorStreaming")
      .setMaster("local[*]")                                            // 本地模式，生产改为 yarn
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.streaming.stopGracefullyOnShutdown", "true")          // 优雅停止
      .set("spark.sql.adaptive.enabled", "true")

    val ssc = new StreamingContext(conf, Seconds(10))                   // 微批次间隔 10 秒

    // ========== 2. 检查点配置（用于窗口操作恢复） ==========
    ssc.checkpoint("./checkpoint")

    // ========== 3. 创建 Kafka DStream ==========
    // 行为日志流
    val behaviorStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](
        Set(KafkaConfig.TOPIC_USER_BEHAVIOR),
        KafkaConfig.getConsumerParams()
      )
    )

    // 黑名单指令流
    val blacklistStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](
        Set(KafkaConfig.TOPIC_BLACKLIST),
        KafkaConfig.getConsumerParams("blacklist_group")
      )
    )

    // ========== 4. 黑名单实时维护 ==========
    // 提取日志的消息体
    val logLines = behaviorStream.map(_.value())

    // 异步维护黑名单集合（本批次立即生效）
    var blacklistSet = scala.collection.mutable.Set[Long]()
    blacklistStream.foreachRDD { rdd =>
      val cmds = rdd.collect()
      cmds.foreach { record =>
        BlacklistCommand.fromJson(record.value()) match {
          case Some(cmd) =>
            cmd.`type` match {
              case "add" =>
                blacklistSet += cmd.userId
                println(s"[Blacklist] + 已添加黑名单: userId=${cmd.userId}, reason=${cmd.reason}")
              case "remove" =>
                blacklistSet -= cmd.userId
                println(s"[Blacklist] - 已移除黑名单: userId=${cmd.userId}")
              case _ =>
                println(s"[Blacklist] 未知操作类型: ${cmd.`type`}")
            }
            println(s"[Blacklist] 当前黑名单: ${blacklistSet.mkString("{", ", ", "}")}")
          case None =>
            println(s"[Blacklist] 解析失败: ${record.value()}")
        }
      }
    }

    // ========== 5. 日志解析与清洗 ==========
    val cleanedStream = logLines
      .map(parseLine)                  // String → Option[UserBehaviorLog]
      .filter(_.isDefined)             // 过滤解析失败
      .map(_.get)                      // 提取有效记录
      .filter(log => validActions.contains(log.action))   // 过滤无效行为
      .filter(log => !blacklistSet.contains(log.userId))  // 黑名单拦截

    // 打印清洗后的数据（调试用）
    cleanedStream.print(5)

    // ========== 6. 四大业务计算 ==========

    // ---- 6.1 地区访问流量统计 ----
    val regionStats = cleanedStream
      .map(log => (log.region, 1L))
      .reduceByKey(_ + _)

    regionStats.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        if (partition.nonEmpty) {
          val conn = MySQLConnectionPool.getConnection
          try {
            val sql =
              """INSERT INTO region_stats (region, visit_count, update_time)
                |VALUES (?, ?, NOW())
                |ON DUPLICATE KEY UPDATE
                |  visit_count = visit_count + VALUES(visit_count),
                |  update_time = NOW()""".stripMargin
            val stmt = conn.prepareStatement(sql)
            partition.foreach { case (region, count) =>
              stmt.setString(1, region)
              stmt.setLong(2, count)
              stmt.addBatch()
            }
            stmt.executeBatch()
            stmt.close()
            println(s"[MySQL] region_stats: 写入 ${partition.size} 条")
          } finally {
            MySQLConnectionPool.closeConnection(conn)
          }
        }
      }
    }

    // ---- 6.2 1 分钟实时在线人次（滑动窗口：窗口 60s，滑动 10s） ----
    val onlineCount = cleanedStream
      .map(log => (log.userId, 1L))
      .reduceByKeyAndWindow(
        (a: Long, b: Long) => a + b,    // 窗口内相同 userId 计数
        (a: Long, b: Long) => a - b,    // 逆向操作（离开窗口的数据）
        Minutes(1),                      // 窗口长度 60 秒
        Seconds(10)                      // 滑动间隔 10 秒
      )
      .map(_._1)                         // 只保留 userId
      .count()                           // 统计去重后的用户数（实际上不去重，统计活跃记录数）

    onlineCount.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        if (partition.nonEmpty) {
          val conn = MySQLConnectionPool.getConnection
          try {
            val sql =
              """INSERT INTO online_count (id, online_users, update_time)
                |VALUES (1, ?, NOW())
                |ON DUPLICATE KEY UPDATE
                |  online_users = VALUES(online_users),
                |  update_time = NOW()""".stripMargin
            val stmt = conn.prepareStatement(sql)
            partition.foreach { count =>
              stmt.setLong(1, count)
              stmt.addBatch()
            }
            stmt.executeBatch()
            stmt.close()
            println(s"[MySQL] online_count: 写入成功")
          } finally {
            MySQLConnectionPool.closeConnection(conn)
          }
        }
      }
    }

    // ---- 6.3 用户行为分布统计 ----
    val behaviorDistribution = cleanedStream
      .map(log => (log.action, 1L))
      .reduceByKey(_ + _)

    behaviorDistribution.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        if (partition.nonEmpty) {
          val conn = MySQLConnectionPool.getConnection
          try {
            val sql =
              """INSERT INTO behavior_distribution (action_type, count, update_time)
                |VALUES (?, ?, NOW())
                |ON DUPLICATE KEY UPDATE
                |  count = count + VALUES(count),
                |  update_time = NOW()""".stripMargin
            val stmt = conn.prepareStatement(sql)
            partition.foreach { case (action, count) =>
              stmt.setString(1, action)
              stmt.setLong(2, count)
              stmt.addBatch()
            }
            stmt.executeBatch()
            stmt.close()
            println(s"[MySQL] behavior_distribution: 写入 ${partition.size} 条")
          } finally {
            MySQLConnectionPool.closeConnection(conn)
          }
        }
      }
    }

    // ---- 6.4 设备类型分布统计 ----
    val deviceStats = cleanedStream
      .map(log => (log.device, 1L))
      .reduceByKey(_ + _)

    deviceStats.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        if (partition.nonEmpty) {
          val conn = MySQLConnectionPool.getConnection
          try {
            val sql =
              """INSERT INTO device_stats (device_type, count, update_time)
                |VALUES (?, ?, NOW())
                |ON DUPLICATE KEY UPDATE
                |  count = count + VALUES(count),
                |  update_time = NOW()""".stripMargin
            val stmt = conn.prepareStatement(sql)
            partition.foreach { case (device, count) =>
              stmt.setString(1, device)
              stmt.setLong(2, count)
              stmt.addBatch()
            }
            stmt.executeBatch()
            stmt.close()
            println(s"[MySQL] device_stats: 写入 ${partition.size} 条")
          } finally {
            MySQLConnectionPool.closeConnection(conn)
          }
        }
      }
    }

    // ---- 6.5 来源渠道分布统计 ----
    val sourceStats = cleanedStream
      .map(log => (log.source, 1L))
      .reduceByKey(_ + _)

    sourceStats.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        if (partition.nonEmpty) {
          val conn = MySQLConnectionPool.getConnection
          try {
            val sql =
              """INSERT INTO source_stats (source_type, count, update_time)
                |VALUES (?, ?, NOW())
                |ON DUPLICATE KEY UPDATE
                |  count = count + VALUES(count),
                |  update_time = NOW()""".stripMargin
            val stmt = conn.prepareStatement(sql)
            partition.foreach { case (source, count) =>
              stmt.setString(1, source)
              stmt.setLong(2, count)
              stmt.addBatch()
            }
            stmt.executeBatch()
            stmt.close()
            println(s"[MySQL] source_stats: 写入 ${partition.size} 条")
          } finally {
            MySQLConnectionPool.closeConnection(conn)
          }
        }
      }
    }

    // ---- 6.6 用户等级分布统计 ----
    val levelStats = cleanedStream
      .map(log => (log.userLevel, 1L))
      .reduceByKey(_ + _)

    levelStats.foreachRDD { rdd =>
      rdd.foreachPartition { partition =>
        if (partition.nonEmpty) {
          val conn = MySQLConnectionPool.getConnection
          try {
            val sql =
              """INSERT INTO user_level_stats (level_type, count, update_time)
                |VALUES (?, ?, NOW())
                |ON DUPLICATE KEY UPDATE
                |  count = count + VALUES(count),
                |  update_time = NOW()""".stripMargin
            val stmt = conn.prepareStatement(sql)
            partition.foreach { case (level, count) =>
              stmt.setString(1, level)
              stmt.setLong(2, count)
              stmt.addBatch()
            }
            stmt.executeBatch()
            stmt.close()
            println(s"[MySQL] user_level_stats: 写入 ${partition.size} 条")
          } finally {
            MySQLConnectionPool.closeConnection(conn)
          }
        }
      }
    }

    // ========== 7. 启动流计算 ==========
    ssc.start()
    println("[Streaming] 流计算已启动，等待数据...")
    ssc.awaitTermination()
  }

  // ==================== 辅助方法 ====================

  /**
   * 解析 Tab 分隔的日志行（10 字段）
   * 格式: userId\tuserName\taction\tpage\ttimestamp\tregion\tdevice\tsource\tduration\tuserLevel
   */
  def parseLine(line: String): Option[UserBehaviorLog] = {
    try {
      val parts = line.split("\t")
      if (parts.length != 10) return None

      val userId    = parts(0).toLong
      val userName  = parts(1).trim
      val action    = parts(2).trim
      val page      = parts(3).trim
      val timestamp = parts(4).trim
      val region    = parts(5).trim
      val device    = parts(6).trim
      val source    = parts(7).trim
      val duration  = parts(8).toInt
      val userLevel = parts(9).trim

      // 空值校验
      if (userName.isEmpty || action.isEmpty || region.isEmpty || device.isEmpty || source.isEmpty || userLevel.isEmpty)
        return None

      Some(UserBehaviorLog(userId, userName, action, page, timestamp, region, device, source, duration, userLevel))
    } catch {
      case _: NumberFormatException => None   // userId/duration 不是数字
      case _: Exception => None
    }
  }
}
