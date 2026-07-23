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
  scopeType?: number
  scopeId?: number
  createTime?: string
}

/** 菜单树返回 */
export interface MenuTreeResult {
  tree: SysMenu[]
  permissions: string[]
}