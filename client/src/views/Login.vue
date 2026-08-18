<template>
  <div class="login-page">
    <el-card class="box">
      <h2>商城管理后台</h2>
      <p class="tip">请使用管理员账号登录，默认 admin / root123456</p>
      <el-form @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'root123456' })

const onSubmit = async () => {
  loading.value = true
  try {
    const res = await request.post('/api/auth/login', form)
    if (res.data.role !== 'ADMIN') {
      ElMessage.error('请使用管理员账号登录')
      await request.post('/api/auth/logout')
      return
    }
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d4ed8, #7c3aed);
}
.box { width: 380px; }
.tip { color: #909399; margin-bottom: 16px; }
</style>
