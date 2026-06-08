<template>
  <div class="card online-card">
    <div class="card-label">实时在线人次 <span class="badge">真人去重</span></div>
    <div class="big-number">{{ animatedCount }}</div>
    <div class="level-row">
      <span class="level-item new">新用户 {{ levelCounts.新用户 }}</span>
      <span class="level-item regular">普通用户 {{ levelCounts.普通用户 }}</span>
      <span class="level-item vip">VIP {{ levelCounts.VIP用户 }}</span>
    </div>
    <div class="card-footer">总用户 {{ totalUsers }} | 更新于 {{ updateTime }}</div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  count: { type: Number, default: 0 },
  updateTime: { type: String, default: '' },
  levelOnline: { type: Array, default: () => [] },
  totalUsers: { type: Number, default: 0 }
})

const animatedCount = ref(0)

const levelCounts = computed(() => {
  const result = { '新用户': 0, '普通用户': 0, 'VIP用户': 0 }
  props.levelOnline.forEach(item => {
    if (item.levelType in result) result[item.levelType] = item.onlineCount
  })
  return result
})

watch(() => props.count, (newVal, oldVal) => {
  const start = oldVal || 0
  const end = newVal || 0
  const duration = 600
  const startTime = Date.now()
  const step = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    animatedCount.value = Math.round(start + (end - start) * progress)
    if (progress < 1) requestAnimationFrame(step)
  }
  step()
}, { immediate: true })
</script>

<style scoped>
.online-card { text-align: center; grid-column: 1 / 3; }
.big-number {
  font-size: 4rem; font-weight: 800;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  line-height: 1.2;
}
.badge {
  font-size: 0.7rem; background: rgba(67,233,123,0.15); color: #43e97b;
  padding: 2px 8px; border-radius: 10px; margin-left: 8px;
}
.level-row {
  display: flex; justify-content: center; gap: 20px; margin-top: 8px;
}
.level-item {
  font-size: 0.9rem; padding: 4px 14px; border-radius: 12px;
}
.level-item.new { background: rgba(79,172,254,0.2); color: #4facfe; }
.level-item.regular { background: rgba(67,233,123,0.2); color: #43e97b; }
.level-item.vip { background: rgba(240,147,251,0.2); color: #f093fb; }
.card-footer { margin-top: 10px; font-size: 0.75rem; color: #556677; }
</style>
