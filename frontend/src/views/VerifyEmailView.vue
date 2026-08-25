<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http, { extractErrorMessage } from '../lib/http'
import AuthBrandPanel from '../components/AuthBrandPanel.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import Spinner from '../components/ui/Spinner.vue'

const route = useRoute()

const token = String(route.query.token ?? '')
const loading = ref(true)
const errorMessage = ref('')
const successMessage = ref('')

async function verify() {
  if (!token) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    await http.post('/auth/verify-email', { token })
    successMessage.value = 'E-mail verificado com sucesso.'
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível verificar o e-mail.')
  } finally {
    loading.value = false
  }
}

onMounted(verify)
</script>

<template>
  <div class="flex min-h-screen bg-white dark:bg-slate-950">
    <AuthBrandPanel
      title="Confirmando seu e-mail."
      subtitle="Só mais um instante para garantir que essa conta é mesmo sua."
    />

    <div class="flex flex-1 flex-col items-center justify-center px-4 py-12">
      <div class="w-full max-w-sm">
        <div class="mb-8 flex flex-col items-center lg:items-start">
          <div
            class="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-lg font-bold text-white shadow-soft lg:hidden"
          >
            N
          </div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Verificação de e-mail</h1>
        </div>

        <Alert v-if="!token" variant="error">Link de verificação inválido.</Alert>

        <template v-else>
          <Spinner v-if="loading" />

          <template v-else>
            <Alert v-if="successMessage" variant="success">{{ successMessage }}</Alert>
            <Alert v-if="errorMessage" variant="error">{{ errorMessage }}</Alert>
          </template>
        </template>

        <RouterLink :to="{ name: 'login' }" class="mt-6 block">
          <BaseButton class="w-full" variant="secondary">Ir para o login</BaseButton>
        </RouterLink>
      </div>
    </div>
  </div>
</template>
