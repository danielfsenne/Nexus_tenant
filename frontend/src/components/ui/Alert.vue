<script setup lang="ts">
withDefaults(defineProps<{ variant?: 'error' | 'success' | 'info' }>(), { variant: 'info' })

const variants = {
  error: 'border-red-400 bg-red-50 text-red-700 dark:border-red-500/60 dark:bg-red-950/40 dark:text-red-300',
  success:
    'border-emerald-400 bg-emerald-50 text-emerald-700 dark:border-emerald-500/60 dark:bg-emerald-950/40 dark:text-emerald-300',
  info: 'border-slate-300 bg-slate-50 text-slate-700 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300',
}
</script>

<template>
  <div class="rounded-lg border-l-4 px-3.5 py-2.5 text-sm leading-relaxed" :class="variants[variant]">
    <slot />
  </div>
</template>
