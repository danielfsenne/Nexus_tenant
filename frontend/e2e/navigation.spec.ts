import { test, expect, type Page } from '@playwright/test'

function uniqueEmail(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 10000)}@e2e.teste`
}

// Serial + uma única conta registrada em beforeAll: os 4 testes abaixo não
// precisam de dados isolados entre si, então compartilhar a conta evita
// múltiplos registros em paralelo esbarrando no rate-limit de /auth/register.
test.describe.serial('Navegação e preferências', () => {
  let page: Page

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage()
    await page.goto('/registro')
    await page.getByLabel('Nome da empresa').fill('Empresa Navegação')
    await page.getByLabel('Seu nome').fill('Admin E2E')
    await page.getByLabel('E-mail').fill(uniqueEmail('nav'))
    await page.getByLabel('Senha').fill('senha123')
    await page.getByRole('button', { name: 'Criar conta' }).click()
    await expect(page).toHaveURL('/')
  })

  test.afterAll(async () => {
    await page.close()
  })

  test('apenas a página atual fica marcada como ativa no menu', async () => {
    const dashboardLink = page.getByRole('link', { name: 'Dashboard' })
    const clientesLink = page.getByRole('link', { name: 'Clientes' })

    await expect(dashboardLink).toHaveClass(/bg-brand-50/)
    await expect(clientesLink).not.toHaveClass(/bg-brand-50/)

    await clientesLink.click()
    await expect(page).toHaveURL('/clientes')
    await expect(clientesLink).toHaveClass(/bg-brand-50/)
    await expect(dashboardLink).not.toHaveClass(/bg-brand-50/)

    await page.goto('/')
  })

  test('acessa o perfil pelo rodapé da sidebar e edita o próprio nome', async () => {
    await page.getByTitle('Meu perfil').click()
    await expect(page).toHaveURL('/perfil')
    await expect(page.getByRole('heading', { name: 'Meu perfil' })).toBeVisible()

    await page.getByLabel('Nome').fill('Nome Atualizado E2E')
    await page.getByRole('button', { name: 'Salvar' }).click()
    await expect(page.getByText('Dados atualizados.')).toBeVisible()

    await page.goto('/')
  })

  test('alternar tema escuro persiste após recarregar a página', async () => {
    const html = page.locator('html')
    await expect(html).not.toHaveClass(/dark/)

    await page.getByTitle('Modo escuro').click()
    await expect(html).toHaveClass(/dark/)

    await page.reload()
    await expect(html).toHaveClass(/dark/)

    // Volta pro claro para não afetar a leitura visual de outros testes.
    await page.getByTitle('Modo claro').click()
    await expect(html).not.toHaveClass(/dark/)
  })

  test('menu mobile abre e fecha ao navegar', async () => {
    await page.setViewportSize({ width: 375, height: 800 })

    const clientesLink = page.getByRole('link', { name: 'Clientes' })
    await expect(clientesLink).not.toBeInViewport()

    await page.getByTitle('Abrir menu').click()
    await expect(clientesLink).toBeInViewport()

    await clientesLink.click()
    await expect(page).toHaveURL('/clientes')
    await expect(clientesLink).not.toBeInViewport()
  })
})
