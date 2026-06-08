package util

/**
 * Kafka 连接配置
 * 开发环境：连接远程 Kafka 服务器
 * 生产环境：同上（Spark 任务提交到集群后直接访问）
 */
object KafkaConfig {
  // Kafka 集群地址
  val BOOTSTRAP_SERVERS = "192.168.100.140:9092"

  // 主题名称
  val TOPIC_USER_BEHAVIOR = "user_behavior"
  val TOPIC_BLACKLIST     = "user_blacklist"

  // 消费者组 ID
  val CONSUMER_GROUP = "user_behavior_group"

  // Kafka 消费者参数
  def getConsumerParams(groupId: String = CONSUMER_GROUP): Map[String, Object] = Map(
    "bootstrap.servers"  -> BOOTSTRAP_SERVERS,
    "key.deserializer"   -> "org.apache.kafka.common.serialization.StringDeserializer",
    "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
    "group.id"           -> groupId,
    "auto.offset.reset"  -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean),
    "client.dns.lookup" -> "use_all_dns_ips"
  )

  // Kafka 生产者参数
  def getProducerParams(): java.util.Properties = {
    val props = new java.util.Properties()
    props.put("bootstrap.servers", BOOTSTRAP_SERVERS)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "1")
    props.put("retries", "3")
    props
  }
}
