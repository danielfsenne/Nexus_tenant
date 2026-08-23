<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    value: string | number
    accent?: 'brand' | 'emerald' | 'amber'
  }>(),
  { accent: 'brand' },
)

const dotColors = {
  brand: 'bg-brand-500',
  emerald: 'bg-emerald-500',
  amber: 'bg-amber-500',
}
</script>

<template>
  <div
    class="rounded-xl border border-slate-200/80 bg-white p-5 shadow-softer transition-shadow hover:shadow-soft dark:border-slate-800 dark:bg-slate-900"
  >
    <div class="flex items-center justify-between">
      <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ label }}</p>
      <span class="h-2 w-2 rounded-full" :class="dotColors[accent]" />
    </div>
    <p class="mt-2 text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50">{{ value }}</p>
  </div>
</template>
