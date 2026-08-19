<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http, { extractErrorMessage } from '../lib/http'
import type { AppUser, Invite } from '../types'

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
    <h1 class="mb-6 text-xl font-semibold text-slate-900">Usuários</h1>

    <form
      class="mb-6 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4"
      @submit.prevent="handleInvite"
    >
      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">E-mail</label>
        <input
          v-model="email"
          type="email"
          required
          class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
        />
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">Papel</label>
        <select
          v-model="role"
          class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
        >
          <option value="MANAGER">Manager</option>
          <option value="EMPLOYEE">Employee</option>
        </select>
      </div>

      <button
        type="submit"
        :disabled="saving"
        class="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
      >
        Convidar
      </button>

      <p v-if="errorMessage" class="w-full text-sm text-red-600">{{ errorMessage }}</p>
      <p v-if="successMessage" class="w-full text-sm text-emerald-600">{{ successMessage }}</p>
    </form>

    <div v-if="loading" class="text-sm text-slate-500">Carregando...</div>

    <template v-else>
      <h2 class="mb-2 text-sm font-semibold text-slate-900">Ativos</h2>
      <table class="mb-6 w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
        <thead class="bg-slate-50 text-left text-slate-500">
          <tr>
            <th class="px-4 py-2">Nome</th>
            <th class="px-4 py-2">E-mail</th>
            <th class="px-4 py-2">Papel</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id" class="border-t border-slate-100">
            <td class="px-4 py-2">{{ user.name }}</td>
            <td class="px-4 py-2">{{ user.email }}</td>
            <td class="px-4 py-2">{{ user.role }}</td>
          </tr>
        </tbody>
      </table>

      <h2 class="mb-2 text-sm font-semibold text-slate-900">Convites pendentes</h2>
      <table class="w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-sm">
        <thead class="bg-slate-50 text-left text-slate-500">
          <tr>
            <th class="px-4 py-2">E-mail</th>
            <th class="px-4 py-2">Papel</th>
            <th class="px-4 py-2">Expira em</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="invite in invites" :key="invite.id" class="border-t border-slate-100">
            <td class="px-4 py-2">{{ invite.email }}</td>
            <td class="px-4 py-2">{{ invite.role }}</td>
            <td class="px-4 py-2">{{ formatDate(invite.expiresAt) }}</td>
          </tr>
          <tr v-if="invites.length === 0">
            <td colspan="3" class="px-4 py-6 text-center text-slate-400">Nenhum convite pendente.</td>
          </tr>
        </tbody>
      </table>
    </template>
  </div>
</template>
