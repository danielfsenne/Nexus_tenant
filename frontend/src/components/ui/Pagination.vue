<script setup lang="ts">
const props = defineProps<{
  page: number
  totalPages: number
  totalElements: number
}>()

const emit = defineEmits<{ 'update:page': [value: number] }>()

function go(page: number) {
  if (page < 0 || page >= props.totalPages || page === props.page) return
  emit('update:page', page)
}
</script>

<template>
  <div
    v-if="totalElements > 0"
    class="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 px-4 py-3 dark:border-slate-800"
  >
    <p class="text-xs text-slate-500 dark:text-slate-400">
      {{ totalElements }} {{ totalElements === 1 ? 'registro' : 'registros' }} · página {{ page + 1 }} de {{ Math.max(totalPages, 1) }}
    </p>

    <div class="flex items-center gap-1">
      <button
        type="button"
        class="rounded-md px-2.5 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 disabled:opacity-40 disabled:hover:bg-transparent dark:text-slate-300 dark:hover:bg-slate-800"
        :disabled="page === 0"
        @click="go(page - 1)"
      >
        Anterior
      </button>
      <button
        type="button"
        class="rounded-md px-2.5 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 disabled:opacity-40 disabled:hover:bg-transparent dark:text-slate-300 dark:hover:bg-slate-800"
        :disabled="page >= totalPages - 1"
        @click="go(page + 1)"
      >
        Próxima
      </button>
    </div>
  </div>
</template>
