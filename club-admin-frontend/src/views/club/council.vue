<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clubCouncilApi } from '@/api/clubCouncil'
import { formatDateTime } from '@/utils/format'
import { levelLabel } from '@/utils/level'
import type { ClubCouncilDetail } from '@/types/generated'

const route = useRoute()
const router = useRouter()

const clubId = computed(() => Number(route.params.clubId))
const loading = ref(false)
const signing = ref(false)
const detail = ref<ClubCouncilDetail | null>(null)

async function load() {
  if (!clubId.value) {
    ElMessage.warning('无效的社团 ID')
    router.replace('/club')
    return
  }
  loading.value = true
  try {
    const res = await clubCouncilApi.detail({ clubId: clubId.value })
    detail.value = res.data || null
  } catch {
    ElMessage.warning('合议信息不存在或无权查看')
    router.replace('/club')
  } finally {
    loading.value = false
  }
}

async function handleSign() {
  if (!detail.value?.id) return
  await ElMessageBox.confirm('确认在本合议上签字？', '合议签字', { type: 'warning' })
  signing.value = true
  try {
    await clubCouncilApi.sign(detail.value.id)
    ElMessage.success('签字成功')
    await load()
    if (detail.value?.status === 2) {
      ElMessageBox.alert('社团已解散', '提示', { type: 'success' }).then(() => {
        router.replace('/club')
      })
    }
  } finally {
    signing.value = false
  }
}

function goBack() {
  router.push('/club')
}

onMounted(load)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <el-page-header @back="goBack">
      <template #content>
        <span class="page-title">合议签字</span>
        <span v-if="detail?.clubName" class="page-sub">{{ detail.clubName }}</span>
      </template>
    </el-page-header>

    <el-card v-if="detail" shadow="never">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="社团编号">{{ detail.clubCode }}</el-descriptions-item>
        <el-descriptions-item label="挂靠学院">{{ detail.collegeName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ detail.initiatorName }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">
          {{ formatDateTime(detail.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="解散原因" :span="2">
          {{ detail.reason }}
        </el-descriptions-item>
        <el-descriptions-item label="合议状态">
          <el-tag v-if="detail.status === 1" type="warning">合议中</el-tag>
          <el-tag v-else-if="detail.status === 2" type="success">已通过</el-tag>
          <el-tag v-else type="info">已驳回</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <h3 class="section-title">签字人列表</h3>
      <el-table :data="detail.signatories || []" border stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="roleCode" label="角色码" width="140" />
        <el-table-column label="等级" width="120">
          <template #default="{ row }">{{ levelLabel(row.level ?? 4) }}</template>
        </el-table-column>
        <el-table-column label="签字时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.signTime) }}</template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!(detail.signatories && detail.signatories.length)"
        description="暂无签字记录"
        :image-size="64"
      />

      <div class="actions">
        <el-button
          v-if="detail.canSign"
          type="primary"
          :loading="signing"
          @click="handleSign"
        >
          确认签字
        </el-button>
        <el-tag v-else-if="detail.alreadySigned" type="success">您已签字</el-tag>
        <el-tag v-else type="info">当前不可签字</el-tag>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.page-title {
  font-weight: 600;
  color: var(--brand-deep);
}
.page-sub {
  margin-left: 10px;
  color: #606266;
  font-size: 14px;
}
.section-title {
  margin: 20px 0 12px;
  font-size: 15px;
  color: var(--brand-deep);
}
.actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
