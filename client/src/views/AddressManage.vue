<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="收货人/手机号/地址" style="width:220px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openEdit()">新增地址</el-button>
    </div>
    <el-table :data="table.records" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="手机号" width="130" />
      <el-table-column label="完整地址">
        <template #default="{ row }">{{ (row.province || '') + (row.city || '') + (row.district || '') + (row.detail || '') }}</template>
      </el-table-column>
      <el-table-column label="默认" width="80">
        <template #default="{ row }">{{ row.isDefault === 1 ? '是' : '否' }}</template>
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
      :total="table.total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
    <el-dialog v-model="visible" :title="form.id ? '编辑地址' : '新增地址'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="用户" v-if="!form.id">
          <el-select v-model="form.userId" filterable placeholder="选择用户" style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="u.username + ' (' + (u.nickname || '') + ')'" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="收货人"><el-input v-model="form.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区/县"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="form.detail" type="textarea" rows="2" /></el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
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

const keyword = ref('')
const page = ref(1)
const table = reactive({ records: [], total: 0 })
const users = ref([])
const visible = ref(false)
const emptyForm = { id: null, userId: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '', isDefault: 0 }
const form = reactive({ ...emptyForm })

const load = async () => {
  const res = await request.get('/api/admin/addresses', {
    params: { page: page.value, size: 10, keyword: keyword.value }
  })
  table.records = res.data.records
  table.total = Number(res.data.total)
}
const loadUsers = async () => {
  const res = await request.get('/api/admin/users', { params: { page: 1, size: 100 } })
  users.value = res.data.records || []
}
const openEdit = (row) => {
  Object.assign(form, row ? { ...row } : { ...emptyForm })
  visible.value = true
}
const save = async () => {
  if (form.id) {
    await request.put(`/api/admin/addresses/${form.id}`, form)
  } else {
    await request.post('/api/admin/addresses', form)
  }
  visible.value = false
  ElMessage.success('保存成功')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该收货地址？')
  await request.delete(`/api/admin/addresses/${id}`)
  load()
}
onMounted(() => { load(); loadUsers() })
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
</style>
