<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const companyName = ref('')
const adminName = ref('')
const email = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

async function handleSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    await auth.register({
      companyName: companyName.value,
      adminName: adminName.value,
      email: email.value,
      password: password.value,
    })
    router.push({ name: 'dashboard' })
  } catch {
    errorMessage.value = 'Não foi possível criar a conta. Verifique os dados.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-50">
    <div class="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500">Cadastre sua empresa</p>

      <form class="space-y-4" @submit.prevent="handleSubmit">
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Nome da empresa</label>
          <input
            v-model="companyName"
            type="text"
            required
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Seu nome</label>
          <input
            v-model="adminName"
            type="text"
            required
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">E-mail</label>
          <input
            v-model="email"
            type="email"
            required
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Senha</label>
          <input
            v-model="password"
            type="password"
            minlength="6"
            required
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <p v-if="errorMessage" class="text-sm text-red-600">{{ errorMessage }}</p>

        <button
          type="submit"
          :disabled="loading"
          class="w-full rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {{ loading ? 'Criando conta...' : 'Criar conta' }}
        </button>
      </form>

      <p class="mt-4 text-center text-sm text-slate-500">
        Já tem uma conta?
        <RouterLink to="/login" class="font-medium text-slate-900 hover:underline">
          Entrar
        </RouterLink>
      </p>
    </div>
  </div>
</template>
