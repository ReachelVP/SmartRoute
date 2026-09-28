import { BrowserRouter, Route, Routes } from 'react-router-dom'
import NavBar from './components/NavBar'
import { UsuarioProvider } from './context/UsuarioContext'
import ConfirmarItinerarioPage from './pages/ConfirmarItinerarioPage'
import InicioPage from './pages/InicioPage'
import LugaresPage from './pages/LugaresPage'
import RecomendacionesPage from './pages/RecomendacionesPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <UsuarioProvider>
        <NavBar />
        <Routes>
          <Route path="/" element={<InicioPage />} />
          <Route path="/lugares" element={<LugaresPage />} />
          <Route path="/recomendaciones" element={<RecomendacionesPage />} />
          <Route path="/confirmar-itinerario" element={<ConfirmarItinerarioPage />} />
        </Routes>
      </UsuarioProvider>
    </BrowserRouter>
  )
}

export default App
