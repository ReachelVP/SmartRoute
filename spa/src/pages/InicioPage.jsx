import { useEffect, useState } from 'react'
import { apiGet } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function InicioPage() {
  const { estaAutenticado, sesion } = useAuth()
  const [health, setHealth] = useState({ state: 'loading' })

  useEffect(() => {
    apiGet('/api/salud')
      .then((data) => setHealth({ state: 'ok', data }))
      .catch(() => setHealth({ state: 'error' }))
  }, [])

  return (
    <main className="page">
      <h1>SmartRoute</h1>
      <p>
        Planificá tus fines de semana: descubrí y organizá actividades recreativas y turismo
        interno en Costa Rica según tus preferencias.
      </p>

      <div className={`status status--${health.state}`}>
        {health.state === 'loading' && 'Consultando backend...'}
        {health.state === 'ok' && `Backend conectado (${health.data.service}: ${health.data.status})`}
        {health.state === 'error' && 'No se pudo conectar con el backend.'}
      </div>

      {estaAutenticado ? (
        <p className="nota-demo">
          Sesión activa como <strong>{sesion.nombre}</strong> ({sesion.rol}). Mirá{' '}
          <strong>Mis itinerarios</strong> para confirmar uno nuevo, o pedí una{' '}
          <strong>Recomendación</strong>.
        </p>
      ) : (
        <p className="nota-demo">
          Iniciá sesión para pedir recomendaciones y confirmar itinerarios — el catálogo de{' '}
          <strong>Lugares</strong> es público.
        </p>
      )}
    </main>
  )
}
