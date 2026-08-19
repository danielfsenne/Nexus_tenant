<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  current: number
  max: number | null
}>()

const percentage = computed(() => {
  if (props.max === null) return 0
  if (props.max === 0) return 100
  return Math.min(100, Math.round((props.current / props.max) * 100))
})

const isNearLimit = computed(() => props.max !== null && percentage.value >= 80)
</script>

<template>
  <div>
    <div class="mb-1 flex items-center justify-between text-sm">
      <span class="text-slate-600">{{ label }}</span>
      <span class="text-slate-500">{{ current }} / {{ max ?? '∞' }}</span>
    </div>

    <div v-if="max !== null" class="h-2 w-full overflow-hidden rounded-full bg-slate-100">
      <div
        class="h-full rounded-full transition-all"
        :class="isNearLimit ? 'bg-red-500' : 'bg-slate-900'"
        :style="{ width: percentage + '%' }"
      />
    </div>
  </div>
</template>
