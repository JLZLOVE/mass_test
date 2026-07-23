<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="page">
    <h2>个人中心</h2>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ userStore.displayName }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ userStore.roleName }}</el-descriptions-item>
      <el-descriptions-item label="手机">{{ userStore.userInfo?.phone || '—' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '—' }}</el-descriptions-item>
    </el-descriptions>
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
  margin: 0 0 16px;
  color: var(--brand-deep);
}
</style>
