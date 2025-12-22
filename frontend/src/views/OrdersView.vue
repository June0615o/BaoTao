<template>
  <el-card>
    <el-table :data="orders" style="width: 100%">
      <el-table-column prop="id" label="订单号" width="100" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="totalAmount" label="金额" width="120" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="明细">
        <template #default="scope">
          <ul>
            <li v-for="item in scope.row.items" :key="item.id">
              {{ item.product.name }} x {{ item.quantity }}
            </li>
          </ul>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top: 16px"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="size"
      :current-page="page + 1"
      @current-change="handlePageChange"
    />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import http from '../api/http';

interface OrderItem {
  id: number;
  quantity: number;
  product: { id: number; name: string };
}

interface Order {
  id: number;
  status: string;
  totalAmount: number;
  createdAt: string;
  items: OrderItem[];
}

const orders = reactive<Order[]>([]);
const page = ref(0);
const size = ref(10);
const total = ref(0);

async function fetchOrders() {
  const resp = await http.get('/orders', {
    params: { page: page.value, size: size.value }
  });
  const data = resp.data as { content: Order[]; totalElements: number };
  orders.splice(0, orders.length, ...data.content);
  total.value = data.totalElements;
}

function handlePageChange(p: number) {
  page.value = p - 1;
  fetchOrders();
}

onMounted(fetchOrders);
</script>


