export default function DifficultyBadge({ difficulty }) {
  const key = difficulty?.toLowerCase() || 'medium'
  return <span className={`badge diff-${key}`}>{difficulty}</span>
}
