package producer

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import util.KafkaConfig
import scala.util.Random

/**
 * Kafka 生产者 — 模拟用户行为日志生成
 *
 * 运行方式：
 *   sbt "runMain producer.KafkaProducerApp"
 */
object KafkaProducerApp {

  // 模拟数据池
  private val actions = Array(
    "浏览", "点击", "收藏", "购买", "退出",
    "搜索", "分享", "评论", "登录", "注册",
    "加购", "下载"
  )
  private val regions = Array(
    "北京", "上海", "广东", "深圳",
    "杭州", "成都", "武汉", "南京",
    "天津", "重庆", "苏州", "青岛",
    "大连", "厦门", "长沙", "郑州",
    "昆明", "济南", "合肥", "福州"
  )
  private val devices  = Array("手机", "电脑", "平板")
  private val sources  = Array("直接访问", "搜索引擎", "社交媒体", "广告投放")
  private val levels   = Array("新用户", "普通用户", "VIP用户")
  private val pages = Array(
    "index.html", "product-detail.html", "search.html",
    "checkout.html", "user-center.html"
  )
  private val surnames = Array(
    "张", "李", "王", "赵", "孙", "周", "吴", "郑", "刘", "陈"
  )
  private val givenNames = Array(
    "伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "洋", "勇",
    "艳", "杰", "娟", "涛", "明", "超", "秀英", "华", "慧", "鑫"
  )

  def main(args: Array[String]): Unit = {
    println("=" * 60)
    println(s"[KafkaProducer] 启动中...")
    println(s"[KafkaProducer] 目标: ${KafkaConfig.BOOTSTRAP_SERVERS}")
    println(s"[KafkaProducer] Topic: ${KafkaConfig.TOPIC_USER_BEHAVIOR}")
    println("=" * 60)

    val props   = KafkaConfig.getProducerParams()
    val producer = new KafkaProducer[String, String](props)
    val random   = new Random()
    var msgCount = 0L

    try {
      while (true) {
        // 随机生成一条用户行为日志
        val userId   = 10001 + random.nextInt(100)
        val userName = surnames(random.nextInt(surnames.length)) +
                       givenNames(random.nextInt(givenNames.length))
        val action   = actions(random.nextInt(actions.length))
        val page     = pages(random.nextInt(pages.length))
        val region   = regions(random.nextInt(regions.length))
        val device   = devices(random.nextInt(devices.length))
        val source   = sources(random.nextInt(sources.length))
        val duration = 5 + random.nextInt(595)     // 5~600 秒
        val level    = levels(random.nextInt(levels.length))
        val timestamp = java.time.LocalDateTime.now()
          .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // Tab 分隔的日志行（10 字段）
        val logLine = s"$userId\t$userName\t$action\t$page\t$timestamp\t$region\t$device\t$source\t$duration\t$level"

        // 发送到 Kafka
        val record = new ProducerRecord[String, String](
          KafkaConfig.TOPIC_USER_BEHAVIOR,
          null,  // key 为空，均匀分布到各分区
          logLine
        )
        producer.send(record, (metadata, exception) => {
          if (exception != null) {
            System.err.println(s"[KafkaProducer] 发送失败: ${exception.getMessage}")
          }
        })

        msgCount += 1
        if (msgCount % 100 == 0) {
          println(s"[KafkaProducer] 已发送 $msgCount 条消息 | 最新: $logLine")
        }

        // 间隔 200~500ms，模拟真实流量
        Thread.sleep(200 + random.nextInt(300))
      }
    } catch {
      case e: Exception =>
        System.err.println(s"[KafkaProducer] 异常: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      producer.close()
      println(s"[KafkaProducer] 已关闭，共发送 $msgCount 条消息")
    }
  }
}
