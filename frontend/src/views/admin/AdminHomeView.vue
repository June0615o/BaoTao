<template>
  <el-tabs v-model="activeTab">
    <el-tab-pane label="商品管理" name="products">
      <ProductAdmin />
    </el-tab-pane>
    <el-tab-pane label="订单管理" name="orders">
      <OrderAdmin />
    </el-tab-pane>
    <el-tab-pane label="销售统计" name="stats">
      <StatsAdmin />
    </el-tab-pane>
    <el-tab-pane label="客户日志" name="logs">
      <CustomerLogsAdmin />
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import ProductAdmin from './ProductAdmin.vue';
import OrderAdmin from './OrderAdmin.vue';
import StatsAdmin from './StatsAdmin.vue';
import CustomerLogsAdmin from './CustomerLogsAdmin.vue';

const activeTab = ref('products');
</script>


