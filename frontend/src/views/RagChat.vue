<script setup lang="ts">
import { ref } from 'vue'
import { ragQuery, type RagResult } from '../api/rag'

const query = ref('')
const topK = ref(5)
const result = ref<RagResult | null>(null)
const loading = ref(false)
const error = ref('')

async function handleQuery() {
  if (!query.value.trim()) return

  error.value = ''
  result.value = null
  loading.value = true
  try {
    const { data } = await ragQuery(query.value, topK.value)
    result.value = data.data
  } catch (e: any) {
    error.value = e.response?.data?.message || '查询失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="rag-chat">
    <h2>RAG 问答</h2>

    <form class="query-form" @submit.prevent="handleQuery">
      <input
        v-model="query"
        type="text"
        placeholder="输入你的问题..."
        :disabled="loading"
      />
      <div class="query-options">
        <label>
          引用数量
          <select v-model.number="topK">
            <option :value="3">3</option>
            <option :value="5">5</option>
            <option :value="10">10</option>
          </select>
        </label>
        <button type="submit" :disabled="loading || !query.trim()">
          {{ loading ? '查询中...' : '提问' }}
        </button>
      </div>
    </form>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="result" class="result">
      <div class="answer">
        <h3>回答</h3>
        <p>{{ result.answer }}</p>
      </div>

      <div v-if="result.sources.length > 0" class="sources">
        <h3>引用来源（{{ result.sources.length }} 条）</h3>
        <div v-for="(source, i) in result.sources" :key="i" class="source-item">
          <div class="source-header">来源 {{ i + 1 }}</div>
          <p class="source-content">{{ source.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.rag-chat {
  padding: 1.5rem 0;
}

.rag-chat h2 {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 1rem;
}

.query-form input {
  width: 100%;
  padding: 0.7rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.query-form input:focus {
  outline: none;
  border-color: #4a90d9;
}

.query-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.6rem;
}

.query-options label {
  font-size: 0.85rem;
  color: #666;
}

.query-options select {
  margin-left: 0.4rem;
  padding: 0.3rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.query-options button {
  padding: 0.5rem 1.2rem;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.query-options button:hover:not(:disabled) {
  background: #357abd;
}

.query-options button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

.result {
  margin-top: 1.5rem;
}

.answer {
  background: #fff;
  padding: 1.2rem;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.answer h3 {
  font-size: 1rem;
  color: #333;
  margin-bottom: 0.6rem;
}

.answer p {
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

.sources {
  margin-top: 1.2rem;
}

.sources h3 {
  font-size: 1rem;
  color: #333;
  margin-bottom: 0.6rem;
}

.source-item {
  background: #fff;
  padding: 0.8rem 1rem;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  margin-bottom: 0.5rem;
}

.source-header {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 0.3rem;
}

.source-content {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.5;
  max-height: 120px;
  overflow-y: auto;
}
</style>
