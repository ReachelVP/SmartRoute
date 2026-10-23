import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { apiPost, borrarSesion, guardarSesion, leerSesionGuardada, registrarManejador401 } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(leerSesionGuardada)

  const cerrarSesion = useCallback(() => {
    borrarSesion()
    setSesion(null)
  }, [])

  // El cliente HTTP llama esto cuando el backend responde 401 (token ausente/inválido/expirado),
  // sin importar desde qué página salió la petición.
  useEffect(() => {
    registrarManejador401(() => {
      borrarSesion()
      setSesion(null)
    })
  }, [])

  async function iniciarSesion(correo, password) {
    const respuesta = await apiPost('/auth/login', { correo, password })
    const nuevaSesion = {
      token: respuesta.token,
      usuarioId: respuesta.usuarioId,
      nombre: respuesta.nombre,
      rol: respuesta.rol,
    }
    guardarSesion(nuevaSesion)
    setSesion(nuevaSesion)
    return nuevaSesion
  }

  return (
    <AuthContext.Provider
      value={{
        sesion,
        estaAutenticado: sesion != null,
        esAdmin: sesion?.rol === 'ADMIN',
        iniciarSesion,
        cerrarSesion,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return contexto
}
