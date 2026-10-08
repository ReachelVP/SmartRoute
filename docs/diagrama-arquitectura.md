# Diagrama de arquitectura — SmartRoute

Arquitectura en capas del backend (Spring Boot) y su interacción con el frontend (React) y las
bases de datos. Refleja el código entregado a la fecha (Laboratorios 1 a 4): los dos procesos de
negocio completos (DTO validado → reglas → repositorios en transacción → DTO de salida), los
repositorios generalizados, las consultas de negocio, la corrección del problema N+1 y la garantía
transaccional del Proceso 2, verificada con rollback
(ver [docs/consultas-negocio.md](consultas-negocio.md) y [docs/capa-negocio.md](capa-negocio.md)).

```mermaid
flowchart TD
    subgraph Frontend["Frontend · React + Vite — localhost:5173"]
        UI["App.jsx<br/>Página de bienvenida"]
    end

    subgraph Backend["Backend · Spring Boot — localhost:8080"]
        direction TB

        subgraph Presentation["presentation"]
            HC["HealthController<br/>GET /api/salud"]
            LC["LugarController<br/>GET /api/lugares"]
            RC["RecomendacionController<br/>POST /api/recomendaciones"]
            IC["ItinerarioController<br/>POST /api/itinerarios"]
            GEH["GlobalExceptionHandler"]
        end

        subgraph Business["business"]
            LS["LugarService"]
            RS["RecomendacionService"]
            RF["RecomendacionFilters<br/>(funciones puras — filtros del Proceso 1)"]
            CIS["ConfirmarItinerarioService<br/>@Transactional — Proceso 2"]
            IF["ItinerarioFactory<br/>(Factory)"]
            EXC["excepciones de negocio<br/>(business.exception)"]
        end

        subgraph Data["data"]
            BASE["BaseRepository&lt;T, ID&gt;<br/>JpaRepository + JpaSpecificationExecutor"]
            LR["LugarRepository, ItinerarioRepository<br/>+ 6 repositorios JPA más"]
            SPEC["LugarSpecifications<br/>ItinerarioSpecifications"]
            SEED["RecomendacionLogSeeder<br/>(bootstrap, no request)"]
            RL["RecomendacionLogRepository<br/>(Mongo, extends BaseMongoRepository)"]
        end

        subgraph Config["config"]
            CC["CorsConfig"]
        end
    end

    subgraph Bases["Bases de datos"]
        SQL[("SQL Server<br/>smartroute")]
        MONGO[("MongoDB<br/>recomendaciones_log")]
    end

    UI -- "fetch GET /api/salud" --> HC

    LC --> LS
    LS --> LR
    RC --> RS
    RS --> RF
    RS --> LR
    IC --> CIS
    CIS --> IF
    CIS -. "lanza" .-> EXC
    CIS --> LR
    LR -.-> BASE
    LR -.-> SPEC
    LR --> SQL
    SEED --> RL
    RL -.-> BASE
    RL --> MONGO
    GEH -. "traduce excepciones a HTTP" .-> EXC

    CC -. "aplica CORS sobre /api/**" .-> Presentation
```

**Nota de fidelidad:** `RecomendacionLogRepository` y la colección `recomendaciones_log` de Mongo
existen y están sembradas (Laboratorio 2), pero hoy solo las alimenta `RecomendacionLogSeeder` al
arrancar la aplicación — `RecomendacionService` todavía no escribe ahí cada vez que genera una
recomendación real. Conectar ambas cosas es un paso pendiente (ver más abajo).

## Capas y responsabilidades

| Capa | Paquete | Responsabilidad | Contenido actual |
| --- | --- | --- | --- |
| Presentación | `cr.ac.una.smartroute.presentation` | Recibe peticiones HTTP, valida forma de entrada y devuelve DTOs (nunca entidades JPA). | `HealthController`, `LugarController`, `RecomendacionController`, DTOs en `presentation.dto`. |
| Negocio | `cr.ac.una.smartroute.business` | Reglas, cálculos y validaciones del dominio. No conoce HTTP; no construye SQL. | `LugarService`, `RecomendacionService` (Proceso 1) y `ConfirmarItinerarioService` (Proceso 2, `@Transactional`), con `RecomendacionFilters` e `ItinerarioFactory` como reglas/construcción puras y `business.exception` como las excepciones propias de cada regla. 24 pruebas (10 JUnit puro + 14 Mockito) y ≥70 % de cobertura JaCoCo — ver [docs/capa-negocio.md](capa-negocio.md). |
| Datos | `cr.ac.una.smartroute.data` | Acceso y persistencia. | 8 entidades JPA + repositorios generalizados (`BaseRepository` con soporte de Specifications) sobre SQL Server, y `RecomendacionLog` + `BaseMongoRepository` sobre MongoDB. Dos consultas JPQL y dos Specifications documentadas en [docs/consultas-negocio.md](consultas-negocio.md). |
| Configuración | `cr.ac.una.smartroute.config` | Configuración transversal, fuera del flujo de negocio. | `CorsConfig`, `OpenApiConfig`, `SecurityConfig` + `config.security` (JWT: filtro, servicio de tokens, handlers de 401/403). |

## Regla de dependencia

Las dependencias fluyen en una sola dirección: **presentación → negocio → datos**. Por ejemplo,
`RecomendacionController` nunca llama a `LugarRepository` directamente — pasa siempre por
`RecomendacionService`, que es quien conoce tanto las reglas (`RecomendacionFilters`) como el dato
real. La razón de esta regla, y las alternativas que se descartaron, están documentadas en
[ADR-001](adr/ADR-001-arquitectura-en-capas.md).

## Frontend

El frontend (`frontend/`, React + Vite) es un cliente independiente que hoy solo consume
`GET /api/salud` para validar la conexión; consumir `/api/lugares` y `/api/recomendaciones` queda
para un laboratorio posterior.

## Laboratorio 5 — API v1 y seguridad

Además de la API heredada de este diagrama, el backend expone ahora `/api/v1/**`: versionada,
paginada/filtrable, documentada con OpenAPI/Swagger y protegida con JWT (dos roles, USUARIO y
ADMIN, y verificación de propiedad del recurso en la capa de negocio — OWASP API1). Las dos APIs
coexisten a propósito, para no romper el frontend actual mientras no implemente login real.
Detalle completo en [docs/api-rest-seguridad.md](api-rest-seguridad.md).

## Próximos pasos

- Que `RecomendacionService` escriba en `recomendaciones_log` cada vez que resuelve una
  recomendación real (hoy la colección solo tiene los documentos de ejemplo del seeder).
- Que el motor de recomendación genere 1–2 itinerarios candidatos completos (hoy
  `ConfirmarItinerarioService` confirma las paradas que el cliente ya eligió, no las sugiere).
- Conectar el frontend a `/api/v1` con login real (hoy sigue consumiendo la API heredada sin
  autenticación).
