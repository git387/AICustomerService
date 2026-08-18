import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '',
  withCredentials: true,
  timeout: 30000
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(data)
    }
    return data
  },
  err => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      ElMessage.error('请先登录管理员账号')
      router.push('/login')
    } else {
      ElMessage.error(err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
