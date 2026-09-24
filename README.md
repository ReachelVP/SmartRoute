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


## Stack

| Capa       | Tecnología                                    |
| ---------- | ---------------------------------------------- |
| Backend    | Java 21 · Spring Boot 4 · Gradle              |
| Frontend   | React · Vite                                  |
| Base de datos | SQL Server (relacional) · MongoDB (documental) — ver [modelo de datos](docs/modelo-datos.md) |
| Pruebas    | JUnit 5 (reglas puras) + Testcontainers (integración contra SQL Server/Mongo reales) |
| CI         | GitHub Actions (`.github/workflows/ci.yml`)   |

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
- [Docker](https://www.docker.com/) (para SQL Server y MongoDB)

### Base de datos

```bash
docker compose up -d
```

Levanta SQL Server y MongoDB, crea la base `smartroute`, aplica las migraciones y siembra ambas
bases — sin necesidad de correr la aplicación (ver [detalle del modelo de datos](docs/modelo-datos.md)).

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

### Pruebas

```bash
cd backend
./gradlew test
```

No hace falta `docker compose up` para esto: las pruebas de integración levantan su propio SQL
Server y Mongo con [Testcontainers](https://testcontainers.com/) (contenedores desechables,
independientes de los de desarrollo) — solo necesitás Docker corriendo. Las reglas de negocio puras
y las pruebas con Mockito (34 pruebas en total) ni siquiera necesitan eso. Detalle de las consultas
de negocio y la corrección de un problema N+1 real en
[docs/consultas-negocio.md](docs/consultas-negocio.md); los dos procesos de negocio, sus reglas,
excepciones, patrones y la prueba de rollback transaccional en
[docs/capa-negocio.md](docs/capa-negocio.md).

`./gradlew check` (parte de `build`) además verifica con JaCoCo que la capa de negocio
(`cr.ac.una.smartroute.business*`) tenga al menos 70 % de cobertura de líneas — falla el build si
no.

## Integración continua

Cada `push` o `pull request` a `main` dispara el workflow de GitHub Actions (`.github/workflows/ci.yml`),
que compila y prueba el backend (`./gradlew build`, con sus propias pruebas de integración vía
Testcontainers) y el frontend (`npm run build`) por separado. El estado se puede ver en la pestaña
**Actions** del repositorio.

## Arquitectura

Ver el [diagrama de arquitectura](docs/diagrama-arquitectura.md) con el detalle de las capas y su
interacción, [ADR-001](docs/adr/ADR-001-arquitectura-en-capas.md) con el porqué de esa organización
y las alternativas que se descartaron, [docs/consultas-negocio.md](docs/consultas-negocio.md) con
las consultas de negocio (JPQL + Specifications) y la corrección de un problema N+1 real, y
[docs/capa-negocio.md](docs/capa-negocio.md) con los dos procesos de negocio, sus patrones y la
garantía transaccional verificada con rollback.

## API

| Método | Endpoint | Descripción |
| --- | --- | --- |
| GET | `/api/salud` | Chequeo simple de salud. |
| GET | `/api/lugares?provinciaId=` | Lugares de una provincia. |
| POST | `/api/recomendaciones` | Proceso 1: recomienda lugares según el `PerfilPreferencias` del usuario y la provincia pedida. |
| POST | `/api/itinerarios` | Proceso 2: confirma un itinerario (todo o nada) con las paradas elegidas. |

```bash
curl -X POST http://localhost:8080/api/recomendaciones \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"provinciaId":1}'

curl -X POST http://localhost:8080/api/itinerarios \
  -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"fecha":"2026-12-20","confirmarExcesoPresupuesto":false,"paradas":[
        {"lugarId":3,"horaInicio":"09:00","horaFin":"11:00"},
        {"lugarId":4,"horaInicio":"11:30","horaFin":"13:00"}
      ]}'
```

## Próximos pasos

- [x] Propuesta de dominio (entidades y procesos de negocio) — [docs/propuesta-dominio.md](docs/propuesta-dominio.md)
- [x] Diagrama de arquitectura — [docs/diagrama-arquitectura.md](docs/diagrama-arquitectura.md)
- [x] ADR de al menos una decisión de arquitectura — [ADR-001](docs/adr/ADR-001-arquitectura-en-capas.md)
- [x] Modelo de datos relacional (SQL Server) + subdominio documental (MongoDB) — Laboratorio 2 — [docs/modelo-datos.md](docs/modelo-datos.md)
- [x] Repositorios generalizados, consultas de negocio (JPQL + Specifications) y corrección de un N+1 real, con pruebas de integración vía Testcontainers — Laboratorio 3 — [docs/consultas-negocio.md](docs/consultas-negocio.md)
- [x] Los 2 procesos de negocio como servicios con reglas/excepciones propias, frontera DTO, 2 patrones de diseño (Specification, Factory), transacción con rollback probado y ≥70 % de cobertura JaCoCo — Laboratorio 4 — [docs/capa-negocio.md](docs/capa-negocio.md)
- [ ] Que el motor de recomendación genere 1–2 itinerarios candidatos completos (hoy se confirman las paradas que el cliente ya eligió)
- [ ] Flujo de negocio completo conectado al frontend
- [ ] Que `RecomendacionService` escriba en la bitácora de MongoDB en cada recomendación real

