package model

/**
 * 用户行为日志数据模型
 * @param userId    用户 ID
 * @param userName  用户姓名
 * @param action    行为类型（browse/click/favorite/purchase/exit）
 * @param page      访问页面
 * @param timestamp 行为时间（yyyy-MM-dd HH:mm:ss）
 * @param region    所在地区
 */
case class UserBehaviorLog(
  userId:    Long,
  userName:  String,
  action:    String,
  page:      String,
  timestamp: String,
  region:    String
)
