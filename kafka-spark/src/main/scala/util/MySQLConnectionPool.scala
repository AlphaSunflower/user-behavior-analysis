package util

import java.sql.{Connection, DriverManager}
import java.util.Properties

/**
 * MySQL 连接工具
 * 开发环境：连接本地 MySQL 8.0
 * 生产环境：连接服务器 MySQL 5.7
 */
object MySQLConnectionPool {

  // 开发环境 MySQL 配置
  private val DEV_URL      = "jdbc:mysql://localhost:3306/user_behavior_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8"
  private val DEV_USER     = "root"
  private val DEV_PASSWORD = "123456"

  // 生产环境 MySQL 配置（服务器 MySQL 5.7）
  private val PROD_URL      = "jdbc:mysql://192.168.100.140:3306/user_behavior_db?useSSL=false&characterEncoding=utf8"
  private val PROD_USER     = "root"
  private val PROD_PASSWORD = "123456"

  // 当前环境标志（true=开发, false=生产）
  private var isDev = true

  /** 切换到生产环境 */
  def useProd(): Unit = { isDev = false }

  /** 切换到开发环境 */
  def useDev(): Unit = { isDev = true }

  /** 获取数据库连接 */
  def getConnection: Connection = {
    // 确保驱动已加载
    Class.forName("com.mysql.jdbc.Driver")

    val (url, user, password) = if (isDev) {
      (DEV_URL, DEV_USER, DEV_PASSWORD)
    } else {
      (PROD_URL, PROD_USER, PROD_PASSWORD)
    }

    DriverManager.getConnection(url, user, password)
  }

  /** 关闭数据库连接 */
  def closeConnection(conn: Connection): Unit = {
    if (conn != null && !conn.isClosed) {
      try { conn.close() } catch { case _: Exception => }
    }
  }
}
