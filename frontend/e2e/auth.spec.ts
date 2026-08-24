import { test, expect } from '@playwright/test'

function uniqueEmail(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 10000)}@e2e.teste`
}

test.describe('Autenticação', () => {
  test('registra uma nova empresa e vai para o dashboard', async ({ page }) => {
    const email = uniqueEmail('registro')

    await page.goto('/registro')
    await page.getByLabel('Nome da empresa').fill('Empresa E2E')
    await page.getByLabel('Seu nome').fill('Admin E2E')
    await page.getByLabel('E-mail').fill(email)
    await page.getByLabel('Senha').fill('senha123')
    await page.getByRole('button', { name: 'Criar conta' }).click()

    await expect(page).toHaveURL('/')
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  })

  test('login com senha errada mostra erro e não navega', async ({ page }) => {
    const email = uniqueEmail('login-errado')

    // Cria a conta primeiro para ter um e-mail válido para testar a senha errada.
    await page.goto('/registro')
    await page.getByLabel('Nome da empresa').fill('Empresa Login')
    await page.getByLabel('Seu nome').fill('Admin')
    await page.getByLabel('E-mail').fill(email)
    await page.getByLabel('Senha').fill('senhaCorreta1')
    await page.getByRole('button', { name: 'Criar conta' }).click()
    await expect(page).toHaveURL('/')

    await page.evaluate(() => localStorage.clear())
    await page.goto('/login')
    await page.getByLabel('E-mail').fill(email)
    await page.getByLabel('Senha').fill('senhaErrada')
    await page.getByRole('button', { name: 'Entrar' }).click()

    await expect(page.getByText('Credenciais inválidas')).toBeVisible()
    await expect(page).toHaveURL('/login')
  })

  test('login correto entra e logout volta para a tela de login', async ({ page }) => {
    const email = uniqueEmail('login-ok')

    await page.goto('/registro')
    await page.getByLabel('Nome da empresa').fill('Empresa Logout')
    await page.getByLabel('Seu nome').fill('Admin')
    await page.getByLabel('E-mail').fill(email)
    await page.getByLabel('Senha').fill('senha123')
    await page.getByRole('button', { name: 'Criar conta' }).click()
    await expect(page).toHaveURL('/')

    await page.getByTitle('Sair').click()
    await expect(page).toHaveURL('/login')

    await page.getByLabel('E-mail').fill(email)
    await page.getByLabel('Senha').fill('senha123')
    await page.getByRole('button', { name: 'Entrar' }).click()
    await expect(page).toHaveURL('/')
  })
})
