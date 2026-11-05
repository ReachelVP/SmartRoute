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
| Backend (API + admin) | Java 21 · Spring Boot 4 · Gradle · Thymeleaf (módulo `/admin`) |
| SPA        | React · Vite                                  |
| Base de datos | SQL Server (relacional, Azure SQL Database en producción) · MongoDB (documental, MongoDB Atlas en producción) — ver [modelo de datos](docs/modelo-datos.md) |
| Pruebas    | JUnit 5 (reglas puras) + Testcontainers (integración contra SQL Server/Mongo reales) |
| CI         | GitHub Actions (`.github/workflows/ci.yml`)   |
| Despliegue | Render (API + SPA como servicios separados) — ver [docs/diagrama-despliegue.md](docs/diagrama-despliegue.md) |

## Estructura del repositorio

```
SmartRoute/
├── backend/    # API REST + módulo admin Thymeleaf, por capas (presentation, business, data, config)
├── spa/        # Aplicación de una sola página para el usuario final (React + Vite)
├── render.yaml # Blueprint de despliegue (Render): ambos servicios en un solo archivo
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

Con el perfil `dev` (el default local), el backend también sirve el módulo administrativo en
`http://localhost:8080/admin/lugares` (login en `/login` — usá las credenciales de un usuario con
rol ADMIN, ver [docs/modelo-datos.md](docs/modelo-datos.md) para los usuarios de semilla).

### SPA

```bash
cd spa
npm install
npm run dev
```

Queda escuchando en `http://localhost:5173` y se conecta automáticamente al backend en el puerto
8080 (con el backend corriendo, la página muestra el estado de la conexión). Iniciá sesión en
`/login` con un usuario de la semilla para probar recomendaciones e itinerarios.

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
Testcontainers) y la SPA (`npm run build`) por separado. El estado se puede ver en la pestaña
**Actions** del repositorio.

## Arquitectura

Ver el [diagrama de arquitectura](docs/diagrama-arquitectura.md) con el detalle de las capas y su
interacción, [ADR-001](docs/adr/ADR-001-arquitectura-en-capas.md) con el porqué de esa organización
y las alternativas que se descartaron, [docs/consultas-negocio.md](docs/consultas-negocio.md) con
las consultas de negocio (JPQL + Specifications) y la corrección de un problema N+1 real, y
[docs/capa-negocio.md](docs/capa-negocio.md) con los dos procesos de negocio, sus patrones y la
garantía transaccional verificada con rollback.

## API

La API heredada sin versionar (abajo) sigue abierta — la usan `LugaresPage` (catálogo público) y
el chequeo de salud de la SPA. El resto de la SPA (login, recomendaciones, itinerarios) y el
módulo admin Thymeleaf usan `/api/v1/**`: versionada, paginada/filtrable, documentada con
OpenAPI/Swagger y protegida con JWT — detalle completo en
[docs/api-rest-seguridad.md](docs/api-rest-seguridad.md) y en la colección
[`backend/api-v1.http`](backend/api-v1.http).

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

### API v1 (JWT)

| Método | Endpoint | Rol/propiedad |
| --- | --- | --- |
| POST | `/auth/login` | Público — devuelve el token Bearer. |
| GET | `/api/v1/lugares`, `/api/v1/lugares/{id}` | Público, paginado/filtrable. |
| POST/PUT/DELETE | `/api/v1/lugares` / `/{id}` | ADMIN. |
| GET | `/api/v1/resenas`, `/api/v1/resenas/{id}` | Público, paginado. |
| POST | `/api/v1/resenas` | Autenticado (usuario sale del token). |
| POST | `/api/v1/recomendaciones` | Autenticado. |
| POST | `/api/v1/itinerarios` | Autenticado. |
| GET | `/api/v1/itinerarios`, `/api/v1/itinerarios/{id}` | Autenticado; USUARIO solo ve los propios, ADMIN ve todos. |

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"reachel.demo@ejemplo.com","password":"Demo1234!"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

curl http://localhost:8080/api/v1/itinerarios -H "Authorization: Bearer $TOKEN"
```

## Despliegue y configuración (Laboratorio 6)

Dos servicios separados en Render (ver [`render.yaml`](render.yaml) y
[docs/diagrama-despliegue.md](docs/diagrama-despliegue.md)): `smartroute-api` (Docker, API + módulo
`/admin`) y `smartroute-web` (sitio estático, la SPA). Base relacional en Azure SQL Database, base
documental en MongoDB Atlas. Guía paso a paso (cuentas, conexión del Blueprint, verificación) en
[docs/guia-despliegue.md](docs/guia-despliegue.md).

**Perfiles**: `dev` (default local, `application-dev.properties`, valores fijos contra
`docker compose up`) y `prod` (`application-prod.properties`, todo desde variables de entorno de
la plataforma — nunca un valor fijo en el repo).

**Variables de entorno de producción** (nombres únicamente — los valores viven en el dashboard de
Render, nunca acá ni en el historial de Git):

| Variable | Para qué |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Activa `application-prod.properties` (`prod`) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a Azure SQL Database |
| `MONGODB_URI` | Conexión a MongoDB Atlas |
| `JWT_SECRET` | Firma de los JWT — distinto del de `application-dev.properties`; Render lo genera solo (`generateValue` en `render.yaml`) |
| `CORS_ALLOWED_ORIGIN` | URL pública de `smartroute-web`, para que el navegador pueda llamar a la API entre orígenes |
| `VITE_API_BASE_URL` | (build de `smartroute-web`) URL pública de `smartroute-api` |
| `PORT` | La inyecta Render — Spring Boot escucha en `${PORT:8080}` |

## Seguridad y robustez (Laboratorio 7)

Auditoría de seguridad completa (lista de verificación + OWASP Top 10:2025) en
[AUDITORIA-SEGURIDAD.md](AUDITORIA-SEGURIDAD.md).

**Concurrencia — por qué `Lugar` tiene contención real:** el catálogo de lugares lo edita más de
un administrador (vía `/admin/lugares` o vía `/api/v1/lugares`), así que dos personas pueden abrir
el mismo lugar para editarlo al mismo tiempo; con bloqueo optimista (`@Version`), quien guarda
segundo con una versión ya desactualizada recibe 409 en vez de pisar silenciosamente el cambio de
la primera. Ningún otro recurso del dominio tiene este patrón: `Itinerario` y `PerfilPreferencias`
son de un solo usuario (nadie más los edita), y `Resena` solo se crea, nunca se edita.

## Próximos pasos

- [x] Propuesta de dominio (entidades y procesos de negocio) — [docs/propuesta-dominio.md](docs/propuesta-dominio.md)
- [x] Diagrama de arquitectura — [docs/diagrama-arquitectura.md](docs/diagrama-arquitectura.md)
- [x] ADR de al menos una decisión de arquitectura — [ADR-001](docs/adr/ADR-001-arquitectura-en-capas.md)
- [x] Modelo de datos relacional (SQL Server) + subdominio documental (MongoDB) — Laboratorio 2 — [docs/modelo-datos.md](docs/modelo-datos.md)
- [x] Repositorios generalizados, consultas de negocio (JPQL + Specifications) y corrección de un N+1 real, con pruebas de integración vía Testcontainers — Laboratorio 3 — [docs/consultas-negocio.md](docs/consultas-negocio.md)
- [x] Los 2 procesos de negocio como servicios con reglas/excepciones propias, frontera DTO, 2 patrones de diseño (Specification, Factory), transacción con rollback probado y ≥70 % de cobertura JaCoCo — Laboratorio 4 — [docs/capa-negocio.md](docs/capa-negocio.md)
- [x] API `/api/v1` versionada (verbos/códigos correctos, paginación/filtros/orden), seguridad JWT con 2 roles y verificación de propiedad del recurso (OWASP API1), errores Problem Details (RFC 9457), documentación OpenAPI/Swagger y pruebas de integración de los códigos 201/400/401/403/404/409/422 — Laboratorio 5 — [docs/api-rest-seguridad.md](docs/api-rest-seguridad.md)
- [x] Módulo administrativo Thymeleaf (lugares y categorías: listado paginado, formulario validado, Post/Redirect/Get, CSRF), SPA en React con login JWT y el proceso principal (confirmar itinerario), despliegue en Render con perfiles `dev`/`prod` y secretos por variable de entorno — Laboratorio 6 — [docs/diagrama-despliegue.md](docs/diagrama-despliegue.md), [docs/guia-despliegue.md](docs/guia-despliegue.md)
- [x] Auditoría de seguridad (lista de verificación + OWASP Top 10:2025) con hallazgos y correcciones, bloqueo optimista (`@Version`) en `Lugar` con prueba de conflicto, pipeline de GitHub Actions con despliegue automático gateado por las pruebas, protección de `main`, y registros estructurados de eventos de seguridad — Laboratorio 7 — [AUDITORIA-SEGURIDAD.md](AUDITORIA-SEGURIDAD.md)
- [ ] Que el motor de recomendación genere 1–2 itinerarios candidatos completos (hoy se confirman las paradas que el cliente ya eligió)
- [ ] Que `RecomendacionService` escriba en la bitácora de MongoDB en cada recomendación real

