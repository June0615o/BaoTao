<template>
  <el-row :gutter="16">
    <el-col :span="8">
      <h3>客户列表</h3>
      <el-table :data="customers" @row-click="selectCustomer">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
      </el-table>
    </el-col>
    <el-col :span="16">
      <h3>浏览/购买日志 (用户ID: {{ selectedCustomerId || '-' }})</h3>
      <el-table :data="logs">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="action" label="动作" />
        <el-table-column prop="product.id" label="商品ID" />
        <el-table-column prop="product.name" label="商品名称" />
        <el-table-column prop="createdAt" label="时间" />
      </el-table>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import http from '../../api/http';

interface Customer {
  id: number;
  username: string;
  email: string;
}

interface Log {
  id: number;
  action: string;
  createdAt: string;
  product?: { id: number; name: string };
}

const customers = reactive<Customer[]>([]);
const logs = reactive<Log[]>([]);
const selectedCustomerId = ref<number | null>(null);

async function fetchCustomers() {
  const resp = await http.get('/admin/customers');
  customers.splice(0, customers.length, ...(resp.data as Customer[]));
}

async function selectCustomer(row: Customer) {
  selectedCustomerId.value = row.id;
  const resp = await http.get(`/admin/customers/${row.id}/logs`);
  logs.splice(0, logs.length, ...(resp.data as Log[]));
}

onMounted(fetchCustomers);
</script>


