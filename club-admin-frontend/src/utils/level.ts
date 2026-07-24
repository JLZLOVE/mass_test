import type { SysUserRole } from '@/types/generated'

/** 有效等级：数值越小权限越高（与后端 Level / UserScopeResolver 对齐） */
export const LEVEL = {
  SUPER_ADMIN: 0,
  ADMIN: 1,
  CLUB_LEADER: 2,
  DEPT_LEADER: 3,
  STUDENT: 4,
} as const

export type EffectiveLevel = (typeof LEVEL)[keyof typeof LEVEL]

const ROLE_CODE_LEVEL: Record<string, number> = {
  SUPER_ADMIN: 0,
  ADMIN: 1,
  ADVISOR: 1,
  CLUB_PRESIDENT: 2,
  CLUB_MINISTER: 3,
  MEMBER: 4,
}

const LEVEL_LABEL: Record<number, string> = {
  0: '超级管理员',
  1: '院长/指导老师',
  2: '社团社长',
  3: '部门部长',
  4: '普通学生',
}

/** 是否为指导老师角色码（ADVISOR / ADVISOR_*） */
export function isAdvisorRoleCode(roleCode?: string | null): boolean {
  if (!roleCode) return false
  return roleCode.toUpperCase().startsWith('ADVISOR')
}

function isPresidentRole(r: SysUserRole): boolean {
  const code = (r.roleCode || '').toUpperCase()
  const name = r.roleName || ''
  return code === 'CLUB_PRESIDENT' || name.includes('社长')
}

function isMinisterRole(r: SysUserRole): boolean {
  const code = (r.roleCode || '').toUpperCase()
  const name = r.roleName || ''
  return code === 'CLUB_MINISTER' || name.includes('部长')
}

/** 单个角色码 → 有效等级 */
export function mapRoleCodeToLevel(roleCode?: string | null, roleLevel?: number | null, roleName?: string | null): number {
  if (roleCode) {
    const mapped = ROLE_CODE_LEVEL[roleCode.toUpperCase()]
    if (mapped !== undefined) return mapped
    if (isAdvisorRoleCode(roleCode)) return LEVEL.ADMIN
  }
  const name = roleName || ''
  if (name.includes('超级')) return LEVEL.SUPER_ADMIN
  if (name.includes('指导') || name.includes('学院管理') || name.includes('管理员')) return LEVEL.ADMIN
  if (name.includes('社长')) return LEVEL.CLUB_LEADER
  if (name.includes('部长')) return LEVEL.DEPT_LEADER
  if (name.includes('成员') || name.includes('学生')) return LEVEL.STUDENT
  if (roleLevel != null) return roleLevel
  return LEVEL.STUDENT
}

/** 多角色取最高权限（数值最小） */
export function resolveEffectiveLevel(roles: SysUserRole[]): number {
  if (!roles.length) return LEVEL.STUDENT
  let min: number = LEVEL.STUDENT
  for (const r of roles) {
    min = Math.min(min, mapRoleCodeToLevel(r.roleCode, r.roleLevel, r.roleName))
  }
  return min
}

export function levelLabel(level: number): string {
  return LEVEL_LABEL[level] ?? `Level ${level}`
}

/** 取当前生效角色展示名（优先最高权限角色） */
export function resolvePrimaryRoleName(roles: SysUserRole[], level: number): string {
  const matched = roles.find((r) => mapRoleCodeToLevel(r.roleCode, r.roleLevel, r.roleName) === level)
  return matched?.roleName || levelLabel(level)
}

/** 社长可切换的社团 scope 列表 */
export function resolveClubScopes(roles: SysUserRole[]): number[] {
  const presidentRoleIds = new Set(
    roles.filter(isPresidentRole).map((r) => r.roleId).filter(Boolean),
  )
  return roles
    .filter((r) => presidentRoleIds.has(r.roleId) && r.scopeType === 2 && r.scopeId)
    .map((r) => r.scopeId!)
}

export function resolvePrimaryDepartmentId(roles: SysUserRole[]): number | null {
  const ministerRoleIds = new Set(
    roles.filter(isMinisterRole).map((r) => r.roleId).filter(Boolean),
  )
  const hit = roles.find(
    (r) => ministerRoleIds.has(r.roleId) && r.scopeType === 3 && r.scopeId,
  )
  return hit?.scopeId ?? null
}
