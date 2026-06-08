package util

import java.text.SimpleDateFormat
import java.util.Date

/**
 * 时间处理工具
 */
object DateUtils {

  private val datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private val dateFormat     = new SimpleDateFormat("yyyy-MM-dd")

  /** 获取当前日期时间字符串 */
  def now(): String = datetimeFormat.format(new Date())

  /** 获取当前日期字符串 */
  def today(): String = dateFormat.format(new Date())

  /** 时间戳转日期字符串 */
  def formatTimestamp(ts: Long): String = datetimeFormat.format(new Date(ts))
}
