<template>
  <div class="card blacklist-card">
    <div class="card-label">
      黑名单管理
      <button class="btn-add" @click="showForm = !showForm">
        {{ showForm ? '取消' : '+ 添加' }}
      </button>
    </div>

    <!-- 添加表单 -->
    <div v-if="showForm" class="add-form">
      <input v-model="form.userId" placeholder="用户ID" type="number" class="input" />
      <input v-model="form.userName" placeholder="用户名" class="input" />
      <input v-model="form.reason" placeholder="原因" class="input" />
      <button class="btn-submit" @click="handleAdd" :disabled="loading">
        {{ loading ? '提交中...' : '确认添加' }}
      </button>
      <p v-if="error" class="error">{{ error }}</p>
    </div>

    <!-- 黑名单列表 -->
    <table v-if="list.length > 0">
      <thead>
        <tr><th>用户ID</th><th>用户名</th><th>原因</th><th>时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.userId">
          <td>{{ item.userId }}</td>
          <td>{{ item.userName }}</td>
          <td>{{ item.reason }}</td>
          <td>{{ item.createTime }}</td>
          <td>
            <button class="btn-remove" @click="handleRemove(item.userId)">移除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="empty">暂无黑名单</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { addBlacklist, removeBlacklist } from '../api/stats.js'

const props = defineProps({
  list: { type: Array, default: () => [] }
})

const emit = defineEmits(['refresh'])
const showForm = ref(false)
const loading = ref(false)
const error = ref('')
const form = reactive({ userId: '', userName: '', reason: '' })

async function handleAdd() {
  if (!form.userId) { error.value = '请输入用户ID'; return }
  loading.value = true
  error.value = ''
  try {
    await addBlacklist(Number(form.userId), form.userName, form.reason)
    form.userId = ''; form.userName = ''; form.reason = ''
    showForm.value = false
    emit('refresh')
  } catch (e) {
    error.value = '添加失败: ' + (e.response?.data?.message || e.message)
  } finally {
    loading.value = false
  }
}

async function handleRemove(userId) {
  await removeBlacklist(userId)
  emit('refresh')
}
</script>

<style scoped>
.blacklist-card { grid-column: 1 / -1; }
.add-form { display: flex; gap: 8px; margin: 12px 0; flex-wrap: wrap; }
.input {
  padding: 8px 12px; border-radius: 6px; border: 1px solid #38444d;
  background: #15202b; color: #e1e8ed; font-size: 0.85rem; flex: 1; min-width: 120px;
}
.btn-add, .btn-submit, .btn-remove {
  padding: 6px 16px; border-radius: 6px; border: none; cursor: pointer; font-size: 0.85rem; transition: all 0.2s;
}
.btn-add { background: #1da1f2; color: white; }
.btn-submit { background: #43e97b; color: #0f1923; }
.btn-remove { background: transparent; color: #ff6b6b; border: 1px solid #ff6b6b; }
.btn-remove:hover { background: #ff6b6b; color: white; }
.btn-add:disabled, .btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
table { width: 100%; border-collapse: collapse; margin-top: 8px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #1c2936; font-size: 0.85rem; }
th { color: #8899a6; font-weight: 600; }
.empty { color: #8899a6; padding: 20px; text-align: center; }
.error { color: #ff6b6b; font-size: 0.8rem; width: 100%; }
</style>
