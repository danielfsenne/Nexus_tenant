import { onMounted, onUnmounted, type Ref } from 'vue'

export function onClickOutside(target: Ref<HTMLElement | null>, callback: () => void) {
  function handler(event: MouseEvent) {
    if (target.value && !target.value.contains(event.target as Node)) {
      callback()
    }
  }

  onMounted(() => document.addEventListener('click', handler))
  onUnmounted(() => document.removeEventListener('click', handler))
}
