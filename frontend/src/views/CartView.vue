<template>
  <el-card>
    <el-table :data="items" style="width: 100%">
      <el-table-column prop="product.name" label="商品" />
      <el-table-column prop="product.price" label="单价" />
      <el-table-column label="数量">
        <template #default="scope">
          <el-input-number
            v-model="scope.row.quantity"
            :min="1"
            @change="q => updateQuantity(scope.row.id, q as number)"
          />
        </template>
      </el-table-column>
      <el-table-column label="小计">
        <template #default="scope">
          ￥{{ (scope.row.product.price * scope.row.quantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button type="text" @click="removeItem(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 16px; text-align: right">
      总计：<b>￥{{ total.toFixed(2) }}</b>
      <el-button
        type="primary"
        style="margin-left: 16px"
        :disabled="items.length === 0"
        @click="checkout"
      >
        结算下单
      </el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue';
import http from '../api/http';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

interface CartItem {
  id: number;
  quantity: number;
  product: { id: number; name: string; price: number };
}

const router = useRouter();
const items = reactive<CartItem[]>([]);

const total = computed(() =>
  items.reduce((sum, i) => sum + i.quantity * i.product.price, 0)
);

async function fetchCart() {
  const resp = await http.get('/cart');
  items.splice(0, items.length, ...(resp.data as CartItem[]));
}

async function updateQuantity(id: number, quantity: number) {
  await http.put(`/cart/items/${id}`, { quantity });
  await fetchCart();
}

async function removeItem(id: number) {
  await http.delete(`/cart/items/${id}`);
  await fetchCart();
}

async function checkout() {
  try {
    await http.post('/orders/checkout');
    ElMessage.success('下单成功');
    router.push('/orders');
  } catch (e: any) {
    ElMessage.error(e?.response?.data || '下单失败');
  }
}

onMounted(fetchCart);
</script>


