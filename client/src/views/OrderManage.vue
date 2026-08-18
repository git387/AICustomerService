<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="订单号" style="width:200px" />
      <el-select v-model="status" clearable placeholder="状态" style="width:140px">
        <el-option label="未支付" value="UNPAID" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="table.records" stripe>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="totalAmount" label="金额" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="address" label="地址" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="changeStatus(row, 'SHIPPED')" v-if="row.status==='PAID'">发货</el-button>
          <el-button link type="success" @click="changeStatus(row, 'COMPLETED')" v-if="row.status==='SHIPPED'">完成</el-button>
          <el-button link type="danger" @click="changeStatus(row, 'CANCELLED')" v-if="row.status==='UNPAID'">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top:16px"
      v-model:current-page="page"
      :total="table.total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
    <el-dialog v-model="visible" title="订单详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥ {{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detail.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(detail.payTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付宝交易号" :span="2">{{ detail.alipayTradeNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <h4 class="block-title">商品明细</h4>
      <el-table :data="detail.items || []" stripe>
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.productImage"
              :src="row.productImage"
              :preview-src-list="[row.productImage]"
              preview-teleported
              fit="cover"
              class="thumb"
            />
            <span v-else class="no-img">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">¥ {{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="110">
          <template #default="{ row }">¥ {{ ((Number(row.price) || 0) * (Number(row.quantity) || 0)).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const STATUS_TEXT = {
  UNPAID: '未支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}
const STATUS_TAG = {
  UNPAID: 'warning',
  PAID: 'primary',
  SHIPPED: '',
  COMPLETED: 'success',
  CANCELLED: 'info'
}

const keyword = ref('')
const status = ref('')
const page = ref(1)
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const detail = reactive({
  orderNo: '',
  status: '',
  username: '',
  totalAmount: 0,
  receiverName: '',
  receiverPhone: '',
  address: '',
  createTime: '',
  payTime: '',
  alipayTradeNo: '',
  items: []
})

const statusText = (value) => STATUS_TEXT[value] || value || '-'
const statusTag = (value) => STATUS_TAG[value] || 'info'
const formatTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 19)
}

const load = async () => {
  const res = await request.get('/api/admin/orders', {
    params: { page: page.value, size: 10, keyword: keyword.value, status: status.value }
  })
  table.records = res.data.records
  table.total = Number(res.data.total)
}
const openDetail = async (row) => {
  const res = await request.get(`/api/admin/orders/${row.id}`)
  Object.assign(detail, { items: [] }, res.data)
  visible.value = true
}
const changeStatus = async (row, next) => {
  await request.put(`/api/admin/orders/${row.id}/status`, { status: next })
  ElMessage.success('状态已更新')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.block-title { margin: 16px 0 10px; font-size: 14px; }
.thumb { width: 48px; height: 48px; border-radius: 6px; }
.no-img { color: #c0c4cc; font-size: 12px; }
</style>
