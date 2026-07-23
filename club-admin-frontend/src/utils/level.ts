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

/** 单个角色码 → 有效等级 */
export function mapRoleCodeToLevel(roleCode?: string | null, roleLevel?: number | null): number {
  if (!roleCode) return roleLevel ?? LEVEL.STUDENT
  const mapped = ROLE_CODE_LEVEL[roleCode]
  if (mapped !== undefined) return mapped
  if (isAdvisorRoleCode(roleCode)) return LEVEL.ADMIN
  if (roleLevel != null) return roleLevel
  return LEVEL.STUDENT
}

/** 多角色取最高权限（数值最小） */
export function resolveEffectiveLevel(roles: SysUserRole[]): number {
  if (!roles.length) return LEVEL.STUDENT
  let min: number = LEVEL.STUDENT
  for (const r of roles) {
    min = Math.min(min, mapRoleCodeToLevel(r.roleCode, r.roleLevel))
  }
  return min
}

export function levelLabel(level: number): string {
  return LEVEL_LABEL[level] ?? `Level ${level}`
}

/** 取当前生效角色展示名（优先最高权限角色） */
export function resolvePrimaryRoleName(roles: SysUserRole[], level: number): string {
  const matched = roles.find((r) => mapRoleCodeToLevel(r.roleCode) === level)
  return matched?.roleName || levelLabel(level)
}

/** 社长可切换的社团 scope 列表 */
export function resolveClubScopes(roles: SysUserRole[]): number[] {
  const presidentRoleIds = new Set(
    roles.filter((r) => r.roleCode === 'CLUB_PRESIDENT').map((r) => r.roleId).filter(Boolean),
  )
  return roles
    .filter((r) => presidentRoleIds.has(r.roleId) && r.scopeType === 2 && r.scopeId)
    .map((r) => r.scopeId!)
}

export function resolvePrimaryDepartmentId(roles: SysUserRole[]): number | null {
  const ministerRoleIds = new Set(
    roles.filter((r) => r.roleCode === 'CLUB_MINISTER').map((r) => r.roleId).filter(Boolean),
  )
  const hit = roles.find(
    (r) => ministerRoleIds.has(r.roleId) && r.scopeType === 3 && r.scopeId,
  )
  return hit?.scopeId ?? null
}
