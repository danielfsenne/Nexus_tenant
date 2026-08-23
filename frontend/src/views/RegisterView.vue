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
  <div class="flex min-h-screen bg-white dark:bg-slate-950">
    <AuthBrandPanel
      title="Coloque sua empresa no Nexus em minutos."
      subtitle="Crie sua conta, convide sua equipe e comece a organizar clientes, produtos e vendas com controle de acesso por papel desde o primeiro dia."
    />

    <div class="flex flex-1 flex-col items-center justify-center px-4 py-12">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex flex-col items-center lg:items-start">
          <div
            class="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-lg font-bold text-white shadow-soft lg:hidden"
          >
            N
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Crie sua conta</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">Cadastre sua empresa para começar</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="companyName" label="Nome da empresa" required />
          <BaseInput v-model="adminName" label="Seu nome" required />
          <BaseInput v-model="email" label="E-mail" type="email" placeholder="voce@empresa.com" required />
          <BaseInput v-model="password" label="Senha" type="password" placeholder="••••••••" :minlength="6" required />

          <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>

          <BaseButton type="submit" :loading="loading" class="w-full">
            {{ loading ? 'Criando conta...' : 'Criar conta' }}
          </BaseButton>
        </form>

        <p class="mt-6 text-center text-sm text-slate-500 dark:text-slate-400 lg:text-left">
          Já tem uma conta?
          <RouterLink to="/login" class="font-semibold text-brand-600 hover:underline dark:text-brand-400">
            Entrar
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
