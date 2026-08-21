<script setup lang="ts">
defineProps<{ message: string }>()
</script>

<template>
  <div class="px-4 py-10 text-center text-sm text-slate-400 dark:text-slate-500">
    {{ message }}
  </div>
</template>
