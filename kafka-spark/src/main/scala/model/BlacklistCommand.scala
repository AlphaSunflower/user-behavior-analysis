package model

import com.alibaba.fastjson.{JSON, JSONObject}

/**
 * 黑名单操作指令
 * @param `type`  操作类型（add/remove）
 * @param userId  用户 ID
 * @param reason  拉黑原因（仅 add 时需要）
 */
case class BlacklistCommand(
  `type`:  String,   // "add" 或 "remove"
  userId:  Long,
  reason:  String = ""
) {
  def toJson: String = {
    val json = new JSONObject()
    json.put("type", `type`)
    json.put("userId", userId)
    json.put("reason", reason)
    json.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
    json.toJSONString
  }
}

object BlacklistCommand {
  def fromJson(jsonStr: String): Option[BlacklistCommand] = {
    try {
      val json = JSON.parseObject(jsonStr)
      Some(BlacklistCommand(
        `type` = json.getString("type"),
        userId = json.getLong("userId"),
        reason = if (json.containsKey("reason")) json.getString("reason") else ""
      ))
    } catch {
      case _: Exception => None
    }
  }
}
