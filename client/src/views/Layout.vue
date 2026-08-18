<template>
  <el-container class="layout">
    <el-aside width="210px">
      <div class="brand">AI 商城后台</div>
      <el-menu :default-active="$route.path" router background-color="#1f2937" text-color="#d1d5db" active-text-color="#93c5fd">
        <el-menu-item index="/dashboard">首页</el-menu-item>
        <el-menu-item index="/products">商品管理</el-menu-item>
        <el-menu-item index="/categories">分类管理</el-menu-item>
        <el-menu-item index="/orders">订单管理</el-menu-item>
        <el-menu-item index="/users">用户管理</el-menu-item>
        <el-menu-item index="/addresses">收货地址</el-menu-item>
        <el-menu-item index="/knowledge">知识库管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span>{{ $route.meta.title || '管理后台' }}</span>
        <el-button type="primary" link @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import request from '../api/request'

const router = useRouter()
const logout = async () => {
  await request.post('/api/auth/logout')
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.el-aside { background: #1f2937; color: #fff; }
.brand { height: 60px; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}
.el-main { background: #f5f7fb; }
.el-menu { border-right: none; }
</style>
