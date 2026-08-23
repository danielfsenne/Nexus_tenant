<script setup lang="ts">
import { useId } from 'vue'

defineProps<{
  modelValue: string | number | null
  label?: string
  type?: string
  placeholder?: string
  required?: boolean
  minlength?: number
  min?: number | string
  step?: string
  error?: string
}>()

defineEmits<{ 'update:modelValue': [value: string] }>()

const id = useId()
</script>

<template>
  <div>
    <label v-if="label" :for="id" class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
      {{ label }}
    </label>
    <input
      :id="id"
      :type="type ?? 'text'"
      :value="modelValue"
      :placeholder="placeholder"
      :required="required"
      :minlength="minlength"
      :min="min"
      :step="step"
      class="w-full rounded-lg border bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 transition-colors focus:outline-none focus:ring-2 dark:bg-slate-800 dark:text-slate-100 dark:placeholder:text-slate-500"
      :class="
        error
          ? 'border-red-400 focus:ring-red-400'
          : 'border-slate-300 focus:border-brand-500 focus:ring-brand-500/40 dark:border-slate-700'
      "
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <p v-if="error" class="mt-1 text-xs text-red-600 dark:text-red-400">{{ error }}</p>
  </div>
</template>
