<template>
  <div class="card online-card">
    <div class="card-label">实时在线人次 <span class="badge">1分钟窗口</span></div>
    <div class="big-number">{{ animatedCount }}</div>
    <div class="card-footer">更新于 {{ updateTime }}</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  count: { type: Number, default: 0 },
  updateTime: { type: String, default: '' }
})

const animatedCount = ref(0)

// 数字动画效果
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
</style>
