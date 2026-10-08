# API REST versionada y seguridad — SmartRoute (Laboratorio 5)

## Coexistencia de dos APIs

La API heredada sin versionar (`/api/**`, Labs 1–4) sigue abierta (`permitAll()`) para no romper
el frontend existente, que todavía no implementa login. La API nueva, graded, vive bajo
**`/api/v1/**`**: versionada, documentada, paginada/filtrable y protegida con JWT. Es una decisión
de alcance deliberada bajo el plazo del laboratorio — migrar el frontend a la API v1 con sesión
real queda pendiente.

En `SecurityConfig`, las reglas de `/api/v1/**` van **antes** que el `permitAll()` genérico de
`/api/**`: Spring Security resuelve por la primera regla que haga match, así que si el orden fuera
al revés, el `permitAll()` heredado capturaría también todo `/api/v1/**` y lo dejaría público por
accidente.

## Autenticación — JWT sin estado

`POST /auth/login` (sin versionar, fuera de `/api`, como pide la guía) recibe `correo`/`password`,
verifica con `PasswordEncoder.matches(...)` contra el `password_hash` del `Usuario` y devuelve un
JWT (HMAC-SHA, `io.jsonwebtoken`/jjwt) con el `usuarioId` como *subject* y el `rol` como claim.

- `JwtAuthenticationFilter` (`OncePerRequestFilter`) lee `Authorization: Bearer <token>` en cada
  petición, lo valida y puebla el `SecurityContextHolder`. Un token ausente, corrupto o expirado
  simplemente no autentica — no lanza; es la regla de autorización de más abajo la que decide si
  la ruta necesitaba autenticación (401) o no.
- `SessionCreationPolicy.STATELESS`: no hay sesión de servidor ni cookie; cada petición se
  autentica desde cero con el token.
- No hay `AuthenticationManager`/`UserDetailsService` de Spring Security de por medio — el login es
  una verificación directa en `AuthService`, más simple y con el mismo nivel de seguridad.
- El secreto (`smartroute.jwt.secret`, en `application.properties`) es una clave de desarrollo en
  claro, marcada como tal en un comentario; en un entorno real vendría de una variable de entorno o
  un gestor de secretos.

## Autorización por rol

Dos roles (`USUARIO`, `ADMIN`; columna `usuario.rol`, migración `V3`). `SecurityConfig` exige
`hasRole("ADMIN")` en `POST/PUT/DELETE /api/v1/lugares/**` (alta, edición y baja del catálogo); el
resto de `/api/v1/**` solo exige estar autenticado, sin distinción de rol.

## Autorización por propiedad del recurso (OWASP API1 — Broken Object Level Authorization)

Dos reglas, aplicadas siempre en la capa de negocio (nunca confiando en lo que mande el cliente):

1. **Nunca se acepta un `usuarioId` del cuerpo de la petición.** Cada escritura
   (`POST /api/v1/resenas`, `POST /api/v1/recomendaciones`, `POST /api/v1/itinerarios`) saca el
   usuario del JWT vía `UsuarioAutenticado.id()`, no de un campo del DTO.
2. **Cada lectura por id o lista de un recurso propio de un usuario verifica propiedad en el
   servicio.** `ItinerarioConsultaService`: si el usuario autenticado no es ADMIN, la lista se
   fuerza a sus propios itinerarios (ignora cualquier `usuarioId` que mande en el query param) y
   `obtener(id, ...)` lanza `RecursoNoPropioException` (403) si el itinerario existe pero es de
   otro usuario. Un ADMIN puede ver cualquiera, y filtrar explícitamente por `usuarioId`.

## Errores — Problem Details (RFC 9457)

Un único `@RestControllerAdvice` (`GlobalExceptionHandler`) traduce toda excepción de negocio a
`ProblemDetail`, para la API heredada y para `/api/v1` por igual:

| Excepción | HTTP |
|---|---|
| `CredencialesInvalidasException` | 401 |
| `RecursoNoPropioException` | 403 |
| `LugarInexistenteOInactivoException`, `ItinerarioInexistenteException`, `ResenaInexistenteException` | 404 |
| `MethodArgumentNotValidException` (`@Valid`), `IllegalArgumentException` | 400 |
| `ResenaDuplicadaException` | 409 |
| `PerfilPreferenciasIncompletoException`, `TransportePropioRequeridoException`, `HorariosSolapadosException`, `PresupuestoExcedidoException` | 422 |
| cualquier otra `Exception` | 500, con el detalle solo en el log del servidor — nunca una traza al cliente |

El 401 (token ausente/inválido) y el 403 (rol o propiedad insuficiente) de la cadena de *filtros*
de Spring Security —que ocurren antes de llegar a un controlador, así que el `@RestControllerAdvice`
no los puede interceptar— se manejan aparte, en `ProblemDetailAuthenticationEntryPoint` y
`ProblemDetailAccessDeniedHandler`, con el mismo formato de cuerpo. Esos dos escriben el JSON a
mano en vez de inyectar un `ObjectMapper`: Spring Boot 4 autoconfigura por defecto un `ObjectMapper`
de **Jackson 3** (`tools.jackson.databind`), no el de Jackson 2
(`com.fasterxml.jackson.databind`) que un bean más antiguo esperaría — y como el cuerpo de estas
dos respuestas es fijo (sin datos del request), evitar el `ObjectMapper` de por medio evita esa
ambigüedad de versión por completo.

## Paginación, orden y filtros

Todas las colecciones (`/api/v1/lugares`, `/api/v1/resenas`, `/api/v1/itinerarios`) usan
`Pageable` + `Specification` (patrón ya establecido en Labs 2/3) y devuelven `PaginaResponse<T>`
— un wrapper propio (`contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas`) en vez de
serializar el `Page<T>` nativo de Spring Data, para no acoplar el contrato público de la API a un
tipo interno del framework. El mapeo entidad → DTO ocurre siempre en el controlador
(`pagina.map(Response::from)`), nunca en `business`.

Filtros por recurso: `lugares` (provincia, categoría, accesibilidad, costo máximo — los mismos
`LugarSpecifications` del Lab 2/3), `resenas` (`lugarId`), `itinerarios` (`usuarioId` solo para
ADMIN, `estado`, `fechaDesde`/`fechaHasta`).

## `FetchType.EAGER` en las colecciones que cruzan a DTO

Con `spring.jpa.open-in-view=false`, la sesión de Hibernate se cierra al salir del método
`@Transactional` del servicio. Los controladores mapean entidad → DTO **después** de esa frontera,
así que cualquier asociación `LAZY` que un `*Response.from(...)` lea revienta con
`LazyInitializationException`. Mismo patrón que `Lugar.provincia`/`categoria` (Lab 2) y
`Resena.lugar`: `Itinerario.paradas` (`@OneToMany`, LAZY por defecto en JPA) y
`ItinerarioParada.lugar` se marcaron `EAGER` porque `ItinerarioResponse`/`ParadaResponse` los leen
fuera de la transacción.

## Documentación — OpenAPI / Swagger

`springdoc-openapi-starter-webmvc-ui` expone `/v3/api-docs` y `/swagger-ui.html`, con el esquema de
seguridad `bearerAuth` declarado en `OpenApiConfig` (botón "Authorize" en Swagger UI). La colección
de peticiones manuales está en [`backend/api-v1.http`](../backend/api-v1.http) (formato `.http`,
compatible con el plugin REST Client de VS Code y con IntelliJ): cubre login, los 3 recursos de
`/api/v1` y, a propósito, los casos de error de la rúbrica (400, 401, 403, 404, 409, 422).

## Pruebas

`ApiV1SecurityIT` (Testcontainers, SQL Server y Mongo reales) ejercita la API completa a través de
`MockMvc` con la cadena real de filtros de seguridad activa (no solo la capa de negocio): login,
201+`Location`, 400 de validación, 401 sin token, 403 por rol, 404, 409 por duplicado y 422 por
regla de negocio (horarios solapados).
