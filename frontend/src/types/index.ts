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
}

export interface Invite {
  id: number
  email: string
  role: 'ADMIN' | 'MANAGER' | 'EMPLOYEE'
  expiresAt: string
  createdAt: string
}

export interface TenantUsage {
  companyName: string
  plan: 'FREE' | 'PRO' | 'ENTERPRISE'
  usage: { users: number; customers: number; products: number }
  limits: { maxUsers: number | null; maxCustomers: number | null; maxProducts: number | null }
}
