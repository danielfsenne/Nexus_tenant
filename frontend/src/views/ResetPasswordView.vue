<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { extractErrorMessage } from '../lib/http'

const route = useRoute()
const router = useRouter()

const token = String(route.query.token ?? '')
const newPassword = ref('')
const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

async function handleSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    await http.post('/auth/reset-password', { token, newPassword: newPassword.value })
    successMessage.value = 'Senha redefinida com sucesso.'
    setTimeout(() => router.push({ name: 'login' }), 1500)
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível redefinir a senha.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-50">
    <div class="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500">Defina sua nova senha</p>

      <p v-if="!token" class="text-sm text-red-600">Link de redefinição inválido.</p>

      <form v-else class="space-y-4" @submit.prevent="handleSubmit">
        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Nova senha</label>
          <input
            v-model="newPassword"
            type="password"
            minlength="6"
            required
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <p v-if="errorMessage" class="text-sm text-red-600">{{ errorMessage }}</p>
        <p v-if="successMessage" class="text-sm text-emerald-600">{{ successMessage }}</p>

        <button
          type="submit"
          :disabled="loading"
          class="w-full rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {{ loading ? 'Salvando...' : 'Redefinir senha' }}
        </button>
      </form>
    </div>
  </div>
</template>
