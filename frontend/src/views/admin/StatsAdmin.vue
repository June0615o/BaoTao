<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <h3>按日销售额</h3>
      <el-table :data="salesByDay" size="small">
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="amount" label="金额" />
      </el-table>
    </el-col>
    <el-col :span="12">
      <h3>热销商品 Top</h3>
      <el-table :data="topProducts" size="small">
        <el-table-column prop="productId" label="商品ID" />
        <el-table-column prop="quantity" label="销量" />
      </el-table>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue';
import http from '../../api/http';

interface SalesByDay {
  date: string;
  amount: number;
}

interface TopProduct {
  productId: number;
  quantity: number;
}

const salesByDay = reactive<SalesByDay[]>([]);
const topProducts = reactive<TopProduct[]>([]);

async function fetchStats() {
  const r1 = await http.get('/admin/statistics/sales-by-day');
  salesByDay.splice(0, salesByDay.length, ...(r1.data as SalesByDay[]));

  const r2 = await http.get('/admin/statistics/top-products', {
    params: { limit: 10 }
  });
  topProducts.splice(0, topProducts.length, ...(r2.data as TopProduct[]));
}

onMounted(fetchStats);
</script>


