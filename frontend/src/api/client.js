// Local dev: Vite proxies /api → localhost:8080
// Production: set VITE_API_URL in Vercel (e.g. https://your-app.back4app.io/api)
const BASE = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '')

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  if (res.status === 204) {
    if (!res.ok) throw new Error('Request failed')
    return null
  }
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    throw new Error(data.message || 'Request failed')
  }
  return data
}

export const api = {
  getProfile: (username) => request(`/users/${encodeURIComponent(username)}/profile`),
  getSolved: (username) => request(`/users/${encodeURIComponent(username)}/solved`),
  getSheets: () => request('/sheets'),
  getSheetProgress: (username) => request(`/sheets/progress/${encodeURIComponent(username)}`),
  getSheetDetail: (sheetId, username) =>
    request(`/sheets/${sheetId}/progress/${encodeURIComponent(username)}`),
  getBadges: (username) => request(`/badges/${encodeURIComponent(username)}`),
  generateContest: (body) =>
    request('/contests/generate', { method: 'POST', body: JSON.stringify(body) }),
  importList: (url) =>
    request('/lists/import', { method: 'POST', body: JSON.stringify({ url }) }),
  getImportedLists: () => request('/lists'),
  getImportedList: (listId) => request(`/lists/${encodeURIComponent(listId)}`),
  getImportedListProgress: (listId, username) =>
    request(`/lists/${encodeURIComponent(listId)}/progress/${encodeURIComponent(username)}`),
  deleteImportedList: (listId) =>
    request(`/lists/${encodeURIComponent(listId)}`, { method: 'DELETE' }),
}
