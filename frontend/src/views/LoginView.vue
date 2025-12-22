<template>
  <el-row justify="center" style="margin-top: 40px">
    <el-col :span="8">
      <el-card>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" label-width="80px" @submit.prevent>
              <el-form-item label="用户名">
                <el-input v-model="loginForm.username" autocomplete="username" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  autocomplete="current-password"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="doLogin" :loading="loading">
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" label-width="80px" @submit.prevent>
              <el-form-item label="用户名">
                <el-input v-model="registerForm.username" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="registerForm.email" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="registerForm.password" type="password" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="doRegister" :loading="loading">
                  注册
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import http from '../api/http';
import { useAuthStore } from '../store/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const activeTab = ref<'login' | 'register'>('login');
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const registerForm = reactive({
  username: '',
  email: '',
  password: ''
});

async function doLogin() {
  loading.value = true;
  try {
    const resp = await http.post('/auth/login', loginForm);
    const data = resp.data as { token: string; username: string; role: string };
    auth.setAuth(data.token, data.username, data.role);
    ElMessage.success('登录成功');
    const redirect = (route.query.redirect as string) || '/';
    router.push(redirect);
  } catch (e: any) {
    ElMessage.error(e?.response?.data || '登录失败');
  } finally {
    loading.value = false;
  }
}

async function doRegister() {
  loading.value = true;
  try {
    const resp = await http.post('/auth/register', registerForm);
    const data = resp.data as { token: string; username: string; role: string };
    auth.setAuth(data.token, data.username, data.role);
    ElMessage.success('注册并登录成功');
    router.push('/');
  } catch (e: any) {
    ElMessage.error(e?.response?.data || '注册失败');
  } finally {
    loading.value = false;
  }
}
</script>


