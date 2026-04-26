<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const username = localStorage.getItem('username') || ''

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <header>
      <div class="left">
        <h1>RagHub</h1>
        <nav>
          <router-link to="/documents">文档管理</router-link>
          <router-link to="/rag">RAG 问答</router-link>
        </nav>
      </div>
      <div class="user-info">
        <span>{{ username }}</span>
        <button @click="handleLogout">退出</button>
      </div>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 1rem;
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

header h1 {
  font-size: 1.4rem;
  color: #333;
}

nav {
  display: flex;
  gap: 1rem;
}

nav a {
  text-decoration: none;
  color: #666;
  font-size: 0.95rem;
  padding: 0.3rem 0;
  border-bottom: 2px solid transparent;
}

nav a.router-link-active {
  color: #4a90d9;
  border-bottom-color: #4a90d9;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.user-info span {
  color: #666;
  font-size: 0.9rem;
}

.user-info button {
  padding: 0.3rem 0.8rem;
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 0.85rem;
}

.user-info button:hover {
  background: #f5f5f5;
}
</style>
