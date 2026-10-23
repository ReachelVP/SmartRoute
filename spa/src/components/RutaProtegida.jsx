import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function RutaProtegida({ children }) {
  const { estaAutenticado } = useAuth()
  if (!estaAutenticado) {
    return <Navigate to="/login" replace />
  }
  return children
}
