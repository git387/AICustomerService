<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品名" style="width:220px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openEdit()">新增商品</el-button>
    </div>
    <el-table :data="table.records" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image
            v-if="row.image"
            :src="row.image"
            :preview-src-list="[row.image]"
            preview-teleported
            fit="cover"
            class="thumb"
          />
          <span v-else class="no-img">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.status === 1 ? '上架' : '下架' }}</template>
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
      v-model:page-size="size"
      :total="table.total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
    <el-dialog v-model="visible" :title="form.id ? '编辑商品' : '新增商品'" width="520px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="主图">
          <div class="image-editor">
            <el-image
              v-if="form.image"
              :src="form.image"
              :preview-src-list="[form.image]"
              preview-teleported
              fit="cover"
              class="preview"
            />
            <div v-else class="preview empty">暂无图片</div>
            <el-upload :show-file-list="false" accept="image/*" :http-request="uploadImage">
              <el-button>{{ form.image ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
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
const size = ref(10)
const table = reactive({ records: [], total: 0 })
const categories = ref([])
const visible = ref(false)
const form = reactive({ id: null, name: '', categoryId: null, price: 0, stock: 0, status: 1, image: '', description: '' })

const load = async () => {
  const res = await request.get('/api/admin/products', { params: { page: page.value, size: size.value, keyword: keyword.value } })
  table.records = res.data.records
  table.total = Number(res.data.total)
}
const loadCategories = async () => {
  const res = await request.get('/api/admin/categories/all')
  categories.value = res.data
}
const openEdit = (row) => {
  Object.assign(form, row ? { ...row } : { id: null, name: '', categoryId: null, price: 0, stock: 0, status: 1, image: '', description: '' })
  visible.value = true
}
const uploadImage = async ({ file }) => {
  const data = new FormData()
  data.append('file', file)
  const res = await request.post('/api/admin/upload/product-image', data, { headers: { 'Content-Type': 'multipart/form-data' } })
  form.image = res.data.url
  ElMessage.success('图片已上传')
}
const save = async () => {
  if (form.id) {
    await request.put(`/api/admin/products/${form.id}`, form)
  } else {
    await request.post('/api/admin/products', form)
  }
  visible.value = false
  ElMessage.success('保存成功')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该商品？')
  await request.delete(`/api/admin/products/${id}`)
  load()
}
onMounted(() => { load(); loadCategories() })
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.thumb { width: 48px; height: 48px; border-radius: 6px; }
.no-img { color: #c0c4cc; font-size: 12px; }
.image-editor { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.preview { width: 120px; height: 120px; border-radius: 8px; border: 1px solid #ebeef5; }
.preview.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 13px;
  background: #f5f7fa;
}
</style>
