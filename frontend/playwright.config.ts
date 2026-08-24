import { defineConfig, devices } from '@playwright/test'

const isCI = !!process.env.CI
// No CI, a stack inteira sobe via docker-compose (frontend servido pelo
// Nginx em 8082); localmente, o Playwright sobe o servidor de dev do Vite.
const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? 'http://localhost:5173'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  workers: isCI ? 2 : 4,
  forbidOnly: isCI,
  retries: isCI ? 1 : 0,
  reporter: isCI ? [['list'], ['html', { open: 'never' }]] : 'list',
  expect: {
    // Padrão (5s) é justo demais sob contenção de CPU com vários Chromium
    // rodando em paralelo; navegação após submit de formulário pode passar
    // disso em máquinas mais lentas mesmo sem nada de errado no app.
    timeout: 10_000,
  },
  use: {
    baseURL,
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: isCI
    ? undefined
    : {
        command: 'npm run dev',
        url: baseURL,
        reuseExistingServer: true,
        timeout: 30_000,
      },
})
