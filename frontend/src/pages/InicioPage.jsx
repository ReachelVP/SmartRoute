import { useEffect, useState } from 'react'
import { apiGet } from '../api/client'

export default function InicioPage() {
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
        {health.state === 'error' && 'No se pudo conectar con el backend. ¿Está corriendo en el puerto 8080?'}
      </div>

      <p className="nota-demo">
        Todavía no hay login real (llega en el Laboratorio 5) — arriba a la derecha podés elegir
        con cuál <strong>usuario de prueba</strong> navegar para probar las recomendaciones y la
        confirmación de itinerarios.
      </p>
    </main>
  )
}
