<script setup lang="ts">
defineProps<{ message: string }>()
</script>

<template>
  <div class="flex flex-col items-center gap-2 px-4 py-14 text-center">
    <svg
      class="h-9 w-9 text-slate-300 dark:text-slate-700"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.5"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        d="M20 13V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h7M16 3v4M8 3v4M4 11h16m-3 6 2 2m0 0 2 2m-2-2-2 2m2-2 2-2"
      />
    </svg>
    <p class="text-sm text-slate-400 dark:text-slate-500">{{ message }}</p>
  </div>
</template>
