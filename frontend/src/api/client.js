const API_BASE_URL = 'http://localhost:8080'

export class ApiError extends Error {
  constructor(status, codigo, mensaje) {
    super(mensaje || `Error HTTP ${status}`)
    this.status = status
    this.codigo = codigo
  }
}

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

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

export const apiGet = (path) => request(path)
export const apiPost = (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) })
