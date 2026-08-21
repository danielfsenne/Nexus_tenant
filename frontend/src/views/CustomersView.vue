<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http, { extractErrorMessage } from '../lib/http'
import type { Customer } from '../types'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import Alert from '../components/ui/Alert.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import Spinner from '../components/ui/Spinner.vue'

const customers = ref<Customer[]>([])
const loading = ref(true)

const name = ref('')
const email = ref('')
const editingId = ref<number | null>(null)
const saving = ref(false)
const errorMessage = ref('')

async function loadCustomers() {
  loading.value = true
  const { data } = await http.get<Customer[]>('/customers')
  customers.value = data
  loading.value = false
}

async function handleSubmit() {
  saving.value = true
  errorMessage.value = ''
  try {
    if (editingId.value) {
      await http.put(`/customers/${editingId.value}`, { name: name.value, email: email.value })
    } else {
      await http.post('/customers', { name: name.value, email: email.value })
    }
    resetForm()
    await loadCustomers()
  } catch (error) {
    errorMessage.value = extractErrorMessage(error, 'Não foi possível salvar o cliente.')
  } finally {
    saving.value = false
  }
}

function editCustomer(customer: Customer) {
  editingId.value = customer.id
  name.value = customer.name
  email.value = customer.email ?? ''
}

function resetForm() {
  editingId.value = null
  name.value = ''
  email.value = ''
}

async function deleteCustomer(id: number) {
  await http.delete(`/customers/${id}`)
  await loadCustomers()
}

onMounted(loadCustomers)
</script>

<template>
  <div>
    <PageHeader title="Clientes" />

    <BaseCard class="mb-6 p-4">
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="handleSubmit">
        <div class="min-w-48">
          <BaseInput v-model="name" label="Nome" required />
        </div>
        <div class="min-w-48">
          <BaseInput v-model="email" label="E-mail" type="email" />
        </div>

        <BaseButton type="submit" :loading="saving">
          {{ editingId ? 'Salvar' : 'Adicionar' }}
        </BaseButton>

        <BaseButton v-if="editingId" type="button" variant="secondary" @click="resetForm">
          Cancelar
        </BaseButton>

        <Alert v-if="errorMessage" variant="error" class="w-full">{{ errorMessage }}</Alert>
      </form>
    </BaseCard>

    <Spinner v-if="loading" />

    <BaseCard v-else class="overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-50 text-left text-slate-500 dark:bg-slate-900/40 dark:text-slate-400">
          <tr>
            <th class="px-4 py-2">Nome</th>
            <th class="px-4 py-2">E-mail</th>
            <th class="px-4 py-2"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="customer in customers"
            :key="customer.id"
            class="border-t border-slate-100 dark:border-slate-700"
          >
            <td class="px-4 py-2 text-slate-900 dark:text-slate-100">{{ customer.name }}</td>
            <td class="px-4 py-2 text-slate-600 dark:text-slate-400">{{ customer.email }}</td>
            <td class="px-4 py-2 text-right">
              <button class="text-slate-600 hover:underline dark:text-slate-400" @click="editCustomer(customer)">
                Editar
              </button>
              <button class="ml-3 text-red-600 hover:underline dark:text-red-400" @click="deleteCustomer(customer.id)">
                Excluir
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <EmptyState v-if="customers.length === 0" message="Nenhum cliente cadastrado." />
    </BaseCard>
  </div>
</template>
