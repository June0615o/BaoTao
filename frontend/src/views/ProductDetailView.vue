<template>
  <el-card v-if="product">
    <h2>{{ product.name }}</h2>
    <p>{{ product.description }}</p>
    <p>分类：{{ product.category || '无' }}</p>
    <p>库存：{{ product.stock }}</p>
    <p style="color: #f56c6c; font-size: 18px">￥{{ product.price }}</p>
    <el-input-number v-model="quantity" :min="1" />
    <el-button type="primary" style="margin-left: 8px" @click="addToCart">
      加入购物车
    </el-button>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import http from '../api/http';
import { useAuthStore } from '../store/auth';
import { ElMessage } from 'element-plus';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  category?: string;
}

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const product = ref<Product | null>(null);
const quantity = ref(1);

async function fetchProduct() {
  const id = route.params.id;
  const resp = await http.get(`/products/${id}`);
  product.value = resp.data as Product;
}

async function addToCart() {
  auth.loadFromStorage();
  if (!auth.token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  await http.post('/cart/items', {
    productId: product.value?.id,
    quantity: quantity.value
  });
  ElMessage.success('已加入购物车');
}

onMounted(fetchProduct);
</script>


