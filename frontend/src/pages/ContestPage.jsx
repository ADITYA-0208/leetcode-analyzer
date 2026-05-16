import { useEffect, useState } from 'react'
import DifficultyBadge from '../components/DifficultyBadge'
import { api } from '../api/client'
import { useUser } from '../context/UserContext'

const POOLS = [
  { value: 'SOLVED', label: 'My Solved (Revision)' },
  { value: 'CATALOG', label: 'Full Catalog' },
  { value: 'SHEET', label: 'Striver Sheet' },
  { value: 'CUSTOM_LIST', label: 'Imported LC List' },
]

export default function ContestPage() {
  const { username } = useUser()
  const [sheets, setSheets] = useState([])
  const [importedLists, setImportedLists] = useState([])
  const [pool, setPool] = useState('SOLVED')
  const [sheetId, setSheetId] = useState('striver-sde')
  const [listId, setListId] = useState('')
  const [easy, setEasy] = useState(1)
  const [medium, setMedium] = useState(2)
  const [hard, setHard] = useState(1)
  const [total, setTotal] = useState(4)
  const [duration, setDuration] = useState(90)
  const [contest, setContest] = useState(null)
  const [timeLeft, setTimeLeft] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.getSheets().then(setSheets).catch(() => {})
    api.getImportedLists().then((lists) => {
      setImportedLists(lists)
      if (lists.length) setListId(lists[0].id)
    }).catch(() => {})
  }, [])

  useEffect(() => {
    if (timeLeft === null || timeLeft <= 0) return
    const id = setInterval(() => setTimeLeft((t) => t - 1), 1000)
    return () => clearInterval(id)
  }, [timeLeft])

  async function generate() {
    if (!username) return
    setLoading(true)
    setError(null)
    try {
      const result = await api.generateContest({
        username,
        pool,
        sheetId: pool === 'SHEET' ? sheetId : null,
        listId: pool === 'CUSTOM_LIST' ? listId : null,
        easyCount: easy,
        mediumCount: medium,
        hardCount: hard,
        totalQuestions: total,
        durationMinutes: duration,
      })
      setContest(result)
      setTimeLeft(result.durationMinutes * 60)
    } catch (e) {
      setError(e.message)
      setContest(null)
    } finally {
      setLoading(false)
    }
  }

  const formatTime = (secs) => {
    const m = Math.floor(secs / 60)
    const s = secs % 60
    return `${m}:${String(s).padStart(2, '0')}`
  }

  if (!username) {
    return <p className="muted center">Enter a username to generate contests.</p>
  }

  return (
    <div className="page contest-page">
      <div className="contest-layout">
        <section className="card contest-form">
          <h2>Custom Contest Generator</h2>
          <label>
            Question pool
            <select value={pool} onChange={(e) => setPool(e.target.value)}>
              {POOLS.map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </label>

          {pool === 'SHEET' && (
            <label>
              Sheet
              <select value={sheetId} onChange={(e) => setSheetId(e.target.value)}>
                {sheets.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </label>
          )}

          {pool === 'CUSTOM_LIST' && (
            <label>
              Imported list
              <select value={listId} onChange={(e) => setListId(e.target.value)}>
                {importedLists.length === 0 ? (
                  <option value="">Import a list first (LC Lists tab)</option>
                ) : (
                  importedLists.map((l) => (
                    <option key={l.id} value={l.id}>{l.name} ({l.totalProblems})</option>
                  ))
                )}
              </select>
            </label>
          )}

          <div className="diff-grid">
            <label>Easy<input type="number" min={0} max={20} value={easy} onChange={(e) => setEasy(+e.target.value)} /></label>
            <label>Medium<input type="number" min={0} max={20} value={medium} onChange={(e) => setMedium(+e.target.value)} /></label>
            <label>Hard<input type="number" min={0} max={20} value={hard} onChange={(e) => setHard(+e.target.value)} /></label>
          </div>

          <label>
            Total questions
            <input type="range" min={1} max={15} value={total} onChange={(e) => setTotal(+e.target.value)} />
            <span>{total}</span>
          </label>

          <label>
            Duration (minutes)
            <input type="number" min={5} max={300} value={duration} onChange={(e) => setDuration(+e.target.value)} />
          </label>

          <button type="button" className="primary-btn" onClick={generate} disabled={loading}>
            {loading ? 'Generating…' : 'Generate Contest'}
          </button>
          {error && <p className="error">{error}</p>}
        </section>

        <section className="card contest-result">
          {contest ? (
            <>
              <div className="timer-display">
                <span className="timer-label">Time remaining</span>
                <span className={`timer-value ${timeLeft <= 300 ? 'urgent' : ''}`}>
                  {timeLeft > 0 ? formatTime(timeLeft) : "Time's up!"}
                </span>
              </div>
              <ul className="contest-list">
                {contest.problems.map((p) => (
                  <li key={p.titleSlug} className="contest-item">
                    <span className="q-num">Q{p.number}</span>
                    <a href={p.leetcodeUrl} target="_blank" rel="noreferrer">{p.title}</a>
                    <DifficultyBadge difficulty={p.difficulty} />
                  </li>
                ))}
              </ul>
            </>
          ) : (
            <div className="empty-state small">
              <p className="muted">Configure and generate a mock contest. Problems open on LeetCode.</p>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
