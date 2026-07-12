/** 从 OpenAPI components/schemas 生成的实体类型 */

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

export interface SysUserRole {
  id?: number
  username?: string
  realName?: string
  roleId?: number
  roleName?: string
  roleCode?: string
  scopeType?: number
  scopeId?: number
  createTime?: string
}

export interface SysRole {
  id?: number
  roleName?: string
  roleCode?: string
  roleLevel?: number
  dataScope?: number
  description?: string
  status?: number
  createTime?: string
}

export interface SysRoleMenu {
  id?: number
  roleId?: number
  menuId?: number
  createTime?: string
}

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
  createTime?: string
  children?: SysMenu[]
}

export interface SysMajor {
  id?: number
  majorName?: string
  majorCode?: string
  collegeId?: number
  headTeacherId?: number
  status?: number
  createTime?: string
}

export interface SysDepartment {
  id?: number
  deptName?: string
  clubId?: number
  parentId?: number
  leaderId?: number
  description?: string
  status?: number
  createTime?: string
}

export interface SysDataPermission {
  id?: number
  roleId?: number
  tableName?: string
  fieldName?: string
  visible?: number
  conditionType?: number
  conditionValue?: string
  description?: string
  status?: number
  createTime?: string
}

export interface SysCollege {
  id?: number
  collegeName?: string
  collegeCode?: string
  deanId?: number
  status?: number
  createTime?: string
}

export interface SysClub {
  id?: number
  clubName?: string
  clubCode?: string
  category?: string
  collegeId?: number
  advisorId?: number
  description?: string
  logo?: string
  status?: number
  createTime?: string
}

export interface NoticeReadRecord {
  id?: number
  noticeId?: number
  username?: string
  readTime?: string
  confirmTime?: string
  isConfirmed?: number
}

export interface NoticeInfo {
  id?: number
  title?: string
  content?: string
  categoryId?: number
  publisherName?: string
  importance?: number
  urgency?: number
  receiverType?: number
  receiverValues?: string
  needConfirm?: number
  publishTime?: string
  expireTime?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface NoticeCategory {
  id?: number
  categoryName?: string
  priority?: number
  icon?: string
  status?: number
  createTime?: string
}

export interface ClubStatistics {
  id?: number
  clubId?: number
  statDate?: string
  totalMembers?: number
  newMembers?: number
  activityCount?: number
  totalParticipants?: number
  totalBudget?: number
  avgScore?: number
  createTime?: string
}

export interface ActivitySign {
  id?: number
  activityId?: number
  username?: string
  realName?: string
  signType?: number
  signTime?: string
  signLocation?: number
  address?: string
  signStatus?: number
  createTime?: string
}

export interface ActivityCategory {
  id?: number
  categoryName?: string
  approveFlowId?: number
  needLocation?: number
  needReport?: number
  status?: number
  createTime?: string
  codeSuffix?: string
}

export interface ActivityApproveFlow {
  id?: number
  activityId?: number
  step?: number
  approveRoleId?: number
  approveUsername?: string
  approveResult?: number
  approveOpinion?: string
  approveTime?: string
  createTime?: string
}

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
  safetyPlan?: string
  applyUsername?: string
  applyTime?: string
  currentApproveStep?: number
  approveStatus?: number
  rejectReason?: string
  createTime?: string
  updateTime?: string
}
