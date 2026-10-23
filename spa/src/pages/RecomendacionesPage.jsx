import { useEffect, useState } from 'react'
import { ApiError, apiGet, apiPost } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function RecomendacionesPage() {
  const { sesion } = useAuth()
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
    if (!provinciaId) return
    setCargando(true)
    setError(null)
    setResultado(null)
    try {
      // v1: el usuario sale del JWT, no se manda en el cuerpo (ver docs/api-rest-seguridad.md).
      const respuesta = await apiPost('/api/v1/recomendaciones', { provinciaId: Number(provinciaId) })
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
        Proceso 1 del dominio: usa el perfil de preferencias de <strong>{sesion?.nombre}</strong>{' '}
        (categorías de interés, presupuesto, transporte propio) más la provincia que elijas acá.
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
        <button className="btn" type="button" onClick={pedirRecomendacion} disabled={cargando}>
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
