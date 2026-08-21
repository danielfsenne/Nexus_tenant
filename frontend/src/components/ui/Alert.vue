<script setup lang="ts">
withDefaults(defineProps<{ variant?: 'error' | 'success' | 'info' }>(), { variant: 'info' })

const variants = {
  error: 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-300',
  success: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300',
  info: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
}
</script>

<template>
  <div class="rounded-md px-3 py-2 text-sm" :class="variants[variant]">
    <slot />
  </div>
</template>
