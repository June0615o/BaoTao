<template>
  <el-select v-model="status" placeholder="按状态筛选" style="margin-bottom: 8px">
    <el-option label="全部" :value="''" />
    <el-option label="已创建" value="CREATED" />
    <el-option label="已支付" value="PAID" />
    <el-option label="已发货" value="SHIPPED" />
    <el-option label="已完成" value="FINISHED" />
    <el-option label="已取消" value="CANCELLED" />
  </el-select>
  <el-button style="margin-left: 8px" @click="fetchOrders">刷新</el-button>

  <el-table :data="orders" style="width: 100%; margin-top: 8px">
    <el-table-column prop="id" label="订单号" width="100" />
    <el-table-column prop="user.username" label="用户" />
    <el-table-column prop="status" label="状态" width="120" />
    <el-table-column prop="totalAmount" label="金额" width="120" />
    <el-table-column label="操作" width="220">
      <template #default="scope">
        <el-button
          size="small"
          @click="updateStatus(scope.row.id, 'SHIPPED')"
          v-if="scope.row.status === 'PAID'"
        >
          标记发货
        </el-button>
        <el-button
          size="small"
          type="danger"
          @click="updateStatus(scope.row.id, 'CANCELLED')"
          v-if="scope.row.status === 'CREATED' || scope.row.status === 'PAID'"
        >
          取消
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue';
import http from '../../api/http';

interface Order {
  id: number;
  status: string;
  totalAmount: number;
  user: { username: string };
}

const status = ref<string>('');
const orders = reactive<Order[]>([]);

async function fetchOrders() {
  const params: any = { page: 0, size: 100 };
  if (status.value) params.status = status.value;
  const resp = await http.get('/admin/orders', { params });
  const data = resp.data as { content: Order[] };
  orders.splice(0, orders.length, ...data.content);
}

async function updateStatus(id: number, s: string) {
  await http.put(`/admin/orders/${id}/status`, null, { params: { status: s } });
  await fetchOrders();
}

watch(status, fetchOrders);
onMounted(fetchOrders);
</script>


