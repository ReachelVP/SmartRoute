import { useEffect, useState } from 'react'
import { ApiError, apiGet } from '../api/client'

// "Listado paginado con estados de carga, datos y error" (rúbrica Lab 6): esta página es la
// referencia de ese patrón — las demás (ItinerariosPage) lo repiten.
export default function LugaresPage() {
  const [provincias, setProvincias] = useState([])
  const [provinciaId, setProvinciaId] = useState('')
  const [pagina, setPagina] = useState(null)
  const [numeroPagina, setNumeroPagina] = useState(0)
  const [estado, setEstado] = useState('cargando') // cargando | datos | error
  const [mensajeError, setMensajeError] = useState('')

  useEffect(() => {
    apiGet('/api/provincias').then((lista) => setProvincias(lista))
  }, [])

  useEffect(() => {
    setEstado('cargando')
    apiGet('/api/v1/lugares', { provinciaId: provinciaId || undefined, page: numeroPagina, size: 10 })
      .then((respuesta) => {
        setPagina(respuesta)
        setEstado('datos')
      })
      .catch((excepcion) => {
        setMensajeError(excepcion instanceof ApiError ? excepcion.message : 'No se pudieron cargar los lugares.')
        setEstado('error')
      })
  }, [provinciaId, numeroPagina])

  function cambiarProvincia(evento) {
    setProvinciaId(evento.target.value)
    setNumeroPagina(0)
  }

  return (
    <section className="container">
      <h1>Lugares</h1>

      <div className="form-row">
        <label>
          Provincia:{' '}
          <select value={provinciaId} onChange={cambiarProvincia}>
            <option value="">Todas</option>
            {provincias.map((provincia) => (
              <option key={provincia.id} value={provincia.id}>
                {provincia.nombre}
              </option>
            ))}
          </select>
        </label>
      </div>

      {estado === 'cargando' && <p>Cargando lugares...</p>}
      {estado === 'error' && <p className="status status--error">{mensajeError}</p>}

      {estado === 'datos' && pagina.contenido.length === 0 && <p>No hay lugares para este filtro.</p>}

      {estado === 'datos' && pagina.contenido.length > 0 && (
        <>
          <table className="tabla">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Categoría</th>
                <th>Provincia</th>
                <th>Costo estimado</th>
                <th>Accesibilidad</th>
                <th>Requiere transporte propio</th>
              </tr>
            </thead>
            <tbody>
              {pagina.contenido.map((lugar) => (
                <tr key={lugar.id}>
                  <td>{lugar.nombre}</td>
                  <td>{lugar.categoria}</td>
                  <td>{lugar.provincia}</td>
                  <td>₡{lugar.costoEstimado}</td>
                  <td>{lugar.nivelAccesibilidad}</td>
                  <td>{lugar.requiereTransportePropio ? 'Sí' : 'No'}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="paginacion">
            <button
              type="button"
              className="btn"
              disabled={pagina.pagina === 0}
              onClick={() => setNumeroPagina((p) => p - 1)}
            >
              « Anterior
            </button>
            <span>
              Página {pagina.pagina + 1} de {pagina.totalPaginas} ({pagina.totalElementos} lugares)
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
    </section>
  )
}
