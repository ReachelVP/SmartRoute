import { useEffect, useState } from 'react'
import './App.css'

const API_BASE_URL = 'http://localhost:8080'

function App() {
  const [health, setHealth] = useState({ state: 'loading' })

  useEffect(() => {
    fetch(`${API_BASE_URL}/api/salud`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then((data) => setHealth({ state: 'ok', data }))
      .catch(() => setHealth({ state: 'error' }))
  }, [])

  return (
    <main className="page">
      <h1>SmartRoute</h1>
      <p>
        Planificá tus fines de semana: descubrí y organizá actividades
        recreativas y turismo interno en Costa Rica según tus preferencias.
      </p>

      <div className={`status status--${health.state}`}>
        {health.state === 'loading' && 'Consultando backend...'}
        {health.state === 'ok' &&
          `Backend conectado (${health.data.service}: ${health.data.status})`}
        {health.state === 'error' &&
          'No se pudo conectar con el backend. ¿Está corriendo en el puerto 8080?'}
      </div>
    </main>
  )
}

export default App
