<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card>
          <div class="muted">{{ item.label }}</div>
          <div class="num">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="14">
        <el-card>
          <template #header>近 7 天订单量</template>
          <div ref="lineRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>订单状态分布</template>
          <div ref="pieRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const lineRef = ref()
const pieRef = ref()
const cards = reactive([
  { label: '用户数', value: 0 },
  { label: '商品数', value: 0 },
  { label: '订单数', value: 0 },
  { label: '已支付销售额', value: 0 }
])

onMounted(async () => {
  const res = await request.get('/api/admin/dashboard')
  const data = res.data
  cards[0].value = data.userCount
  cards[1].value = data.productCount
  cards[2].value = data.orderCount
  cards[3].value = data.totalSales
  const trend = data.orderTrend || []
  const line = echarts.init(lineRef.value)
  line.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trend.map(i => i.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'line', smooth: true, data: trend.map(i => i.count), areaStyle: {} }]
  })
  const statusMap = {
    UNPAID: '未支付',
    PAID: '已支付',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  const pie = echarts.init(pieRef.value)
  pie.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['36%', '64%'],
      data: (data.orderStatus || []).map(i => ({
        name: statusMap[i.status] || i.status,
        value: i.count
      }))
    }]
  })
})
</script>

<style scoped>
.muted { color: #909399; }
.num { font-size: 28px; font-weight: 700; margin-top: 8px; }
</style>
