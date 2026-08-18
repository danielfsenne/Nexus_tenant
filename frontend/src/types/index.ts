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
