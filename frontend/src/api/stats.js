import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
})

/** 获取全部统计数据 */
export function fetchOverview() {
  return api.get('/stats/overview')
}

/** 获取地区流量 */
export function fetchRegions() {
  return api.get('/stats/regions')
}

/** 获取在线人次 */
export function fetchOnline() {
  return api.get('/stats/online')
}

/** 获取行为分布 */
export function fetchBehaviors() {
  return api.get('/stats/behaviors')
}

/** 获取黑名单 */
export function fetchBlacklist() {
  return api.get('/blacklist')
}

/** 添加黑名单 */
export function addBlacklist(userId, userName, reason) {
  return api.post('/blacklist', { userId, userName, reason })
}

/** 移除黑名单 */
export function removeBlacklist(userId) {
  return api.delete(`/blacklist/${userId}`)
}

export default api
