<template>
  <el-card>
    <p class="tip">支持上传 txt / doc / pdf / markdown，系统会解析分块后写入 Redis 向量库，供智能客服 RAG 检索。</p>
    <el-upload :show-file-list="false" :http-request="upload" accept=".txt,.doc,.docx,.pdf,.md,.markdown">
      <el-button type="primary" :loading="uploading">上传知识库文件</el-button>
    </el-upload>
    <el-table :data="list" stripe style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="originalName" label="文件名" />
      <el-table-column prop="fileType" label="类型" width="90" />
      <el-table-column prop="chunkCount" label="分块数" width="90" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column prop="errorMsg" label="失败原因" />
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const list = ref([])
const uploading = ref(false)

const load = async () => {
  const res = await request.get('/api/admin/knowledge')
  list.value = res.data
}
const upload = async ({ file }) => {
  uploading.value = true
  try {
    const data = new FormData()
    data.append('file', file)
    await request.post('/api/admin/knowledge', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    })
    ElMessage.success('上传并向量化成功')
    load()
  } finally {
    uploading.value = false
  }
}
const remove = async (id) => {
  await ElMessageBox.confirm('删除后对应向量也会从 Redis 中移除，确认继续？')
  await request.delete(`/api/admin/knowledge/${id}`)
  load()
}
onMounted(load)
</script>

<style scoped>
.tip { color: #606266; margin-bottom: 12px; }
</style>
