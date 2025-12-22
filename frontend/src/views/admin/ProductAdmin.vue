<template>
  <el-button type="primary" style="margin-bottom: 8px" @click="openCreate">
    新增商品
  </el-button>
  <el-table :data="products" style="width: 100%">
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column prop="name" label="名称" />
    <el-table-column prop="price" label="价格" />
    <el-table-column prop="stock" label="库存" />
    <el-table-column label="操作">
      <template #default="scope">
        <el-button type="text" @click="openEdit(scope.row)">编辑</el-button>
        <el-button type="text" @click="remove(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog v-model="dialogVisible" :title="editing ? '编辑商品' : '新增商品'">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="价格">
        <el-input v-model.number="form.price" />
      </el-form-item>
      <el-form-item label="库存">
        <el-input v-model.number="form.stock" />
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="form.category" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import http from '../../api/http';
import { ElMessageBox, ElMessage } from 'element-plus';

interface Product {
  id?: number;
  name: string;
  price: number;
  stock: number;
  category?: string;
}

const products = reactive<Product[]>([]);
const dialogVisible = ref(false);
const editing = ref(false);
const form = reactive<Product>({
  name: '',
  price: 0,
  stock: 0,
  category: ''
});

async function fetchProducts() {
  const resp = await http.get('/admin/products', { params: { page: 0, size: 100 } });
  const data = resp.data as { content: Product[] };
  products.splice(0, products.length, ...data.content);
}

function openCreate() {
  editing.value = false;
  Object.assign(form, { id: undefined, name: '', price: 0, stock: 0, category: '' });
  dialogVisible.value = true;
}

function openEdit(p: Product) {
  editing.value = true;
  Object.assign(form, p);
  dialogVisible.value = true;
}

async function save() {
  if (editing.value && form.id) {
    await http.put(`/admin/products/${form.id}`, form);
  } else {
    await http.post('/admin/products', form);
  }
  dialogVisible.value = false;
  await fetchProducts();
}

async function remove(id?: number) {
  if (!id) return;
  await ElMessageBox.confirm('确认删除该商品吗？', '提示');
  await http.delete(`/admin/products/${id}`);
  ElMessage.success('已删除');
  await fetchProducts();
}

onMounted(fetchProducts);
</script>


