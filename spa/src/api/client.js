// Módulo cliente centralizado (Laboratorio 6): toda llamada a la API pasa por acá, que adjunta el
// JWT y traduce un 401 en "cerrar sesión y volver a /login" en un solo lugar, en vez de que cada
// página tenga que acordarse de hacerlo.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const TOKEN_STORAGE_KEY = 'smartroute.sesion'

export class ApiError extends Error {
  constructor(status, codigo, mensaje) {
    super(mensaje || `Error HTTP ${status}`)
    this.status = status
    this.codigo = codigo
  }
}

export function leerSesionGuardada() {
  try {
    const guardada = localStorage.getItem(TOKEN_STORAGE_KEY)
    return guardada ? JSON.parse(guardada) : null
  } catch {
    return null
  }
}

export function guardarSesion(sesion) {
  try {
    localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(sesion))
  } catch {
    // localStorage puede no estar disponible (modo incógnito con storage bloqueado); la sesión
    // simplemente no sobrevive un refresh, no es un error fatal para la app.
  }
}

export function borrarSesion() {
  try {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
  } catch {
    // ver guardarSesion
  }
}

// Un solo punto donde un 401 (token ausente/inválido/expirado) decide qué hacer: limpiar la
// sesión y mandar a /login. Las páginas no necesitan manejar el 401 cada una por su cuenta.
let manejador401 = () => {
  borrarSesion()
  window.location.href = '/login'
}

export function registrarManejador401(fn) {
  manejador401 = fn
}

function construirQuery(params) {
  if (!params) return ''
  const entradas = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
  if (entradas.length === 0) return ''
  return '?' + new URLSearchParams(entradas).toString()
}

async function request(path, options = {}) {
  const sesion = leerSesionGuardada()
  const headers = { 'Content-Type': 'application/json', ...options.headers }
  if (sesion?.token) {
    headers.Authorization = `Bearer ${sesion.token}`
  }

  const res = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })

  if (res.status === 401) {
    manejador401()
    throw new ApiError(401, 'No autenticado', 'La sesión expiró o no es válida.')
  }

  if (!res.ok) {
    let cuerpo = null
    try {
      cuerpo = await res.json()
    } catch {
      // el cuerpo no era JSON (p. ej. un 400 de Bean Validation con otro formato)
    }
    // Desde el Laboratorio 5 los errores vienen como Problem Details (RFC 9457):
    // {type, title, status, detail, ...}. "detail" es el mensaje de la regla de negocio.
    throw new ApiError(res.status, cuerpo?.title, cuerpo?.detail)
  }

  if (res.status === 204) return null
  return res.json()
}

export const apiGet = (path, params) => request(`${path}${construirQuery(params)}`)
export const apiPost = (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) })
export const apiPut = (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) })
export const apiDelete = (path) => request(path, { method: 'DELETE' })
