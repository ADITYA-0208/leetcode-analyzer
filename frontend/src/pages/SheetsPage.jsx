import { useEffect, useState } from 'react'
import DifficultyBadge from '../components/DifficultyBadge'
import { api } from '../api/client'
import { useUser } from '../context/UserContext'

export default function SheetsPage() {
  const { username } = useUser()
  const [sheets, setSheets] = useState([])
  const [activeSheet, setActiveSheet] = useState(null)
  const [progress, setProgress] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.getSheets().then(setSheets).catch(() => setSheets([]))
  }, [])

  useEffect(() => {
    if (!username || !activeSheet) return
    setLoading(true)
    api.getSheetDetail(activeSheet, username)
      .then(setProgress)
      .catch(() => setProgress(null))
      .finally(() => setLoading(false))
  }, [username, activeSheet])

  useEffect(() => {
    if (sheets.length && !activeSheet) {
      setActiveSheet(sheets[0].id)
    }
  }, [sheets, activeSheet])

  if (!username) {
    return <p className="muted center">Enter a username to compare Striver sheets.</p>
  }

  return (
    <div className="page sheets-page">
      <div className="sheet-tabs">
        {sheets.map((sheet) => (
          <button
            key={sheet.id}
            type="button"
            className={`sheet-tab ${activeSheet === sheet.id ? 'active' : ''}`}
            onClick={() => setActiveSheet(sheet.id)}
          >
            {sheet.name}
          </button>
        ))}
      </div>

      {loading && <p className="muted center">Loading sheet progress…</p>}

      {progress && (
        <>
          <div className="progress-hero card">
            <div className="progress-ring" style={{ '--p': progress.progressPercent }}>
              <span className="ring-value">{progress.progressPercent}%</span>
            </div>
            <div>
              <h2>{progress.sheetName}</h2>
              <p className="muted">
                {progress.solvedCount} / {progress.totalProblems} problems completed
              </p>
              <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${progress.progressPercent}%` }} />
              </div>
            </div>
          </div>

          <ul className="sheet-problems">
            {progress.problems.map((p) => (
              <li key={p.titleSlug} className={`sheet-row ${p.solved ? 'solved' : ''}`}>
                <span className="order">{p.order}</span>
                <div className="sheet-row-main">
                  <a href={p.leetcodeUrl} target="_blank" rel="noreferrer">
                    {p.title}
                  </a>
                  <span className="topic muted">{p.topic}</span>
                </div>
                <DifficultyBadge difficulty={p.difficulty} />
                <span className={`status ${p.solved ? 'done' : 'todo'}`}>
                  {p.solved ? '✓' : '○'}
                </span>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  )
}
