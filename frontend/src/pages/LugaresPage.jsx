import { useEffect, useState } from 'react'
import { apiGet } from '../api/client'

export default function LugaresPage() {
  const [provincias, setProvincias] = useState([])
  const [provinciaId, setProvinciaId] = useState('')
  const [lugares, setLugares] = useState([])
  const [estado, setEstado] = useState('cargando')

  useEffect(() => {
    apiGet('/api/provincias').then((lista) => {
      setProvincias(lista)
      if (lista.length > 0) setProvinciaId(String(lista[0].id))
    })
  }, [])

  useEffect(() => {
    if (!provinciaId) return
    setEstado('cargando')
    apiGet(`/api/lugares?provinciaId=${provinciaId}`)
      .then((lista) => {
        setLugares(lista)
        setEstado('listo')
      })
      .catch(() => setEstado('error'))
  }, [provinciaId])

  return (
    <section className="container">
      <h1>Lugares</h1>

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

      {estado === 'cargando' && <p>Cargando lugares...</p>}
      {estado === 'error' && <p className="status status--error">No se pudieron cargar los lugares.</p>}
      {estado === 'listo' && lugares.length === 0 && <p>No hay lugares activos en esta provincia.</p>}

      {estado === 'listo' && lugares.length > 0 && (
        <table className="tabla">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Categoría</th>
              <th>Costo estimado</th>
              <th>Accesibilidad</th>
              <th>Requiere transporte propio</th>
            </tr>
          </thead>
          <tbody>
            {lugares.map((lugar) => (
              <tr key={lugar.id}>
                <td>{lugar.nombre}</td>
                <td>{lugar.categoria}</td>
                <td>₡{lugar.costoEstimado}</td>
                <td>{lugar.nivelAccesibilidad}</td>
                <td>{lugar.requiereTransportePropio ? 'Sí' : 'No'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
