<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="page">
    <h2>签到管理</h2>
    <p class="hint">
      今日待签到活动列表入口。查询参数：
      <code>{{ JSON.stringify(route.query) }}</code>
      <span v-if="route.params.activityId"> · 活动 ID {{ route.params.activityId }}</span>
    </p>
    <el-empty description="签到列表对接 /activity-sign/list，待完善业务表单" />
  </div>
</template>

<style scoped>
.page {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
}
h2 {
  margin: 0 0 8px;
  color: var(--brand-deep);
}
.hint {
  color: #909399;
  font-size: 13px;
}
</style>
