<template>
  <el-row justify="space-between" style="margin-bottom: 16px">
    <el-col :span="8">
      <el-input
        v-model="keyword"
        placeholder="搜索商品"
        clearable
        @keyup.enter.native="fetchProducts"
      >
        <template #append>
          <el-button @click="fetchProducts">搜索</el-button>
        </template>
      </el-input>
    </el-col>
  </el-row>
  <el-row :gutter="16">
    <el-col v-for="item in products" :key="item.id" :span="6" style="margin-bottom: 16px">
      <el-card @click="goDetail(item.id)" style="cursor: pointer">
        <h3>{{ item.name }}</h3>
        <p>{{ item.description }}</p>
        <p style="color: #f56c6c">￥{{ item.price }}</p>
      </el-card>
    </el-col>
  </el-row>
  <el-pagination
    style="margin-top: 16px"
    background
    layout="prev, pager, next"
    :total="total"
    :page-size="size"
    :current-page="page + 1"
    @current-change="handlePageChange"
  />
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import http from '../api/http';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
}

const router = useRouter();
const products = reactive<Product[]>([]);
const keyword = ref('');
const page = ref(0);
const size = ref(8);
const total = ref(0);

async function fetchProducts() {
  const resp = await http.get('/products', {
    params: { q: keyword.value, page: page.value, size: size.value }
  });
  const data = resp.data as { content: Product[]; totalElements: number };
  products.splice(0, products.length, ...data.content);
  total.value = data.totalElements;
}

function handlePageChange(p: number) {
  page.value = p - 1;
  fetchProducts();
}

function goDetail(id: number) {
  router.push(`/product/${id}`);
}

onMounted(fetchProducts);
</script>


