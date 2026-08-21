<script setup lang="ts">
defineProps<{ label: string; value: string | number }>()
</script>

<template>
  <div class="rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-800">
    <p class="text-sm text-slate-500 dark:text-slate-400">{{ label }}</p>
    <p class="mt-1 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ value }}</p>
  </div>
</template>
