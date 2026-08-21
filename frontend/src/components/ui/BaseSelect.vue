<script setup lang="ts">
import { useId } from 'vue'

defineProps<{
  modelValue: string | number | null
  label?: string
  required?: boolean
}>()

defineEmits<{ 'update:modelValue': [value: string] }>()

const id = useId()
</script>

<template>
  <div>
    <label v-if="label" :for="id" class="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-300">
      {{ label }}
    </label>
    <select
      :id="id"
      :value="modelValue"
      :required="required"
      class="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100"
      @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <slot />
    </select>
  </div>
</template>
