<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http, { extractErrorMessage } from '../lib/http'
import { useToastStore } from '../stores/toast'
import type { AppUser } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import Spinner from '../components/ui/Spinner.vue'

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
    await http.put('/users/me/password', { currentPassword: currentPassword.value, newPassword: newPassword.value })
    currentPassword.value = ''
    newPassword.value = ''
    toast.success('Senha alterada.')
  } catch (error) {
    passwordError.value = extractErrorMessage(error, 'Não foi possível alterar a senha.')
  } finally {
    savingPassword.value = false
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
    </template>
  </div>
</template>
