<template>
  <el-container style="min-height: 100vh">
    <el-header>
      <el-menu mode="horizontal" :default-active="activePath" router>
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/orders">我的订单</el-menu-item>
        <el-menu-item index="/admin">管理员后台</el-menu-item>
        <div style="flex: 1"></div>
        <el-menu-item v-if="!isLoggedIn" index="/login">登录/注册</el-menu-item>
        <el-sub-menu v-else index="user">
          <template #title>{{ username }}</template>
          <el-menu-item @click="logout">退出登录</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from './store/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const activePath = computed(() => route.path);
const isLoggedIn = computed(() => !!auth.token);
const username = computed(() => auth.username || '用户');

function logout() {
  auth.logout();
  router.push('/login');
}

watch(
  () => route.fullPath,
  () => {
    auth.loadFromStorage();
  },
  { immediate: true }
);
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue',
    Arial, 'Noto Sans', sans-serif;
}
</style>


