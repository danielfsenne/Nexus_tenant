import http from './http'

export async function downloadCsv(url: string, params: Record<string, unknown>, filename: string) {
  const { data } = await http.get<Blob>(url, { params, responseType: 'blob' })
  const objectUrl = URL.createObjectURL(data)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(objectUrl)
}
