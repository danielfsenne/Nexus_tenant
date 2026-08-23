<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http, { extractErrorMessage } from '../lib/http'
import type { AppUser, Invite } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseSelect from '../components/ui/BaseSelect.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import Spinner from '../components/ui/Spinner.vue'

const users = ref<AppUser[]>([])
const invites = ref<Invite[]>([])
const loading = ref(true)

const email = ref('')
const role = ref<'MANAGER' | 'EMPLOYEE'>('EMPLOYEE')
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

async function loadData() {
  loading.value = true
  const [usersRes, invitesRes] = await Promise.all([
    http.get<AppUser[]>('/users'),
    http.get<Invite[]>('/invites'),
  ])
  users.value = usersRes.data
  invites.value = invitesRes.data
  loading.value = false
}

async function handleInvite() {
  saving.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    await http.post('/invites', { email: email.value, role: role.value })
    successMessage.value = `Convite enviado para ${email.value}.`
    email.value = ''
    await loadData()
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível enviar o convite.')
  } finally {
    saving.value = false
  }
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('pt-BR')
}

onMounted(loadData)
</script>

<template>
  <div>
    <PageHeader title="Usuários" />

    <BaseCard class="mb-6 p-4">
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="handleInvite">
        <div class="min-w-48">
          <BaseInput v-model="email" label="E-mail" type="email" required />
        </div>

        <div class="w-40">
          <BaseSelect v-model="role" label="Papel">
            <option value="MANAGER">Manager</option>
            <option value="EMPLOYEE">Employee</option>
          </BaseSelect>
        </div>

        <BaseButton type="submit" :loading="saving">Convidar</BaseButton>

        <Alert v-if="errorMessage" variant="error" class="w-full">{{ errorMessage }}</Alert>
        <Alert v-if="successMessage" variant="success" class="w-full">{{ successMessage }}</Alert>
      </form>
    </BaseCard>

    <Spinner v-if="loading" />

    <template v-else>
      <h2 class="mb-2.5 text-sm font-semibold text-slate-900 dark:text-slate-50">Ativos</h2>
      <BaseCard class="mb-6 overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:bg-slate-800/60 dark:text-slate-400">
            <tr>
              <th class="px-4 py-3">Nome</th>
              <th class="px-4 py-3">E-mail</th>
              <th class="px-4 py-3">Papel</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="user in users"
              :key="user.id"
              class="border-t border-slate-100 transition-colors hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
            >
              <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{{ user.name }}</td>
              <td class="px-4 py-3 text-slate-600 dark:text-slate-400">{{ user.email }}</td>
              <td class="px-4 py-3">
                <span class="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-medium text-brand-700 dark:bg-brand-500/10 dark:text-brand-400">
                  {{ user.role }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>

      <h2 class="mb-2.5 text-sm font-semibold text-slate-900 dark:text-slate-50">Convites pendentes</h2>
      <BaseCard class="overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500 dark:bg-slate-800/60 dark:text-slate-400">
            <tr>
              <th class="px-4 py-3">E-mail</th>
              <th class="px-4 py-3">Papel</th>
              <th class="px-4 py-3">Expira em</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="invite in invites"
              :key="invite.id"
              class="border-t border-slate-100 transition-colors hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800/50"
            >
              <td class="px-4 py-3 font-medium text-slate-900 dark:text-slate-100">{{ invite.email }}</td>
              <td class="px-4 py-3 text-slate-600 dark:text-slate-400">{{ invite.role }}</td>
              <td class="px-4 py-3 text-slate-600 dark:text-slate-400">{{ formatDate(invite.expiresAt) }}</td>
            </tr>
          </tbody>
        </table>

        <EmptyState v-if="invites.length === 0" message="Nenhum convite pendente." />
      </BaseCard>
    </template>
  </div>
</template>
