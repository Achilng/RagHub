<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await login(username.value, password.value)
    localStorage.setItem('token', data.data.token)
    localStorage.setItem('username', data.data.username)
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>登录 RagHub</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            required
          />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            required
          />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="link">
        没有账号？<router-link to="/register">去注册</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.auth-card {
  background: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.auth-card h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  font-size: 0.9rem;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #4a90d9;
}

.error {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

button {
  width: 100%;
  padding: 0.7rem;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

button:hover:not(:disabled) {
  background: #357abd;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.link {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
  color: #666;
}

.link a {
  color: #4a90d9;
  text-decoration: none;
}
</style>
