import { createRouter, createWebHistory } from 'vue-router'
import request from '../api/request'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'products', component: () => import('../views/ProductManage.vue'), meta: { title: '商品管理' } },
      { path: 'categories', component: () => import('../views/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'orders', component: () => import('../views/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'users', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'addresses', component: () => import('../views/AddressManage.vue'), meta: { title: '收货地址' } },
      { path: 'knowledge', component: () => import('../views/KnowledgeManage.vue'), meta: { title: '知识库管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.path === '/login') {
    return true
  }
  try {
    const res = await request.get('/api/auth/me')
    if (res.data && res.data.role === 'ADMIN') {
      return true
    }
    return '/login'
  } catch {
    return '/login'
  }
})

export default router
