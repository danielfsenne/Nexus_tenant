<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { extractErrorMessage } from '../lib/http'
import { useAuthStore } from '../stores/auth'
import type { AuthResponse } from '../stores/auth'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const token = String(route.query.token ?? '')
const name = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

async function handleSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    const { data } = await http.post<AuthResponse>('/invites/accept', {
      token,
      name: name.value,
      password: password.value,
    })
    auth.setAuth(data)
    router.push({ name: 'dashboard' })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível aceitar o convite.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    class="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-brand-50 px-4 dark:from-slate-950 dark:via-slate-950 dark:to-brand-950"
  >
    <div class="w-full max-w-sm">
      <div class="mb-8 flex flex-col items-center">
        <div
          class="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-lg font-bold text-white shadow-soft"
        >
          N
        </div>
        <h1 class="text-xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Nexus</h1>
      </div>

      <div class="rounded-2xl border border-slate-200/80 bg-white p-8 shadow-soft dark:border-slate-800 dark:bg-slate-900">
        <p class="mb-6 text-sm text-slate-500 dark:text-slate-400">Complete seu cadastro para entrar na equipe</p>

        <Alert v-if="!token" variant="error">Link de convite inválido.</Alert>

        <form v-else class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="name" label="Seu nome" required />
          <BaseInput v-model="password" label="Senha" type="password" :minlength="6" required />

          <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>

          <BaseButton type="submit" :loading="loading" class="w-full">
            {{ loading ? 'Entrando...' : 'Aceitar convite e entrar' }}
          </BaseButton>
        </form>
      </div>
    </div>
  </div>
</template>
