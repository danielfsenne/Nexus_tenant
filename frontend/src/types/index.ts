export interface Customer {
  id: number
  name: string
  email: string | null
  createdAt: string
}

export interface Product {
  id: number
  name: string
  price: number
  createdAt: string
}

export interface Order {
  id: number
  customerId: number
  total: number
  createdAt: string
}

export interface AppUser {
  id: number
  name: string
  email: string
  role: 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
  createdAt: string
  emailVerified: boolean
}

export interface Invite {
  id: number
  email: string
  role: 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
  expiresAt: string
  createdAt: string
}

export interface AuditLog {
  id: number
  userEmail: string | null
  action: 'CREATED' | 'UPDATED' | 'DELETED' | 'INVITED' | 'INVITE_ACCEPTED' | 'PROCESSED' | 'PROCESSING_FAILED'
  entityType: string
  entityId: number | null
  details: string | null
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface TenantUsage {
  companyName: string
  plan: 'FREE' | 'PRO' | 'ENTERPRISE'
  usage: { users: number; customers: number; products: number }
  limits: { maxUsers: number | null; maxCustomers: number | null; maxProducts: number | null }
}
