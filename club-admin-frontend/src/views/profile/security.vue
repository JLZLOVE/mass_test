<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function submit() {
  if (!form.oldPassword || !form.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  loading.value = true
  try {
    ElMessage.info('密码修改接口待后端开放，当前仅完成页面骨架')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <h2>安全设置</h2>
    <p class="hint">密码修改与多因素认证绑定页（骨架）</p>
    <el-form label-width="100px" style="max-width: 420px" @submit.prevent>
      <el-form-item label="当前密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.page {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  max-width: 720px;
}
h2 {
  margin: 0 0 8px;
  color: var(--brand-deep);
}
.hint {
  color: #909399;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
