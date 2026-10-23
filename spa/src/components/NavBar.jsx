import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function NavBar() {
  const { sesion, estaAutenticado, cerrarSesion } = useAuth()
  const navigate = useNavigate()

  function manejarSalir() {
    cerrarSesion()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <NavLink to="/" className="navbar__brand">
        SmartRoute
      </NavLink>

      <nav className="navbar__links">
        <NavLink to="/lugares">Lugares</NavLink>
        {estaAutenticado && <NavLink to="/recomendaciones">Recomendaciones</NavLink>}
        {estaAutenticado && <NavLink to="/itinerarios">Mis itinerarios</NavLink>}
      </nav>

      <div className="navbar__usuario">
        {estaAutenticado ? (
          <>
            <span>
              {sesion.nombre} <small>({sesion.rol})</small>
            </span>
            <button type="button" onClick={manejarSalir}>
              Salir
            </button>
          </>
        ) : (
          <NavLink to="/login">Ingresar</NavLink>
        )}
      </div>
    </header>
  )
}
