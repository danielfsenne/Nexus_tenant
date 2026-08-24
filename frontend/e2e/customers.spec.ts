import { test, expect, Page } from '@playwright/test'

function uniqueEmail(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 10000)}@e2e.teste`
}

async function registrarNovaEmpresa(page: Page, prefix: string) {
  await page.goto('/registro')
  await page.getByLabel('Nome da empresa').fill(`Empresa ${prefix}`)
  await page.getByLabel('Seu nome').fill('Admin E2E')
  await page.getByLabel('E-mail').fill(uniqueEmail(prefix))
  await page.getByLabel('Senha').fill('senha123')
  await page.getByRole('button', { name: 'Criar conta' }).click()
  await expect(page).toHaveURL('/')
}

test.describe('CRUD de clientes', () => {
  test.beforeEach(async ({ page }) => {
    await registrarNovaEmpresa(page, 'clientes')
    await page.getByRole('link', { name: 'Clientes' }).click()
    await expect(page).toHaveURL('/clientes')
  })

  test('cria, edita e exclui um cliente', async ({ page }) => {
    const nomeOriginal = `Cliente ${Date.now()}`
    const nomeEditado = `${nomeOriginal} Editado`

    // Cria.
    await page.getByLabel('Nome').fill(nomeOriginal)
    await page.getByLabel('E-mail').fill('cliente@e2e.teste')
    await page.getByRole('button', { name: 'Adicionar' }).click()

    const linha = page.locator('tr', { hasText: nomeOriginal })
    await expect(linha).toBeVisible()
    await expect(page.getByText('Cliente adicionado.')).toBeVisible()

    // Edita.
    await linha.getByRole('button', { name: 'Editar' }).click()
    const campoNome = page.getByLabel('Nome')
    await expect(campoNome).toHaveValue(nomeOriginal)
    await campoNome.fill(nomeEditado)
    await page.getByRole('button', { name: 'Salvar' }).click()

    await expect(page.locator('tr', { hasText: nomeEditado })).toBeVisible()
    await expect(page.getByText('Cliente atualizado.')).toBeVisible()

    // Exclui — pede confirmação antes de efetivar.
    const linhaEditada = page.locator('tr', { hasText: nomeEditado })
    await linhaEditada.getByRole('button', { name: 'Excluir' }).click()

    const dialog = page.getByText('Excluir cliente')
    await expect(dialog).toBeVisible()

    // Cancelar não remove o cliente.
    await page.getByRole('button', { name: 'Cancelar' }).click()
    await expect(page.locator('tr', { hasText: nomeEditado })).toBeVisible()

    // Confirmar remove de verdade.
    await linhaEditada.getByRole('button', { name: 'Excluir' }).click()
    await page.getByRole('button', { name: 'Excluir', exact: true }).last().click()

    await expect(page.locator('tr', { hasText: nomeEditado })).toHaveCount(0)
    await expect(page.getByText(`Cliente '${nomeEditado}' excluído.`)).toBeVisible()
  })

  test('lista vazia mostra a mensagem de nenhum cliente', async ({ page }) => {
    await expect(page.getByText('Nenhum cliente cadastrado.')).toBeVisible()
  })
})
