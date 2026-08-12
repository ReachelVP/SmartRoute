# SmartRoute

Página web para planificar actividades recreativas y turismo interno durante los fines de semana
en Costa Rica. A partir de las preferencias del usuario (provincia, categoría, presupuesto,
accesibilidad, transporte disponible), SmartRoute recomienda lugares y arma un itinerario para el
paseo.

> Proyecto académico del curso **EIF509 Desarrollo de Aplicaciones Basadas en Web** (UNA), y base
> del Trabajo Final de Graduación del mismo nombre. Originalmente concebido como app móvil, se
> desarrolla aquí como aplicación web.

## Equipo

- Reachel Paola Valverde Pérez
- Kimberley Julissa Gómez Quesada

## Stack

| Capa     | Tecnología                                  |
| -------- | -------------------------------------------- |
| Backend  | Java 21 · Spring Boot 4 · Gradle             |
| Frontend | React · Vite                                 |
| CI       | GitHub Actions (`.github/workflows/ci.yml`)  |

## Estructura del repositorio

```
SmartRoute/
├── backend/    # API REST organizada por capas (presentation, business, data, config)
├── frontend/   # Aplicación web (React + Vite)
└── .github/    # Workflows de integración continua
```

El backend sigue una arquitectura en capas con una sola dirección de dependencia:
**presentation → business → data**. Un controlador puede invocar un servicio; un servicio no
conoce la capa de presentación.

## Cómo correr el proyecto

### Requisitos

- [Java 21](https://adoptium.net/) (si no está instalado, Gradle intentará descargarlo
  automáticamente al compilar)
- [Node.js 20+](https://nodejs.org/)

### Backend

```bash
cd backend
./gradlew bootRun      # Windows: gradlew bootRun
```

Queda escuchando en `http://localhost:8080`. Verificar que está arriba:

```bash
curl http://localhost:8080/api/salud
curl http://localhost:8080/actuator/health
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Queda escuchando en `http://localhost:5173` y se conecta automáticamente al backend en el puerto
8080 (con el backend corriendo, la página muestra el estado de la conexión).

## Integración continua

Cada `push` o `pull request` a `main` dispara el workflow de GitHub Actions (`.github/workflows/ci.yml`),
que compila el backend (`./gradlew build`) y el frontend (`npm run build`) por separado. El estado
se puede ver en la pestaña **Actions** del repositorio.

## Próximos pasos (Laboratorio 1)

- [ ] Propuesta de dominio (entidades y procesos de negocio)
- [ ] Diagrama de arquitectura
- [ ] ADR de al menos una decisión de arquitectura (`/docs/adr/`)
