import { useEffect, useState } from 'react'
import { api } from './api/client'
import Header from './components/Header'
import { ThemeProvider } from './context/ThemeContext'
import { UserProvider, useUser } from './context/UserContext'
import BadgesPage from './pages/BadgesPage'
import ContestPage from './pages/ContestPage'
import Dashboard from './pages/Dashboard'
import ListsPage from './pages/ListsPage'
import SheetsPage from './pages/SheetsPage'
import SolvedPage from './pages/SolvedPage'
import './App.css'

function AppContent() {
  const [tab, setTab] = useState('dashboard')
  const { username, setProfile, setLoading, setError } = useUser()

  useEffect(() => {
    if (username) analyze(username)
  }, [])

  async function analyze(username) {
    if (!username?.trim()) return
    setLoading(true)
    setError(null)
    try {
      const profile = await api.getProfile(username.trim())
      setProfile(profile)
    } catch (e) {
      setError(e.message)
      setProfile(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app">
      <Header activeTab={tab} onTabChange={setTab} onAnalyze={analyze} />
      <main className="main">
        <ErrorBanner />
        {tab === 'dashboard' && <Dashboard />}
        {tab === 'solved' && <SolvedPage />}
        {tab === 'sheets' && <SheetsPage />}
        {tab === 'lists' && <ListsPage />}
        {tab === 'contest' && <ContestPage />}
        {tab === 'badges' && <BadgesPage />}
      </main>
    </div>
  )
}

function ErrorBanner() {
  const { error, setError } = useUser()
  if (!error) return null
  return (
    <div className="error-banner">
      <span>{error}</span>
      <button type="button" onClick={() => setError(null)}>×</button>
    </div>
  )
}

export default function App() {
  return (
    <ThemeProvider>
      <UserProvider>
        <AppContent />
      </UserProvider>
    </ThemeProvider>
  )
}
