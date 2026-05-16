import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { useUser } from '../context/UserContext'

export default function BadgesPage() {
  const { username } = useUser()
  const [badges, setBadges] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!username) return
    setLoading(true)
    api.getBadges(username)
      .then(setBadges)
      .catch(() => setBadges([]))
      .finally(() => setLoading(false))
  }, [username])

  if (!username) {
    return <p className="muted center">Enter a username to view badges.</p>
  }

  const earned = badges.filter((b) => b.earned).length

  return (
    <div className="page badges-page">
      <div className="page-header">
        <h2>Achievements</h2>
        <span className="count-pill">{earned} / {badges.length} earned</span>
      </div>

      {loading ? (
        <p className="muted center">Loading badges…</p>
      ) : (
        <div className="badges-grid">
          {badges.map((badge) => (
            <div
              key={badge.id}
              className={`badge-card tier-${badge.tier} ${badge.earned ? 'earned' : 'locked'}`}
            >
              <span className="badge-icon">{badge.icon}</span>
              <h3>{badge.name}</h3>
              <p className="muted">{badge.description}</p>
              <span className="tier-label">{badge.tier}</span>
              {!badge.earned && <span className="lock-overlay">🔒</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
