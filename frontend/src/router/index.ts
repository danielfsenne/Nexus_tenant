import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/registro',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { public: true },
    },
    {
      path: '/aceitar-convite',
      name: 'accept-invite',
      component: () => import('../views/AcceptInviteView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('../layouts/DefaultLayout.vue'),
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
        },
        {
          path: 'clientes',
          name: 'customers',
          component: () => import('../views/CustomersView.vue'),
        },
        {
          path: 'produtos',
          name: 'products',
          component: () => import('../views/ProductsView.vue'),
        },
        {
          path: 'vendas',
          name: 'orders',
          component: () => import('../views/OrdersView.vue'),
        },
        {
          path: 'usuarios',
          name: 'users',
          component: () => import('../views/UsersView.vue'),
          meta: { requiresAdmin: true },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (!to.meta.public && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.public && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresAdmin && auth.role !== 'ADMIN') {
    return { name: 'dashboard' }
  }

  return true
})

export default router
