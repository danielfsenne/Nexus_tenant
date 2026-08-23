<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { extractErrorMessage } from '../lib/http'
import AuthBrandPanel from '../components/AuthBrandPanel.vue'
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
  <div class="flex min-h-screen bg-white dark:bg-slate-950">
    <AuthBrandPanel
      title="Gerencie sua empresa com clareza e controle."
      subtitle="Clientes, produtos, vendas e equipe em um só lugar — com permissões por papel, auditoria completa e notificações em tempo real."
    />

    <div class="flex flex-1 flex-col items-center justify-center px-4 py-12">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex flex-col items-center lg:items-start">
          <div
            class="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-lg font-bold text-white shadow-soft lg:hidden"
          >
            N
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Bem-vindo de volta</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">Entre na sua conta para continuar</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="email" label="E-mail" type="email" placeholder="voce@empresa.com" required />
          <BaseInput v-model="password" label="Senha" type="password" placeholder="••••••••" required />

          <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>

          <BaseButton type="submit" :loading="loading" class="w-full">
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </BaseButton>
        </form>

        <p class="mt-4 text-center text-sm lg:text-left">
          <RouterLink
            to="/esqueci-senha"
            class="font-medium text-slate-500 hover:text-brand-600 hover:underline dark:text-slate-400 dark:hover:text-brand-400"
          >
            Esqueci minha senha
          </RouterLink>
        </p>

        <div class="mt-8 flex items-center gap-3 text-xs text-slate-400 dark:text-slate-600">
          <span class="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
          ou
          <span class="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
        </div>

        <p class="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
          Ainda não tem empresa cadastrada?
          <RouterLink to="/registro" class="font-semibold text-brand-600 hover:underline dark:text-brand-400">
            Cadastre-se
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
