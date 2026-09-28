import { useEffect, useState } from 'react'
import { ApiError, apiGet, apiPost } from '../api/client'
import { useUsuarioActual } from '../context/UsuarioContext'

export default function RecomendacionesPage() {
  const { usuarioActual } = useUsuarioActual()
  const [provincias, setProvincias] = useState([])
  const [provinciaId, setProvinciaId] = useState('')
  const [resultado, setResultado] = useState(null)
  const [error, setError] = useState(null)
  const [cargando, setCargando] = useState(false)

  useEffect(() => {
    apiGet('/api/provincias').then((lista) => {
      setProvincias(lista)
      if (lista.length > 0) setProvinciaId(String(lista[0].id))
    })
  }, [])

  async function pedirRecomendacion() {
    if (!usuarioActual || !provinciaId) return
    setCargando(true)
    setError(null)
    setResultado(null)
    try {
      const respuesta = await apiPost('/api/recomendaciones', {
        usuarioId: usuarioActual.id,
        provinciaId: Number(provinciaId),
      })
      setResultado(respuesta)
    } catch (excepcion) {
      setError(excepcion instanceof ApiError ? excepcion.message : 'Ocurrió un error inesperado.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <section className="container">
      <h1>Recomendaciones</h1>
      <p>
        Proceso 1 del dominio: usa el perfil de preferencias de{' '}
        <strong>{usuarioActual?.nombre ?? '...'}</strong> (categorías de interés, presupuesto,
        transporte propio) más la provincia que elijas acá.
      </p>

      <div className="form-row">
        <label>
          Provincia:{' '}
          <select value={provinciaId} onChange={(evento) => setProvinciaId(evento.target.value)}>
            {provincias.map((provincia) => (
              <option key={provincia.id} value={provincia.id}>
                {provincia.nombre}
              </option>
            ))}
          </select>
        </label>
        <button className="btn" type="button" onClick={pedirRecomendacion} disabled={cargando || !usuarioActual}>
          {cargando ? 'Consultando...' : 'Recomendar'}
        </button>
      </div>

      {error && <p className="status status--error">{error}</p>}

      {resultado?.criterioLimitante && (
        <p className="status status--error">
          Ningún lugar cumplió todos los criterios. El que más limitó fue:{' '}
          <strong>{resultado.criterioLimitante}</strong>. Probá relajarlo en el perfil de
          preferencias.
        </p>
      )}

      {resultado && !resultado.criterioLimitante && resultado.lugares.length > 0 && (
        <ul className="lista-lugares">
          {resultado.lugares.map((lugar) => (
            <li key={lugar.id}>
              <strong>{lugar.nombre}</strong> — {lugar.categoria}, {lugar.provincia}, ₡
              {lugar.costoEstimado}
              {lugar.requiereTransportePropio ? ' (requiere transporte propio)' : ''}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
