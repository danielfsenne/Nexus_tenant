<script setup lang="ts">
defineProps<{ title: string; subtitle: string }>()
</script>

<template>
  <div class="relative hidden w-1/2 flex-col justify-between overflow-hidden bg-gradient-to-br from-brand-600 via-brand-700 to-brand-900 p-12 text-white lg:flex">
    <div
      class="pointer-events-none absolute inset-0 opacity-20"
      style="background-image: radial-gradient(circle at 20% 20%, white 1px, transparent 1px), radial-gradient(circle at 80% 60%, white 1px, transparent 1px); background-size: 48px 48px, 64px 64px;"
    />
    <div class="pointer-events-none absolute -right-24 -top-24 h-96 w-96 rounded-full bg-white/10 blur-3xl" />
    <div class="pointer-events-none absolute -bottom-32 -left-16 h-80 w-80 rounded-full bg-brand-400/20 blur-3xl" />

    <div class="relative flex items-center gap-2.5">
      <div class="flex h-9 w-9 items-center justify-center rounded-lg bg-white/15 text-sm font-bold backdrop-blur">
        N
      </div>
      <span class="text-lg font-bold tracking-tight">Nexus</span>
    </div>

    <div class="relative max-w-md">
      <h2 class="text-3xl font-bold leading-tight tracking-tight">{{ title }}</h2>
      <p class="mt-4 text-sm leading-relaxed text-brand-100">{{ subtitle }}</p>
    </div>

    <p class="relative text-xs text-brand-200">© {{ new Date().getFullYear() }} Nexus. Todos os direitos reservados.</p>
  </div>
</template>
