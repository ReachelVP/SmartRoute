import { NavLink } from 'react-router-dom'
import { useUsuarioActual } from '../context/UsuarioContext'

export default function NavBar() {
  const { usuarios, usuarioActual, estado, seleccionarUsuario } = useUsuarioActual()

  return (
    <header className="navbar">
      <NavLink to="/" className="navbar__brand">
        SmartRoute
      </NavLink>

      <nav className="navbar__links">
        <NavLink to="/lugares">Lugares</NavLink>
        <NavLink to="/recomendaciones">Recomendaciones</NavLink>
        <NavLink to="/confirmar-itinerario">Confirmar itinerario</NavLink>
      </nav>

      <div className="navbar__usuario">
        {estado === 'cargando' && <span>Cargando usuarios...</span>}
        {estado === 'error' && <span>No se pudieron cargar los usuarios</span>}
        {estado === 'listo' && (
          <label>
            Usuario de prueba:{' '}
            <select
              value={usuarioActual?.id ?? ''}
              onChange={(evento) => seleccionarUsuario(Number(evento.target.value))}
            >
              {usuarios.map((usuario) => (
                <option key={usuario.id} value={usuario.id}>
                  {usuario.nombre}
                </option>
              ))}
            </select>
          </label>
        )}
      </div>
    </header>
  )
}
