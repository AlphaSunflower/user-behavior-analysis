<template>
  <div class="dashboard">
    <!-- 顶部标题栏 -->
    <header class="header">
      <h1>用户实时行为分析平台</h1>
      <div class="header-right">
        <span class="status" :class="{ connected }">
          {{ connected ? '● 实时连接中' : '○ 连接断开' }}
        </span>
        <span class="refresh-info">每 {{ interval / 1000 }} 秒自动刷新</span>
      </div>
    </header>

    <!-- 卡片网格 -->
    <div class="grid">
      <OnlineCount
        :count="data.online?.onlineUsers || 0"
        :updateTime="data.online?.updateTime || ''"
        :levelOnline="data.levelOnline || []"
        :totalUsers="data.totalUsers || 0"
      />

      <div class="card stat-card">
        <div class="card-label">累计访问量</div>
        <div class="big-number-sm">{{ totalVisits }}</div>
      </div>

      <RegionBarChart :data="data.regions || []" />
      <BehaviorPieChart :data="data.behaviors || []" />
      <DeviceBarChart :data="data.devices || []" />
      <SourcePieChart :data="data.sources || []" />
      <BlacklistPanel :list="data.blacklist || []" @refresh="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import OnlineCount from '../components/OnlineCount.vue'
import RegionBarChart from '../components/RegionBarChart.vue'
import BehaviorPieChart from '../components/BehaviorPieChart.vue'
import DeviceBarChart from '../components/DeviceBarChart.vue'
import SourcePieChart from '../components/SourcePieChart.vue'
import BlacklistPanel from '../components/BlacklistPanel.vue'
import { fetchOverview } from '../api/stats.js'

const data = ref({ regions: [], online: {}, behaviors: [], blacklist: [] })
const connected = ref(false)
const interval = ref(5000)
let timer = null

const totalVisits = computed(() =>
  (data.value.regions || []).reduce((sum, r) => sum + (r.visitCount || 0), 0)
)

async function loadData() {
  try {
    const res = await fetchOverview()
    if (res.data.code === 200) {
      data.value = res.data.data
      connected.value = true
    }
  } catch (e) {
    connected.value = false
    console.error('数据加载失败:', e.message)
  }
}

onMounted(() => {
  loadData()
  timer = setInterval(loadData, interval.value)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #1c2936;
}
.header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.header-right { display: flex; gap: 20px; align-items: center; }
.status { font-size: 0.85rem; color: #8899a6; }
.status.connected { color: #43e97b; }
.refresh-info { font-size: 0.8rem; color: #556677; }

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.card {
  background: #15202b;
  border: 1px solid #1c2936;
  border-radius: 12px;
  padding: 20px;
  transition: border-color 0.3s;
}
.card:hover { border-color: #38444d; }

.card-label {
  font-size: 0.85rem;
  color: #8899a6;
  margin-bottom: 10px;
  display: flex; align-items: center; justify-content: space-between;
}

.big-number-sm {
  font-size: 2.4rem;
  font-weight: 700;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.card-footer {
  margin-top: 8px;
  font-size: 0.75rem;
  color: #556677;
}
</style>
