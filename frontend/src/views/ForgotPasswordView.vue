<script setup lang="ts">
import { ref } from 'vue'
import http from '../lib/http'

const email = ref('')
const loading = ref(false)
const submitted = ref(false)

async function handleSubmit() {
  loading.value = true
  try {
    await http.post('/auth/forgot-password', { email: email.value })
  } finally {
    loading.value = false
    submitted.value = true
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-50">
    <div class="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500">Recuperar senha</p>

      <div v-if="submitted" class="text-sm text-slate-600">
        Se existir uma conta com esse e-mail, enviamos um link de redefinição de senha.
      </div>

      <form v-else class="space-y-4" @submit.prevent="handleSubmit">
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">E-mail</label>
          <input
            v-model="email"
            type="email"
            required
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {{ loading ? 'Enviando...' : 'Enviar link de redefinição' }}
        </button>
      </form>

      <p class="mt-4 text-center text-sm text-slate-500">
        <RouterLink to="/login" class="font-medium text-slate-900 hover:underline">Voltar para o login</RouterLink>
      </p>
    </div>
  </div>
</template>
