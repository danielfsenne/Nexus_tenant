<script setup lang="ts">
import { useToastStore } from '../../stores/toast'

const toast = useToastStore()

const variantStyles = {
  success: {
    box: 'border-emerald-200 bg-white dark:border-emerald-500/30 dark:bg-slate-900',
    icon: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/10 dark:text-emerald-400',
    path: 'M5 13l4 4L19 7',
  },
  error: {
    box: 'border-red-200 bg-white dark:border-red-500/30 dark:bg-slate-900',
    icon: 'bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400',
    path: 'M6 18 18 6M6 6l12 12',
  },
  info: {
    box: 'border-slate-200 bg-white dark:border-slate-700 dark:bg-slate-900',
    icon: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300',
    path: 'M12 9v4m0 4h.01M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z',
  },
}
</script>

<template>
  <Teleport to="body">
    <div class="pointer-events-none fixed inset-x-0 top-4 z-[100] flex flex-col items-center gap-2 px-4 sm:items-end sm:right-4 sm:left-auto">
      <TransitionGroup
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0 -translate-y-2"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-for="item in toast.items"
          :key="item.id"
          class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border p-4 shadow-soft"
          :class="variantStyles[item.variant].box"
        >
          <span
            class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full"
            :class="variantStyles[item.variant].icon"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" :d="variantStyles[item.variant].path" />
            </svg>
          </span>
          <p class="mt-0.5 flex-1 text-sm text-slate-700 dark:text-slate-200">{{ item.message }}</p>
          <button
            class="shrink-0 rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800 dark:hover:text-slate-300"
            @click="toast.dismiss(item.id)"
          >
            <svg class="h-3.5 w-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>
