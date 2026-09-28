import { createContext, useContext, useEffect, useState } from 'react'
import { apiGet } from '../api/client'

const UsuarioContext = createContext(null)

const STORAGE_KEY = 'smartroute.usuarioActualId'

function leerUsuarioGuardado() {
  try {
    const guardado = localStorage.getItem(STORAGE_KEY)
    return guardado ? Number(guardado) : null
  } catch {
    return null
  }
}

// No hay login real todavía (llega en el Laboratorio 5): esto es un selector de "con cuál usuario
// de prueba estoy navegando", no una sesión autenticada.
export function UsuarioProvider({ children }) {
  const [usuarios, setUsuarios] = useState([])
  const [usuarioActualId, setUsuarioActualId] = useState(leerUsuarioGuardado)
  const [estado, setEstado] = useState('cargando')

  useEffect(() => {
    apiGet('/api/usuarios')
      .then((lista) => {
        setUsuarios(lista)
        setEstado('listo')
        setUsuarioActualId((actual) => actual ?? lista[0]?.id ?? null)
      })
      .catch(() => setEstado('error'))
  }, [])

  function seleccionarUsuario(id) {
    setUsuarioActualId(id)
    try {
      localStorage.setItem(STORAGE_KEY, String(id))
    } catch {
      // localStorage puede no estar disponible; no es crítico para esta app de prueba
    }
  }

  const usuarioActual = usuarios.find((u) => u.id === usuarioActualId) ?? null

  return (
    <UsuarioContext.Provider value={{ usuarios, usuarioActual, estado, seleccionarUsuario }}>
      {children}
    </UsuarioContext.Provider>
  )
}

export function useUsuarioActual() {
  const contexto = useContext(UsuarioContext)
  if (!contexto) {
    throw new Error('useUsuarioActual debe usarse dentro de <UsuarioProvider>')
  }
  return contexto
}
