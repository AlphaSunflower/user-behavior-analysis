<template>
  <div class="card chart-card">
    <div class="card-label">用户行为分布</div>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chart = null

const colors = ['#43e97b', '#38f9d7', '#4facfe', '#a18cd1', '#ff6b6b',
  '#f093fb', '#f5576c', '#4ecdc4', '#ffe66d', '#a8e6cf',
  '#ffd3b6', '#c3cfe2']

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const pieData = props.data.map((d, i) => ({
    name: d.actionType,
    value: d.count,
    itemStyle: { color: colors[i % colors.length] }
  }))

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['50%', '75%'],
      center: ['50%', '50%'],
      data: pieData,
      label: { color: '#8899a6', formatter: '{b}\n{d}%' },
      emphasis: {
        label: { fontSize: 18, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0,0,0,0.5)' }
      }
    }]
  })
}

onMounted(() => renderChart())
watch(() => props.data, () => renderChart(), { deep: true })

window.addEventListener('resize', () => chart?.resize())
onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})
</script>

<style scoped>
.chart-card { grid-column: span 2; }
.chart { width: 100%; height: 320px; }
</style>
