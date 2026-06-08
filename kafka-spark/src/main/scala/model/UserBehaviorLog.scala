package model

/**
 * 用户行为日志数据模型
 * @param userId    用户 ID
 * @param userName  用户姓名
 * @param action    行为类型（browse/click/favorite/purchase/exit/search/share/comment/login/register/add_cart/download）
 * @param page      访问页面
 * @param timestamp 行为时间（yyyy-MM-dd HH:mm:ss）
 * @param region    所在地区
 * @param device    设备类型（mobile/desktop/tablet）
 * @param source    来源渠道（direct/search_engine/social_media/ad）
 * @param duration  页面停留时长（秒）
 * @param userLevel 用户等级（new/regular/vip）
 */
case class UserBehaviorLog(
  userId:    Long,
  userName:  String,
  action:    String,
  page:      String,
  timestamp: String,
  region:    String,
  device:    String,
  source:    String,
  duration:  Int,
  userLevel: String
)
