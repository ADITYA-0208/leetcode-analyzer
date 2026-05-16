import StatCard from '../components/StatCard'
import { useUser } from '../context/UserContext'

export default function Dashboard() {
  const { profile, username } = useUser()

  if (!username) {
    return <EmptyState message="Enter a LeetCode username to see your dashboard." />
  }

  if (!profile) {
    return <EmptyState message="Click Analyze to load your profile." />
  }

  const recent = profile.recentSolved?.slice(0, 8) || []

  return (
    <div className="page dashboard">
      <section className="profile-hero card">
        {profile.avatar && (
          <img src={profile.avatar} alt="" className="avatar" />
        )}
        <div className="profile-info">
          <h2>{profile.realName || profile.username}</h2>
          <p className="muted">@{profile.username}</p>
        </div>
      </section>

      <section className="stats-grid">
        <StatCard label="Total Solved" value={profile.totalSolved} accent="accent-total" />
        <StatCard label="Easy" value={profile.easySolved} accent="accent-easy" />
        <StatCard label="Medium" value={profile.mediumSolved} accent="accent-medium" />
        <StatCard label="Hard" value={profile.hardSolved} accent="accent-hard" />
      </section>

      <section className="card">
        <h3>Recently Solved</h3>
        {recent.length === 0 ? (
          <p className="muted">No recent submissions found.</p>
        ) : (
          <ul className="problem-list compact">
            {recent.map((p) => (
              <li key={p.titleSlug}>
                <a href={p.leetcodeUrl} target="_blank" rel="noreferrer">
                  {p.title}
                </a>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}

function EmptyState({ message }) {
  return (
    <div className="empty-state card">
      <span className="empty-icon">λ</span>
      <p>{message}</p>
    </div>
  )
}
