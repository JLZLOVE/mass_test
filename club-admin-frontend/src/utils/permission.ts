import type { App, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/** 角色等级：数字越小权限越高 */
export const ROLE = {
  SUPER_ADMIN: 1,
  CLUB_PRESIDENT: 2,
  CLUB_MINISTER: 3,
  MEMBER: 4,
  ADVISOR: 5,
} as const

/** 是否可查看预算字段 */
export function canViewBudget(roleIds: number[]): boolean {
  if (roleIds.includes(ROLE.SUPER_ADMIN) || roleIds.includes(ROLE.CLUB_PRESIDENT)) return true
  if (roleIds.includes(ROLE.ADVISOR)) return true
  return false
}

/** 预算是否只读 */
export function isBudgetReadonly(roleIds: number[]): boolean {
  return roleIds.includes(ROLE.CLUB_MINISTER) && !canViewBudget(roleIds)
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
