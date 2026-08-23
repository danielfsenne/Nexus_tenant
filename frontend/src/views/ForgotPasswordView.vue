<script setup lang="ts">
import { ref } from 'vue'
import http from '../lib/http'
import AuthBrandPanel from '../components/AuthBrandPanel.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'

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
  <div class="flex min-h-screen bg-white dark:bg-slate-950">
    <AuthBrandPanel
      title="Acontece com todo mundo."
      subtitle="Informe seu e-mail e enviaremos um link seguro para você criar uma nova senha e voltar a acessar sua conta."
    />

    <div class="flex flex-1 flex-col items-center justify-center px-4 py-12">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex flex-col items-center lg:items-start">
          <div
            class="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-lg font-bold text-white shadow-soft lg:hidden"
          >
            N
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Recuperar senha</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">Enviaremos um link de redefinição para seu e-mail</p>
        </div>

        <Alert v-if="submitted" variant="success">
          Se existir uma conta com esse e-mail, enviamos um link de redefinição de senha.
        </Alert>

        <form v-else class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="email" label="E-mail" type="email" placeholder="voce@empresa.com" required />

          <BaseButton type="submit" :loading="loading" class="w-full">
            {{ loading ? 'Enviando...' : 'Enviar link de redefinição' }}
          </BaseButton>
        </form>

        <p class="mt-4 text-center text-sm lg:text-left">
          <RouterLink to="/login" class="font-medium text-brand-600 hover:underline dark:text-brand-400">
            Voltar para o login
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
