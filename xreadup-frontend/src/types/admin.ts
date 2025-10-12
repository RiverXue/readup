// 管理员角色类型 - 与backbuilding.md文档保持一致
export type AdminRole = 'SUPER_ADMIN' | 'ADMIN';

// 管理员权限类型
export type AdminPermission = 'USER_MANAGE' | 'ARTICLE_MANAGE' | 'SUBSCRIPTION_MANAGE' | 'AI_RESULT_MANAGE' | 'ADMIN_MANAGE';

// 管理员用户类型
export interface AdminUser {
  id: string;
  userId: string;
  role: AdminRole;
  permissions?: string | AdminPermission[];
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}

// 管理员会话状态
export interface AdminSession {
  isAdmin: boolean;
  role: AdminRole | null;
  permissions: AdminPermission[];
  lastLoginTime: string;
  sessionExpiredAt: string;
  userId?: string; // 可选的用户ID字段，用于会话验证
}

// 管理员API响应类型
export interface AdminApiResponse<T> {
  code: number;
  message: string;
  data: T;
  success: boolean;
}