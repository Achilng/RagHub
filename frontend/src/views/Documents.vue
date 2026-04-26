<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listDocuments, uploadDocument, deleteDocument, type DocumentItem } from '../api/document'

const documents = ref<DocumentItem[]>([])
const uploading = ref(false)
const error = ref('')

async function loadDocuments() {
  try {
    const { data } = await listDocuments()
    documents.value = data.data
  } catch (e: any) {
    error.value = e.response?.data?.message || '加载文档列表失败'
  }
}

async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  error.value = ''
  uploading.value = true
  try {
    await uploadDocument(file)
    await loadDocuments()
  } catch (e: any) {
    error.value = e.response?.data?.message || '上传失败'
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function handleDelete(doc: DocumentItem) {
  if (!confirm(`确定删除「${doc.filename}」？`)) return

  try {
    await deleteDocument(doc.id)
    await loadDocuments()
  } catch (e: any) {
    error.value = e.response?.data?.message || '删除失败'
  }
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING: { label: '等待中', color: '#999' },
  PROCESSING: { label: '处理中', color: '#e6a23c' },
  COMPLETED: { label: '已完成', color: '#67c23a' },
  FAILED: { label: '失败', color: '#f56c6c' },
}

onMounted(loadDocuments)
</script>

<template>
  <div class="documents">
    <div class="toolbar">
      <h2>文档管理</h2>
      <label class="upload-btn" :class="{ disabled: uploading }">
        {{ uploading ? '上传中...' : '上传文档' }}
        <input type="file" hidden :disabled="uploading" @change="handleUpload" />
      </label>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="documents.length === 0 && !uploading" class="empty">
      暂无文档，点击上方按钮上传
    </div>

    <table v-else-if="documents.length > 0">
      <thead>
        <tr>
          <th>文件名</th>
          <th>大小</th>
          <th>状态</th>
          <th>分块数</th>
          <th>上传时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="doc in documents" :key="doc.id">
          <td>{{ doc.filename }}</td>
          <td>{{ formatSize(doc.fileSize) }}</td>
          <td>
            <span
              class="status-badge"
              :style="{ color: statusMap[doc.status]?.color }"
            >
              {{ statusMap[doc.status]?.label || doc.status }}
            </span>
          </td>
          <td>{{ doc.chunkCount }}</td>
          <td>{{ new Date(doc.createdAt).toLocaleString() }}</td>
          <td>
            <button class="delete-btn" @click="handleDelete(doc)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.documents {
  padding: 1.5rem 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.toolbar h2 {
  font-size: 1.3rem;
  color: #333;
}

.upload-btn {
  padding: 0.5rem 1rem;
  background: #4a90d9;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.upload-btn:hover:not(.disabled) {
  background: #357abd;
}

.upload-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.empty {
  text-align: center;
  padding: 3rem 0;
  color: #999;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 0.6rem 0.8rem;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 0.9rem;
}

th {
  color: #888;
  font-weight: 500;
}

.status-badge {
  font-weight: 500;
}

.delete-btn {
  padding: 0.2rem 0.6rem;
  background: transparent;
  border: 1px solid #f56c6c;
  color: #f56c6c;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.delete-btn:hover {
  background: #f56c6c;
  color: #fff;
}
</style>
