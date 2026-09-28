import { useEffect, useState } from 'react'
import { ApiError, apiGet, apiPost } from '../api/client'
import { useUsuarioActual } from '../context/UsuarioContext'

export default function ConfirmarItinerarioPage() {
  const { usuarioActual } = useUsuarioActual()

  const [provincias, setProvincias] = useState([])
  const [provinciaId, setProvinciaId] = useState('')
  const [lugares, setLugares] = useState([])

  const [lugarSeleccionadoId, setLugarSeleccionadoId] = useState('')
  const [horaInicio, setHoraInicio] = useState('09:00')
  const [horaFin, setHoraFin] = useState('11:00')

  const [paradas, setParadas] = useState([])
  const [fecha, setFecha] = useState('')
  const [confirmarExceso, setConfirmarExceso] = useState(false)

  const [resultado, setResultado] = useState(null)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    apiGet('/api/provincias').then((lista) => {
      setProvincias(lista)
      if (lista.length > 0) setProvinciaId(String(lista[0].id))
    })
  }, [])

  useEffect(() => {
    if (!provinciaId) return
    apiGet(`/api/lugares?provinciaId=${provinciaId}`).then((lista) => {
      setLugares(lista)
      setLugarSeleccionadoId(lista.length > 0 ? String(lista[0].id) : '')
    })
  }, [provinciaId])

  function agregarParada() {
    const lugar = lugares.find((l) => l.id === Number(lugarSeleccionadoId))
    if (!lugar) return
    setParadas((actuales) => [
      ...actuales,
      { lugarId: lugar.id, nombre: lugar.nombre, horaInicio, horaFin },
    ])
  }

  function quitarParada(indice) {
    setParadas((actuales) => actuales.filter((_, i) => i !== indice))
  }

  async function confirmar() {
    if (!usuarioActual || paradas.length === 0 || !fecha) return
    setEnviando(true)
    setError(null)
    setResultado(null)
    try {
      const respuesta = await apiPost('/api/itinerarios', {
        usuarioId: usuarioActual.id,
        fecha,
        confirmarExcesoPresupuesto: confirmarExceso,
        paradas: paradas.map(({ lugarId, horaInicio: inicio, horaFin: fin }) => ({
          lugarId,
          horaInicio: inicio,
          horaFin: fin,
        })),
      })
      setResultado(respuesta)
      setParadas([])
    } catch (excepcion) {
      setError(excepcion instanceof ApiError ? excepcion.message : 'Ocurrió un error inesperado.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="container">
      <h1>Confirmar itinerario</h1>
      <p>
        Proceso 2 del dominio: armá las paradas que querés visitar y confirmá — el itinerario y
        todas sus paradas se guardan en una sola operación, todo o nada.
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
      </div>

      <div className="form-row">
        <label>
          Lugar:{' '}
          <select
            value={lugarSeleccionadoId}
            onChange={(evento) => setLugarSeleccionadoId(evento.target.value)}
          >
            {lugares.map((lugar) => (
              <option key={lugar.id} value={lugar.id}>
                {lugar.nombre} (₡{lugar.costoEstimado})
              </option>
            ))}
          </select>
        </label>
        <label>
          Hora inicio:{' '}
          <input type="time" value={horaInicio} onChange={(evento) => setHoraInicio(evento.target.value)} />
        </label>
        <label>
          Hora fin:{' '}
          <input type="time" value={horaFin} onChange={(evento) => setHoraFin(evento.target.value)} />
        </label>
        <button className="btn" type="button" onClick={agregarParada} disabled={lugares.length === 0}>
          Agregar parada
        </button>
      </div>

      {paradas.length > 0 && (
        <ol className="lista-paradas">
          {paradas.map((parada, indice) => (
            <li key={`${parada.lugarId}-${indice}`}>
              {parada.nombre} — {parada.horaInicio} a {parada.horaFin}{' '}
              <button className="btn btn--link" type="button" onClick={() => quitarParada(indice)}>
                Quitar
              </button>
            </li>
          ))}
        </ol>
      )}

      <div className="form-row">
        <label>
          Fecha: <input type="date" value={fecha} onChange={(evento) => setFecha(evento.target.value)} />
        </label>
        <label>
          <input
            type="checkbox"
            checked={confirmarExceso}
            onChange={(evento) => setConfirmarExceso(evento.target.checked)}
          />{' '}
          Confirmar aunque supere el presupuesto máximo del perfil
        </label>
      </div>

      <button
        className="btn"
        type="button"
        onClick={confirmar}
        disabled={enviando || paradas.length === 0 || !fecha || !usuarioActual}
      >
        {enviando ? 'Confirmando...' : 'Confirmar itinerario'}
      </button>

      {error && <p className="status status--error">{error}</p>}

      {resultado && (
        <div className="status status--ok">
          <p>
            Itinerario #{resultado.id} confirmado — total estimado: ₡{resultado.presupuestoTotalEstimado}
          </p>
          <ol>
            {resultado.paradas.map((parada) => (
              <li key={parada.orden}>
                {parada.nombreLugar} ({parada.horaInicio}–{parada.horaFin}) — ₡{parada.costoEstimado}
              </li>
            ))}
          </ol>
        </div>
      )}
    </section>
  )
}
