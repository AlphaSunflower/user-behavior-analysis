<template>
  <div class="card chart-card">
    <div class="card-label">地区访问流量</div>
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

function renderChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const regions = props.data.map(d => d.region)
  const counts  = props.data.map(d => d.visitCount)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '8%', bottom: '5%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category', data: regions,
      axisLabel: { color: '#8899a6', rotate: 30 },
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
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#43e97b' },
          { offset: 1, color: '#1a7a3a' }
        ])
      },
      emphasis: {
        itemStyle: { color: '#38f9d7' }
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
