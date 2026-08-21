<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { extractErrorMessage } from '../lib/http'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'

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
  <div class="flex min-h-screen items-center justify-center bg-slate-50 px-4 dark:bg-slate-900">
    <div class="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-8 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <h1 class="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-50">Nexus</h1>
      <p class="mb-6 text-sm text-slate-500 dark:text-slate-400">Defina sua nova senha</p>

      <Alert v-if="!token" variant="error">Link de redefinição inválido.</Alert>

      <form v-else class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput v-model="newPassword" label="Nova senha" type="password" :minlength="6" required />

        <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>
        <Alert v-if="successMessage" variant="success">{{ successMessage }}</Alert>

        <BaseButton type="submit" :loading="loading" class="w-full">
          {{ loading ? 'Salvando...' : 'Redefinir senha' }}
        </BaseButton>
      </form>
    </div>
  </div>
</template>
