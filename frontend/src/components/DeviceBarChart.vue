<template>
  <div class="card chart-card">
    <div class="card-label">设备类型分布</div>
    <div ref="chartRef" class="chart"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ data: { type: Array, default: () => [] } })
const chartRef = ref(null)
let chart = null

const deviceLabels = { mobile: '手机', desktop: '电脑', tablet: '平板' }
const deviceColors = { mobile: '#43e97b', desktop: '#4facfe', tablet: '#a18cd1' }

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const names  = props.data.map(d => deviceLabels[d.deviceType] || d.deviceType)
  const counts = props.data.map(d => d.count)
  const colors = props.data.map(d => deviceColors[d.deviceType] || '#8899a6')

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '5%', bottom: '5%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category', data: names,
      axisLabel: { color: '#8899a6' },
      axisLine: { lineStyle: { color: '#38444d' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#8899a6' },
      splitLine: { lineStyle: { color: '#1c2936' } }
    },
    series: [{
      type: 'bar', data: counts,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: (params) => colors[params.dataIndex]
      }
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
