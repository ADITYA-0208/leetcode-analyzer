import { createContext, useContext, useState } from 'react'

const UserContext = createContext()

export function UserProvider({ children }) {
  const [username, setUsername] = useState(() => localStorage.getItem('lc-username') || '')
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const saveUsername = (name) => {
    const trimmed = name.trim()
    setUsername(trimmed)
    localStorage.setItem('lc-username', trimmed)
  }

  return (
    <UserContext.Provider
      value={{ username, saveUsername, profile, setProfile, loading, setLoading, error, setError }}
    >
      {children}
    </UserContext.Provider>
  )
}

export const useUser = () => useContext(UserContext)
