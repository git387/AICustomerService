<template>
  <el-card>
    <el-input v-model="keyword" placeholder="用户名/昵称" style="width:220px;margin-bottom:12px" @keyup.enter="load" />
    <el-button type="primary" @click="load">查询</el-button>
    <el-table :data="table.records" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '正常' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="warning" @click="toggle(row)" v-if="row.role !== 'ADMIN'">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="remove(row.id)" v-if="row.role !== 'ADMIN'">删除</el-button>
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
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const keyword = ref('')
const page = ref(1)
const table = reactive({ records: [], total: 0 })

const load = async () => {
  const res = await request.get('/api/admin/users', { params: { page: page.value, size: 10, keyword: keyword.value } })
  table.records = res.data.records
  table.total = Number(res.data.total)
}
const toggle = async (row) => {
  await request.put(`/api/admin/users/${row.id}`, { ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success('已更新')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该用户？')
  await request.delete(`/api/admin/users/${id}`)
  load()
}
onMounted(load)
</script>
