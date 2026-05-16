import { useState } from 'react'
import { useTheme } from '../context/ThemeContext'
import { useUser } from '../context/UserContext'

const TABS = [
  { id: 'dashboard', label: 'Dashboard', icon: '◉' },
  { id: 'solved', label: 'Solved', icon: '✓' },
  { id: 'sheets', label: 'Striver Sheets', icon: '▦' },
  { id: 'lists', label: 'LC Lists', icon: '⊞' },
  { id: 'contest', label: 'Contest', icon: '⚡' },
  { id: 'badges', label: 'Badges', icon: '★' },
]

export default function Header({ activeTab, onTabChange, onAnalyze }) {
  const { theme, toggle } = useTheme()
  const { username, saveUsername, loading } = useUser()
  const [input, setInput] = useState(username)

  function handleSubmit(e) {
    e.preventDefault()
    saveUsername(input)
    onAnalyze(input)
  }

  return (
    <header className="header">
      <div className="brand">
        <span className="brand-icon">λ</span>
        <div>
          <h1>LeetCode Analyzer</h1>
          <p>Track progress · Striver sheets · Mock contests</p>
        </div>
      </div>

      <form className="search-form" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="LeetCode username"
          value={input}
          onChange={(e) => setInput(e.target.value)}
        />
        <button type="submit" disabled={loading || !input.trim()}>
          {loading ? 'Loading…' : 'Analyze'}
        </button>
      </form>

      <div className="header-actions">
        <button type="button" className="theme-btn" onClick={toggle} aria-label="Toggle theme">
          {theme === 'dark' ? '☀' : '☾'}
        </button>
      </div>

      <nav className="tabs">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={`tab ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => onTabChange(tab.id)}
          >
            <span className="tab-icon">{tab.icon}</span>
            {tab.label}
          </button>
        ))}
      </nav>
    </header>
  )
}
