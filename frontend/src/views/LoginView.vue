<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { extractErrorMessage } from '../lib/http'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'

const auth = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

async function handleSubmit() {
  errorMessage.value = ''
  loading.value = true
  try {
    await auth.login({ email: email.value, password: password.value })
    router.push({ name: 'dashboard' })
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'E-mail ou senha inválidos.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-50 px-4 dark:bg-slate-900">
    <div class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-50">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500 dark:text-slate-400">Entre na sua conta</p>

      <form class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput v-model="email" label="E-mail" type="email" required />
        <BaseInput v-model="password" label="Senha" type="password" required />

        <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>

        <BaseButton type="submit" :loading="loading" class="w-full">
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </BaseButton>
      </form>

      <p class="mt-3 text-center text-sm">
        <RouterLink to="/esqueci-senha" class="font-medium text-slate-500 hover:underline dark:text-slate-400">
          Esqueci minha senha
        </RouterLink>
      </p>

      <p class="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
        Ainda não tem empresa cadastrada?
        <RouterLink to="/registro" class="font-medium text-brand-600 hover:underline dark:text-brand-400">
          Cadastre-se
        </RouterLink>
      </p>
    </div>
  </div>
</template>
