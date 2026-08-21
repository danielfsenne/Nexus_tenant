import { defineStore } from 'pinia'
import { Client, type IMessage } from '@stomp/stompjs'
import { useAuthStore } from './auth'

export interface Notification {
  type: string
  message: string
  createdAt: string
}

const WS_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/^http/, 'ws') + '/ws'

export const useNotificationsStore = defineStore('notifications', {
  state: () => ({
    client: null as Client | null,
    connected: false,
    items: [] as Notification[],
    unreadCount: 0,
  }),

  actions: {
    connect() {
      if (this.client) return

      const auth = useAuthStore()
      if (!auth.token || !auth.tenantId) return

      const client = new Client({
        brokerURL: WS_URL,
        connectHeaders: { Authorization: `Bearer ${auth.token}` },
        reconnectDelay: 5000,
        onConnect: () => {
          this.connected = true
          client.subscribe(`/topic/tenant/${auth.tenantId}/notifications`, (message: IMessage) => {
            const notification = JSON.parse(message.body) as Notification
            this.items.unshift(notification)
            this.unreadCount += 1
          })
        },
        onDisconnect: () => {
          this.connected = false
        },
      })

      client.activate()
      this.client = client
    },

    disconnect() {
      this.client?.deactivate()
      this.client = null
      this.connected = false
    },

    markAllRead() {
      this.unreadCount = 0
    },
  },
})
