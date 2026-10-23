import { useEffect, useState } from 'react'
import { ApiError, apiGet, apiPost } from '../api/client'

/**
 * Proceso principal del dominio (confirmar itinerario, Lab 4/5) en la SPA: listado paginado de
 * "mis itinerarios" (con sus tres estados — carga, datos, error — explícitos) y, debajo, el
 * formulario para crear uno nuevo contra /api/v1/itinerarios.
 */
export default function ItinerariosPage() {
  const [provincias, setProvincias] = useState([])
  const [provinciaId, setProvinciaId] = useState('')
  const [lugares, setLugares] = useState([])

  const [lugarSeleccionadoId, setLugarSeleccionadoId] = useState('')
  const [horaInicio, setHoraInicio] = useState('09:00')
  const [horaFin, setHoraFin] = useState('11:00')

  const [paradas, setParadas] = useState([])
  const [fecha, setFecha] = useState('')
  const [confirmarExceso, setConfirmarExceso] = useState(false)

  const [enviando, setEnviando] = useState(false)
  const [errorFormulario, setErrorFormulario] = useState(null)

  const [pagina, setPagina] = useState(null)
  const [numeroPagina, setNumeroPagina] = useState(0)
  const [estadoLista, setEstadoLista] = useState('cargando') // cargando | datos | error
  const [errorLista, setErrorLista] = useState('')

  function cargarItinerarios() {
    setEstadoLista('cargando')
    apiGet('/api/v1/itinerarios', { page: numeroPagina, size: 10 })
      .then((respuesta) => {
        setPagina(respuesta)
        setEstadoLista('datos')
      })
      .catch((excepcion) => {
        setErrorLista(excepcion instanceof ApiError ? excepcion.message : 'No se pudieron cargar los itinerarios.')
        setEstadoLista('error')
      })
  }

  useEffect(cargarItinerarios, [numeroPagina])

  useEffect(() => {
    apiGet('/api/provincias').then((lista) => {
      setProvincias(lista)
      if (lista.length > 0) setProvinciaId(String(lista[0].id))
    })
  }, [])

  useEffect(() => {
    if (!provinciaId) return
    apiGet('/api/v1/lugares', { provinciaId, size: 50 }).then((respuesta) => {
      setLugares(respuesta.contenido)
      setLugarSeleccionadoId(respuesta.contenido.length > 0 ? String(respuesta.contenido[0].id) : '')
    })
  }, [provinciaId])

  function agregarParada() {
    const lugar = lugares.find((l) => l.id === Number(lugarSeleccionadoId))
    if (!lugar) return
    setParadas((actuales) => [...actuales, { lugarId: lugar.id, nombre: lugar.nombre, horaInicio, horaFin }])
  }

  function quitarParada(indice) {
    setParadas((actuales) => actuales.filter((_, i) => i !== indice))
  }

  async function confirmar() {
    if (paradas.length === 0 || !fecha) return
    setEnviando(true)
    setErrorFormulario(null)
    try {
      await apiPost('/api/v1/itinerarios', {
        fecha,
        confirmarExcesoPresupuesto: confirmarExceso,
        paradas: paradas.map(({ lugarId, horaInicio: inicio, horaFin: fin }, indice) => ({
          lugarId,
          orden: indice + 1,
          horaInicio: inicio,
          horaFin: fin,
        })),
      })
      setParadas([])
      setFecha('')
      setConfirmarExceso(false)
      setNumeroPagina(0)
      cargarItinerarios()
    } catch (excepcion) {
      setErrorFormulario(excepcion instanceof ApiError ? excepcion.message : 'Ocurrió un error inesperado.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="container">
      <h1>Mis itinerarios</h1>

      {estadoLista === 'cargando' && <p>Cargando itinerarios...</p>}
      {estadoLista === 'error' && <p className="status status--error">{errorLista}</p>}
      {estadoLista === 'datos' && pagina.contenido.length === 0 && <p>Todavía no confirmaste ningún itinerario.</p>}

      {estadoLista === 'datos' && pagina.contenido.length > 0 && (
        <>
          <table className="tabla">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Presupuesto total</th>
                <th>Paradas</th>
              </tr>
            </thead>
            <tbody>
              {pagina.contenido.map((itinerario) => (
                <tr key={itinerario.id}>
                  <td>{itinerario.fecha}</td>
                  <td>{itinerario.estado}</td>
                  <td>₡{itinerario.presupuestoTotalEstimado}</td>
                  <td>{itinerario.paradas.map((p) => p.nombreLugar).join(', ')}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="paginacion">
            <button type="button" className="btn" disabled={pagina.pagina === 0} onClick={() => setNumeroPagina((p) => p - 1)}>
              « Anterior
            </button>
            <span>
              Página {pagina.pagina + 1} de {pagina.totalPaginas}
            </span>
            <button
              type="button"
              className="btn"
              disabled={pagina.pagina + 1 >= pagina.totalPaginas}
              onClick={() => setNumeroPagina((p) => p + 1)}
            >
              Siguiente »
            </button>
          </div>
        </>
      )}

      <h2>Confirmar nuevo itinerario</h2>
      <p>El itinerario y todas sus paradas se guardan en una sola operación: todo o nada.</p>

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
          <select value={lugarSeleccionadoId} onChange={(evento) => setLugarSeleccionadoId(evento.target.value)}>
            {lugares.map((lugar) => (
              <option key={lugar.id} value={lugar.id}>
                {lugar.nombre} (₡{lugar.costoEstimado})
              </option>
            ))}
          </select>
        </label>
        <label>
          Hora inicio: <input type="time" value={horaInicio} onChange={(e) => setHoraInicio(e.target.value)} />
        </label>
        <label>
          Hora fin: <input type="time" value={horaFin} onChange={(e) => setHoraFin(e.target.value)} />
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
          <input type="checkbox" checked={confirmarExceso} onChange={(e) => setConfirmarExceso(e.target.checked)} />{' '}
          Confirmar aunque supere el presupuesto máximo del perfil
        </label>
      </div>

      <button className="btn" type="button" onClick={confirmar} disabled={enviando || paradas.length === 0 || !fecha}>
        {enviando ? 'Confirmando...' : 'Confirmar itinerario'}
      </button>

      {errorFormulario && <p className="status status--error">{errorFormulario}</p>}
    </section>
  )
}
