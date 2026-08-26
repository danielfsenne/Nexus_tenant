<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http, { extractErrorMessage } from '../lib/http'
import { useAuthStore } from '../stores/auth'
import type { AuthResponse } from '../stores/auth'
import { useToastStore } from '../stores/toast'
import type { AppUser } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import Spinner from '../components/ui/Spinner.vue'

const auth = useAuthStore()
const router = useRouter()
const toast = useToastStore()

const loading = ref(true)
const email = ref('')
const role = ref('')

const name = ref('')
const savingProfile = ref(false)
const profileError = ref('')

const currentPassword = ref('')
const newPassword = ref('')
const savingPassword = ref(false)
const passwordError = ref('')

const loggingOutAll = ref(false)

async function loadMe() {
  loading.value = true
  try {
    const { data } = await http.get<AppUser>('/users/me')
    name.value = data.name
    email.value = data.email
    role.value = data.role
  } catch {
    // erro de rede/autenticação já é tratado pelo interceptor global
  } finally {
    loading.value = false
  }
}

async function handleSaveProfile() {
  savingProfile.value = true
  profileError.value = ''
  try {
    await http.put('/users/me', { name: name.value })
    toast.success('Dados atualizados.')
  } catch (error) {
    profileError.value = extractErrorMessage(error, 'Não foi possível salvar os dados.')
  } finally {
    savingProfile.value = false
  }
}

async function handleChangePassword() {
  savingPassword.value = true
  passwordError.value = ''
  try {
    const { data } = await http.put<AuthResponse>('/users/me/password', {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value,
    })
    // Trocar a senha revoga as demais sessões, então aplicamos o par de
    // tokens novo devolvido pra sessão atual continuar funcionando sem
    // precisar logar de novo.
    auth.setAuth(data)
    currentPassword.value = ''
    newPassword.value = ''
    toast.success('Senha alterada. Outros dispositivos precisarão logar novamente.')
  } catch (error) {
    passwordError.value = extractErrorMessage(error, 'Não foi possível alterar a senha.')
  } finally {
    savingPassword.value = false
  }
}

async function handleLogoutAll() {
  loggingOutAll.value = true
  try {
    await http.post('/users/me/logout-all')
    auth.logout()
    router.push({ name: 'login' })
    toast.success('Todas as sessões foram encerradas.')
  } catch {
    toast.error('Não foi possível encerrar as outras sessões.')
  } finally {
    loggingOutAll.value = false
  }
}

onMounted(loadMe)
</script>

<template>
  <div class="max-w-xl">
    <PageHeader title="Meu perfil" />

    <Spinner v-if="loading" />

    <template v-else>
      <BaseCard class="mb-6 p-5">
        <h2 class="mb-4 text-sm font-semibold text-slate-900 dark:text-slate-50">Dados pessoais</h2>

        <form class="space-y-4" @submit.prevent="handleSaveProfile">
          <BaseInput v-model="name" label="Nome" required />
          <BaseInput :model-value="email" label="E-mail" disabled />
          <BaseInput :model-value="role" label="Papel" disabled />

          <Alert v-if="profileError" variant="error">{{ profileError }}</Alert>

          <BaseButton type="submit" :loading="savingProfile">
            {{ savingProfile ? 'Salvando...' : 'Salvar' }}
          </BaseButton>
        </form>
      </BaseCard>

      <BaseCard class="p-5">
        <h2 class="mb-4 text-sm font-semibold text-slate-900 dark:text-slate-50">Alterar senha</h2>

        <form class="space-y-4" @submit.prevent="handleChangePassword">
          <BaseInput v-model="currentPassword" label="Senha atual" type="password" required />
          <BaseInput v-model="newPassword" label="Nova senha" type="password" :minlength="6" required />

          <Alert v-if="passwordError" variant="error">{{ passwordError }}</Alert>

          <BaseButton type="submit" :loading="savingPassword">
            {{ savingPassword ? 'Salvando...' : 'Alterar senha' }}
          </BaseButton>
        </form>
      </BaseCard>

      <BaseCard class="mt-6 p-5">
        <h2 class="mb-1 text-sm font-semibold text-slate-900 dark:text-slate-50">Sessões</h2>
        <p class="mb-4 text-sm text-slate-500 dark:text-slate-400">
          Encerra o acesso em todos os dispositivos e navegadores logados nesta conta, inclusive este.
        </p>

        <BaseButton variant="secondary" :loading="loggingOutAll" @click="handleLogoutAll">
          {{ loggingOutAll ? 'Encerrando...' : 'Sair de todos os dispositivos' }}
        </BaseButton>
      </BaseCard>
    </template>
  </div>
</template>
