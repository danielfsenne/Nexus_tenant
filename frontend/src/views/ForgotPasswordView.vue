<script setup lang="ts">
import { ref } from 'vue'
import http from '../lib/http'
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
  <div class="flex min-h-screen items-center justify-center bg-slate-50 px-4 dark:bg-slate-900">
    <div class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-50">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500 dark:text-slate-400">Recuperar senha</p>

      <Alert v-if="submitted" variant="success">
        Se existir uma conta com esse e-mail, enviamos um link de redefinição de senha.
      </Alert>

      <form v-else class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput v-model="email" label="E-mail" type="email" required />

        <BaseButton type="submit" :loading="loading" class="w-full">
          {{ loading ? 'Enviando...' : 'Enviar link de redefinição' }}
        </BaseButton>
      </form>

      <p class="mt-4 text-center text-sm">
        <RouterLink to="/login" class="font-medium text-brand-600 hover:underline dark:text-brand-400">
          Voltar para o login
        </RouterLink>
      </p>
    </div>
  </div>
</template>
