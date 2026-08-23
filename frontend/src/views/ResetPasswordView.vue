<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http, { extractErrorMessage } from '../lib/http'
import AuthBrandPanel from '../components/AuthBrandPanel.vue'
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
  <div class="flex min-h-screen bg-white dark:bg-slate-950">
    <AuthBrandPanel
      title="Última etapa para voltar ao trabalho."
      subtitle="Escolha uma nova senha segura para continuar gerenciando sua empresa no Nexus."
    />

    <div class="flex flex-1 flex-col items-center justify-center px-4 py-12">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex flex-col items-center lg:items-start">
          <div
            class="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-lg font-bold text-white shadow-soft lg:hidden"
          >
            N
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Defina sua nova senha</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">Crie uma senha forte para proteger sua conta</p>
        </div>

        <Alert v-if="!token" variant="error">Link de redefinição inválido.</Alert>

        <form v-else class="space-y-4" @submit.prevent="handleSubmit">
          <BaseInput v-model="newPassword" label="Nova senha" type="password" placeholder="••••••••" :minlength="6" required />

          <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>
          <Alert v-if="successMessage" variant="success">{{ successMessage }}</Alert>

          <BaseButton type="submit" :loading="loading" class="w-full">
            {{ loading ? 'Salvando...' : 'Redefinir senha' }}
          </BaseButton>
        </form>
      </div>
    </div>
  </div>
</template>
