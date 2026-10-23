import { BrowserRouter, Route, Routes } from 'react-router-dom'
import NavBar from './components/NavBar'
import RutaProtegida from './components/RutaProtegida'
import { AuthProvider } from './auth/AuthContext'
import InicioPage from './pages/InicioPage'
import LoginPage from './pages/LoginPage'
import LugaresPage from './pages/LugaresPage'
import RecomendacionesPage from './pages/RecomendacionesPage'
import ItinerariosPage from './pages/ItinerariosPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NavBar />
        <Routes>
          <Route path="/" element={<InicioPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/lugares" element={<LugaresPage />} />
          <Route
            path="/recomendaciones"
            element={
              <RutaProtegida>
                <RecomendacionesPage />
              </RutaProtegida>
            }
          />
          <Route
            path="/itinerarios"
            element={
              <RutaProtegida>
                <ItinerariosPage />
              </RutaProtegida>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
