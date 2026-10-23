import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { ApiError } from '../api/client'

export default function LoginPage() {
  const { iniciarSesion } = useAuth()
  const navigate = useNavigate()
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [estado, setEstado] = useState('listo') // listo | enviando | error
  const [mensajeError, setMensajeError] = useState('')

  async function manejarEnvio(evento) {
    evento.preventDefault()
    setEstado('enviando')
    setMensajeError('')
    try {
      await iniciarSesion(correo, password)
      navigate('/itinerarios')
    } catch (error) {
      setEstado('error')
      setMensajeError(error instanceof ApiError ? error.message : 'No se pudo iniciar sesión.')
    }
  }

  return (
    <main className="page page--login">
      <h1>Ingresar</h1>
      <form onSubmit={manejarEnvio} className="formulario">
        <label>
          Correo electrónico
          <input
            type="email"
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            required
            autoComplete="username"
          />
        </label>
        <label>
          Contraseña
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>

        {estado === 'error' && <p className="status status--error">{mensajeError}</p>}

        <button type="submit" disabled={estado === 'enviando'}>
          {estado === 'enviando' ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>
    </main>
  )
}
