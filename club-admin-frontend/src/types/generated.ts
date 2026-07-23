/** 系统用户 */
export interface SysUser {
  id?: number
  username?: string
  password?: string
  realName?: string
  gender?: number
  phone?: string
  email?: string
  avatar?: string
  userType?: number
  studentNo?: string
  teacherNo?: string
  idCard?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/** 系统菜单 */
export interface SysMenu {
  id?: number
  parentId?: number
  menuName?: string
  menuType?: number
  permissionCode?: string
  componentPath?: string
  routePath?: string
  icon?: string
  sort?: number
  status?: number
  children?: SysMenu[]
}

/** 用户角色关联 */
export interface SysUserRole {
  id?: number
  userId?: number
  username?: string
  realName?: string
  roleId?: number
  roleName?: string
  roleCode?: string
  roleLevel?: number
  scopeType?: number
  scopeId?: number
  createTime?: string
}

/** 菜单树返回 */
export interface MenuTreeResult {
  tree: SysMenu[]
  permissions: string[]
}

/** MyBatis-Plus 分页 */
export interface PageResult<T> {
  records: T[]
  total: number
  size?: number
  current?: number
  pages?: number
}

/** 活动申请 */
export interface ActivityApply {
  id?: number
  activityNo?: string
  clubId?: number
  activityName?: string
  categoryId?: number
  activityType?: number
  startTime?: string
  endTime?: string
  location?: string
  locationDetail?: string
  expectedPeople?: number
  budget?: number
  activityContent?: string
  coverImage?: string
  organizerNote?: string
  safetyPlan?: string
  attachment?: string
  applyUserId?: number
  applyUsername?: string
  applyTime?: string
  currentApproveStep?: number
  /** 1草稿 2待审批 3审批中 4已通过 5已驳回 6已取消 7变更审批中 */
  approveStatus?: number
  activityLevel?: number
  rejectReason?: string
  summaryContent?: string
  summaryAttachment?: string
  summaryUploadTime?: string
  createTime?: string
  updateTime?: string
}

/** 通知 */
export interface NoticeInfo {
  id?: number
  noticeNo?: string
  coverImage?: string
  viewCount?: number
  receiverCount?: number
  title?: string
  content?: string
  categoryId?: number
  publisherId?: number
  publisherName?: string
  importance?: number
  urgency?: number
  receiverType?: number
  needConfirm?: number
  publishTime?: string
  expireTime?: string
  status?: number
  isPinned?: number
  createTime?: string
  /** 前端扩展：是否未读（收件箱可能带） */
  readStatus?: number
}

/** 门户社团摘要 */
export interface PortalClub {
  id?: number
  clubName?: string
  category?: string
  status?: number
  logo?: string
  memberCount?: number
}

/** 工作台 KPI 卡片 */
export interface DashboardKpiCard {
  key: string
  label: string
  value: number | null
  /** 环比变化百分比，无数据时为 null */
  changePercent?: number | null
  route: string
  query?: Record<string, string>
}

/** 工作台任务流条目 */
export interface DashboardTaskItem {
  id: number
  title: string
  subtitle: string
  statusText: string
  /** 停留天数，用于超时样式 */
  stayDays: number
  route: string
  query?: Record<string, string>
}
