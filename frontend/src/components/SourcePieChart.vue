<template>
  <div class="card chart-card">
    <div class="card-label">来源渠道分布</div>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ data: { type: Array, default: () => [] } })
const chartRef = ref(null)
let chart = null

const colors = ['#43e97b', '#4facfe', '#a18cd1', '#f093fb']

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const pieData = props.data.map((d, i) => ({
    name: d.sourceType,
    value: d.count,
    itemStyle: { color: colors[i % colors.length] }
  }))

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['45%', '70%'],
      center: ['50%', '50%'],
      data: pieData,
      label: { color: '#8899a6', formatter: '{b}\n{d}%' }
    }]
  })
}

onMounted(() => renderChart())
watch(() => props.data, () => renderChart(), { deep: true })
window.addEventListener('resize', () => chart?.resize())
onUnmounted(() => { chart?.dispose(); window.removeEventListener('resize', () => chart?.resize()) })
</script>

<style scoped>
.chart-card { grid-column: span 1; }
.chart { width: 100%; height: 260px; }
</style>
