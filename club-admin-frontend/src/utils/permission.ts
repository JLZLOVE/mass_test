import type { App, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'
import { LEVEL } from '@/utils/level'

/** 角色 ID（兼容旧权限指令，与 effectiveLevel 并存） */
export const ROLE = {
  SUPER_ADMIN: 1,
  CLUB_PRESIDENT: 2,
  CLUB_MINISTER: 3,
  MEMBER: 4,
  ADVISOR: 5,
} as const

/** 是否可查看预算字段 */
export function canViewBudget(roleIds: number[], effectiveLevel = 4): boolean {
  if (effectiveLevel <= LEVEL.CLUB_LEADER) return true
  if (roleIds.includes(ROLE.SUPER_ADMIN) || roleIds.includes(ROLE.CLUB_PRESIDENT)) return true
  if (roleIds.includes(ROLE.ADVISOR)) return true
  return false
}

/** 预算是否只读 */
export function isBudgetReadonly(roleIds: number[], effectiveLevel = 4): boolean {
  if (effectiveLevel <= LEVEL.CLUB_LEADER) return false
  return roleIds.includes(ROLE.CLUB_MINISTER) && !canViewBudget(roleIds, effectiveLevel)
}

export function setupPermissionDirective(app: App) {
  app.directive('permission', {
    mounted(el: HTMLElement, binding: DirectiveBinding<number[]>) {
      const userStore = useUserStore()
      const required = binding.value ?? []
      if (required.length && !required.some((r) => userStore.roleIds.includes(r))) {
        el.parentNode?.removeChild(el)
      }
    },
  })
}
