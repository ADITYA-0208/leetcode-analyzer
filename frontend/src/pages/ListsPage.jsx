import { useEffect, useState } from 'react'
import DifficultyBadge from '../components/DifficultyBadge'
import { api } from '../api/client'
import { useUser } from '../context/UserContext'

export default function ListsPage() {
  const { username } = useUser()
  const [url, setUrl] = useState('')
  const [lists, setLists] = useState([])
  const [activeListId, setActiveListId] = useState(null)
  const [listDetail, setListDetail] = useState(null)
  const [progress, setProgress] = useState(null)
  const [importing, setImporting] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [filter, setFilter] = useState('')

  async function loadLists() {
    try {
      const data = await api.getImportedLists()
      setLists(data)
    } catch {
      setLists([])
    }
  }

  useEffect(() => {
    loadLists()
  }, [])

  useEffect(() => {
    if (!activeListId) {
      setListDetail(null)
      setProgress(null)
      return
    }
    setLoading(true)
    Promise.all([
      api.getImportedList(activeListId),
      username ? api.getImportedListProgress(activeListId, username).catch(() => null) : Promise.resolve(null),
    ])
      .then(([detail, prog]) => {
        setListDetail(detail)
        setProgress(prog)
      })
      .catch(() => {
        setListDetail(null)
        setProgress(null)
      })
      .finally(() => setLoading(false))
  }, [activeListId, username])

  async function handleImport(e) {
    e.preventDefault()
    if (!url.trim()) return
    setImporting(true)
    setError(null)
    try {
      const imported = await api.importList(url.trim())
      await loadLists()
      setActiveListId(imported.id)
      setUrl('')
    } catch (err) {
      setError(err.message)
    } finally {
      setImporting(false)
    }
  }

  async function handleDelete(id) {
    try {
      await api.deleteImportedList(id)
      if (activeListId === id) setActiveListId(null)
      await loadLists()
    } catch (err) {
      setError(err.message)
    }
  }

  const displayProblems = progress?.problems ?? listDetail?.problems ?? []
  const filtered = displayProblems.filter((p) =>
    p.title.toLowerCase().includes(filter.toLowerCase())
  )

  return (
    <div className="page lists-page">
      <section className="card import-section">
        <h2>Import LeetCode List</h2>
        <p className="muted">
          Paste a public list URL, e.g.{' '}
          <code>https://leetcode.com/problem-list/2gt61rn1/</code>
        </p>
        <form className="import-form" onSubmit={handleImport}>
          <input
            type="url"
            placeholder="https://leetcode.com/problem-list/..."
            value={url}
            onChange={(e) => setUrl(e.target.value)}
          />
          <button type="submit" className="primary-btn" disabled={importing || !url.trim()}>
            {importing ? 'Importing…' : 'Import List'}
          </button>
        </form>
        {error && <p className="error">{error}</p>}
      </section>

      <div className="lists-layout">
        <aside className="card lists-sidebar">
          <h3>Imported Lists</h3>
          {lists.length === 0 ? (
            <p className="muted small">No lists yet. Import one above.</p>
          ) : (
            <ul className="imported-lists">
              {lists.map((list) => (
                <li key={list.id}>
                  <button
                    type="button"
                    className={`list-item ${activeListId === list.id ? 'active' : ''}`}
                    onClick={() => setActiveListId(list.id)}
                  >
                    <span className="list-name">{list.name}</span>
                    <span className="muted small">{list.totalProblems} problems</span>
                  </button>
                  <button
                    type="button"
                    className="delete-list-btn"
                    onClick={() => handleDelete(list.id)}
                    aria-label="Remove list"
                  >
                    ×
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>

        <section className="card list-detail">
          {!activeListId ? (
            <div className="empty-state small">
              <p className="muted">Select or import a list to view its problems.</p>
            </div>
          ) : loading ? (
            <p className="muted center">Loading list…</p>
          ) : listDetail ? (
            <>
              <div className="list-detail-header">
                <div>
                  <h2>{listDetail.name}</h2>
                  <a href={listDetail.sourceUrl} target="_blank" rel="noreferrer" className="small">
                    View on LeetCode →
                  </a>
                </div>
                {progress && (
                  <div className="list-progress-pill">
                    {progress.solvedCount}/{progress.totalProblems} solved ({progress.progressPercent}%)
                  </div>
                )}
              </div>

              {progress && (
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: `${progress.progressPercent}%` }} />
                </div>
              )}

              <input
                className="filter-input"
                placeholder="Search in this list…"
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
              />

              <ul className="sheet-problems">
                {filtered.map((p, i) => (
                  <li key={p.titleSlug} className={`sheet-row ${p.solved ? 'solved' : ''}`}>
                    <span className="order">{i + 1}</span>
                    <div className="sheet-row-main">
                      <a
                        href={p.leetcodeUrl || `https://leetcode.com/problems/${p.titleSlug}/`}
                        target="_blank"
                        rel="noreferrer"
                      >
                        {p.title}
                      </a>
                    </div>
                    <DifficultyBadge difficulty={p.difficulty} />
                    {progress && (
                      <span className={`status ${p.solved ? 'done' : 'todo'}`}>
                        {p.solved ? '✓' : '○'}
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            </>
          ) : null}
        </section>
      </div>
    </div>
  )
}
