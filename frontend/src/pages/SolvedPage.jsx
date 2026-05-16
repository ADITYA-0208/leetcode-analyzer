import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { useUser } from '../context/UserContext'

export default function SolvedPage() {
  const { username } = useUser()
  const [problems, setProblems] = useState([])
  const [filter, setFilter] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!username) return
    setLoading(true)
    api.getSolved(username)
      .then(setProblems)
      .catch(() => setProblems([]))
      .finally(() => setLoading(false))
  }, [username])

  const filtered = problems.filter((p) =>
    p.title.toLowerCase().includes(filter.toLowerCase())
  )

  if (!username) {
    return <p className="muted center">Enter a username first.</p>
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Solved Problems</h2>
        <span className="count-pill">{problems.length} unique</span>
      </div>

      <input
        className="filter-input"
        placeholder="Search problems…"
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
      />

      {loading ? (
        <p className="muted center">Loading solved problems…</p>
      ) : (
        <ul className="problem-list">
          {filtered.map((p) => (
            <li key={p.titleSlug} className="problem-row">
              <a href={p.leetcodeUrl} target="_blank" rel="noreferrer" className="problem-title">
                {p.title}
              </a>
              <span className="muted small">
                {new Date(p.timestamp * 1000).toLocaleDateString()}
              </span>
              <a href={p.leetcodeUrl} target="_blank" rel="noreferrer" className="link-btn">
                Open →
              </a>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
