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
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível criar a conta. Verifique os dados.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-slate-50 px-4 dark:bg-slate-900">
    <div class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-50">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500 dark:text-slate-400">Cadastre sua empresa</p>

      <form class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput v-model="companyName" label="Nome da empresa" required />
        <BaseInput v-model="adminName" label="Seu nome" required />
        <BaseInput v-model="email" label="E-mail" type="email" required />
        <BaseInput v-model="password" label="Senha" type="password" :minlength="6" required />

        <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>

        <BaseButton type="submit" :loading="loading" class="w-full">
          {{ loading ? 'Criando conta...' : 'Criar conta' }}
        </BaseButton>
      </form>

      <p class="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
        Já tem uma conta?
        <RouterLink to="/login" class="font-medium text-brand-600 hover:underline dark:text-brand-400">
          Entrar
        </RouterLink>
      </p>
    </div>
  </div>
</template>
