import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="card state">
      <h2>Page not found</h2>
      <p>The page you are looking for does not exist.</p>
      <Link to="/">Back to the dashboard</Link>
    </div>
  )
}
