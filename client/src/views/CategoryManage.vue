<template>
  <el-card>
    <el-button type="success" style="margin-bottom:12px" @click="openEdit()">新增分类</el-button>
    <el-table :data="table.records" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top:16px"
      v-model:current-page="page"
      :page-size="size"
      :total="table.total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
    <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新增分类'" width="460px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const page = ref(1)
const size = 10
const table = reactive({ records: [], total: 0 })
const visible = ref(false)
const form = reactive({ id: null, name: '', description: '', sortOrder: 0, status: 1 })

const load = async () => {
  const res = await request.get('/api/admin/categories', { params: { page: page.value, size } })
  table.records = res.data.records
  table.total = Number(res.data.total)
}
const openEdit = (row) => {
  Object.assign(form, row ? { ...row } : { id: null, name: '', description: '', sortOrder: 0, status: 1 })
  visible.value = true
}
const save = async () => {
  if (form.id) await request.put(`/api/admin/categories/${form.id}`, form)
  else await request.post('/api/admin/categories', form)
  visible.value = false
  ElMessage.success('保存成功')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该分类？')
  await request.delete(`/api/admin/categories/${id}`)
  load()
}
onMounted(load)
</script>
