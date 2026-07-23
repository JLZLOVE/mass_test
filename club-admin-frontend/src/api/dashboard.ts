import { activityApplyApi } from '@/api/activityApply'
import { noticeInfoApi } from '@/api/noticeInfo'
import { portalApi } from '@/api/portal'
import { sysUserApi } from '@/api/sysUser'
import type {
  ActivityApply,
  DashboardKpiCard,
  DashboardTaskItem,
  NoticeInfo,
  PageResult,
} from '@/types/generated'
import { LEVEL } from '@/utils/level'

function pageTotal(data?: PageResult<unknown> | null): number {
  return data?.total ?? 0
}

function pageRecords<T>(data?: PageResult<T> | null): T[] {
  return data?.records ?? []
}

async function safeTotal(
  fn: () => Promise<{ data?: PageResult<unknown> | null }>,
): Promise<number> {
  try {
    const res = await fn()
    return pageTotal(res.data)
  } catch {
    return 0
  }
}

function stayDays(from?: string): number {
  if (!from) return 0
  const t = new Date(from.replace('T', ' ')).getTime()
  if (Number.isNaN(t)) return 0
  return Math.max(0, Math.floor((Date.now() - t) / 86400000))
}

const APPROVE_STATUS_TEXT: Record<number, string> = {
  1: '草稿',
  2: '待审批',
  3: '审批中',
  4: '已通过',
  5: '已驳回',
  6: '已取消',
  7: '变更中',
}

function toTaskItem(row: ActivityApply, mode: 'approve' | 'mine'): DashboardTaskItem {
  const days = stayDays(row.applyTime || row.createTime)
  return {
    id: row.id!,
    title: row.activityName || row.activityNo || `活动#${row.id}`,
    subtitle:
      mode === 'approve'
        ? `${row.applyUsername || '未知发起人'} · 第${row.currentApproveStep ?? '-'}步`
        : APPROVE_STATUS_TEXT[row.approveStatus ?? 0] || '未知状态',
    statusText: APPROVE_STATUS_TEXT[row.approveStatus ?? 0] || '',
    stayDays: days,
    route: `/activity/approve-flow/${row.id}`,
    query: mode === 'approve' ? { status: 'pending' } : undefined,
  }
}

/** 按 effectiveLevel 构建 KPI 卡片定义（3B：无第4卡则不返回） */
export function buildKpiDefs(level: number): Omit<DashboardKpiCard, 'value' | 'changePercent'>[] {
  if (level <= LEVEL.SUPER_ADMIN) {
    return [
      { key: 'users', label: '平台总用户数', route: '/member', query: { status: '1' } },
      { key: 'pending', label: '全平台待审批总量', route: '/activity/apply', query: { status: 'pending' } },
      { key: 'clubs', label: '当前活跃社团数', route: '/club', query: { status: '1' } },
      { key: 'uptime', label: '系统连续运行天数', route: '/statistics' },
    ]
  }
  if (level <= LEVEL.ADMIN) {
    return [
      { key: 'students', label: '管辖范围内学生总数', route: '/member', query: { userType: '1' } },
      { key: 'myPending', label: '待本人审批的任务数', route: '/activity/apply', query: { status: 'pending' } },
      { key: 'weekActs', label: '本周管辖内活动总数', route: '/activity/apply', query: { range: 'week' } },
      { key: 'overdue', label: '超时未处理预警数', route: '/activity/apply', query: { overdue: 'true' } },
    ]
  }
  if (level <= LEVEL.CLUB_LEADER) {
    return [
      { key: 'members', label: '本社团总成员数', route: '/member' },
      { key: 'myInProgress', label: '本人发起的进行中申请数', route: '/activity/apply', query: { status: 'processing' } },
      { key: 'signRate', label: '今日活动签到率', route: '/activity/sign', query: { range: 'today' } },
      { key: 'summaryDue', label: '待提交的活动总结数', route: '/activity/apply', query: { summary: 'pending' } },
    ]
  }
  if (level <= LEVEL.DEPT_LEADER) {
    return [
      { key: 'deptMembers', label: '本部门成员数', route: '/member' },
      { key: 'drafts', label: '本人草稿/待提交申请数', route: '/activity/apply', query: { status: 'draft' } },
      { key: 'deptActs', label: '近期部门参与活动数', route: '/activity/apply' },
      { key: 'collab', label: '部门内待办协作任务数', route: '/activity/apply', query: { status: 'pending' } },
    ]
  }
  // Level 4：仅 3 张（3B 规则隐藏第 4 卡）
  return [
    { key: 'myActs', label: '本人参与的活动总数', route: '/activity/apply' },
    { key: 'mySigns', label: '本人的累计签到次数', route: '/activity/sign' },
    { key: 'unread', label: '平台未读通知数', route: '/notice', query: { unread: 'true' } },
  ]
}

export interface DashboardBundle {
  kpis: DashboardKpiCard[]
  overdueCount: number
  tasks: DashboardTaskItem[]
  notices: NoticeInfo[]
  unreadCount: number
}

/**
 * 并行拉取工作台数据。无专用聚合接口时，用现有 list/inbox/portal 拼装；
 * 无法从后端取得的指标值为 null（不造 Mock）。
 */
export async function fetchDashboardBundle(level: number): Promise<DashboardBundle> {
  const defs = buildKpiDefs(level)

  const pendingQuery = { page: 1, limit: 10, approveStatus: 2 }
  const listQuery = { page: 1, limit: 10 }

  const [
    userTotal,
    pendingPage,
    activityPage,
    clubRaw,
    inboxPage,
    draftPage,
  ] = await Promise.all([
    safeTotal(() => sysUserApi.query({ page: 1, limit: 1, status: 1 })),
    (async () => {
      try {
        return (await activityApplyApi.list(pendingQuery)).data
      } catch {
        return null
      }
    })(),
    (async () => {
      try {
        return (await activityApplyApi.list(listQuery)).data
      } catch {
        return null
      }
    })(),
    (async () => {
      try {
        return (await portalApi.clubs({})).data
      } catch {
        return null
      }
    })(),
    (async () => {
      try {
        return (await noticeInfoApi.inbox({ page: 1, limit: 5 })).data
      } catch {
        return null
      }
    })(),
    (async () => {
      try {
        return (await activityApplyApi.list({ page: 1, limit: 1, approveStatus: 1 })).data
      } catch {
        return null
      }
    })(),
  ])

  let clubCount = 0
  if (Array.isArray(clubRaw)) {
    clubCount = clubRaw.length
  } else if (clubRaw && typeof clubRaw === 'object' && 'total' in clubRaw) {
    clubCount = (clubRaw as PageResult<unknown>).total ?? 0
  }

  const pendingTotal = pageTotal(pendingPage)
  const activityTotal = pageTotal(activityPage)
  const unreadCount = pageTotal(inboxPage)
  const draftTotal = pageTotal(draftPage)

  const overdueTasks = pageRecords(pendingPage).filter(
    (r) => stayDays(r.applyTime || r.createTime) > 7,
  )
  const overdueCount = overdueTasks.length

  const valueMap: Record<string, number | null> = {
    users: userTotal,
    pending: pendingTotal,
    clubs: clubCount,
    uptime: null, // 无后端运行天数接口
    students: userTotal,
    myPending: pendingTotal,
    weekActs: activityTotal,
    overdue: overdueCount,
    members: userTotal,
    myInProgress: pendingTotal,
    signRate: null,
    summaryDue: null,
    deptMembers: userTotal,
    drafts: draftTotal,
    deptActs: activityTotal,
    collab: pendingTotal,
    myActs: activityTotal,
    mySigns: null,
    unread: unreadCount,
  }

  const kpis: DashboardKpiCard[] = defs.map((d) => ({
    ...d,
    value: valueMap[d.key] ?? null,
    changePercent: null,
  }))

  const isApprover = level <= LEVEL.ADMIN
  const taskSource = isApprover ? pendingPage : activityPage
  const tasks = pageRecords(taskSource)
    .slice(0, 8)
    .map((r) => toTaskItem(r, isApprover ? 'approve' : 'mine'))

  const notices = pageRecords(inboxPage).slice(0, 3)

  return { kpis, overdueCount, tasks, notices, unreadCount }
}
