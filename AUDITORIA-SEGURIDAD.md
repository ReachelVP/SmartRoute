# Auditoría de seguridad — SmartRoute (Laboratorio 7)

## Metodología

No tengo acceso al PDF de la lista de verificación de la Sesión 14 en este repositorio, así que
esta auditoría usa como marco el **OWASP Top 10:2025** (confirmado como la edición oficial vigente
en [owasp.org/www-project-top-ten](https://owasp.org/www-project-top-ten/) /
[top10.owasp.org/2025](https://top10.owasp.org/2025) al momento de escribir esto) — son 10
categorías, más que las 8 de una lista de verificación genérica, así que si la de la Sesión 14 es
un subconjunto de estas, queda cubierta igual. Cada fila es una categoría revisada contra el código
real de este repositorio (no una revisión genérica/teórica): o hay un hallazgo con su corrección
aplicada, o se explica concretamente cómo se verificó que no aplicaba.

Todas las correcciones de esta auditoría están en el mismo commit
(`8a7508c`, Laboratorio 7) salvo que se indique lo contrario.

## Tabla de hallazgos

| # | Categoría (OWASP Top 10:2025) | Hallazgo | Riesgo | Corrección aplicada | Commit |
|---|---|---|---|---|---|
| 1 | **A07 — Authentication Failures** (y A06 — Insecure Design) | `POST /auth/login` no tenía ningún límite de intentos: un atacante podía probar contraseñas sin parar contra el correo de un usuario real. | Alto — fuerza bruta de contraseña viable contra cualquier cuenta conocida (p. ej. los correos de la semilla, que son públicos en este mismo repositorio). | `LimitadorIntentosLogin` (en memoria, por correo): bloquea 15 minutos tras 5 fallos seguidos; `AuthService.login` lo consulta antes de verificar la contraseña. Nuevo `DemasiadosIntentosException` → **429** Problem Details. Probado en `ApiV1SecurityIT.login_devuelve429_trasDemasiadosIntentosFallidos` y en `LimitadorIntentosLoginTest` (5 pruebas unitarias). | `8a7508c` |
| 2 | **A02 — Security Misconfiguration** | La app no sabía que corría detrás del proxy de Render: sin `server.forward-headers-strategy`, Spring nunca ve la petición como HTTPS (el proxy termina el TLS), así que la cabecera **HSTS** de Spring Security nunca se agregaba, y el `Location:` de `POST /api/v1/lugares` (`ServletUriComponentsBuilder.fromCurrentRequest()`) podía salir como `http://` en vez de `https://`. | Medio — sin HSTS, un cliente que alguna vez use `http://` queda expuesto a downgrade; una URL `Location` incorrecta rompe clientes que la siguen tal cual. | `server.forward-headers-strategy=framework` en `application.properties` — Spring Boot ahora lee `X-Forwarded-Proto`/`X-Forwarded-Host` (que Render sí manda) y corrige ambas cosas. Sin efecto en dev (no hay proxy de por medio). | `8a7508c` |
| 3 | **A09 — Security Logging and Alerting Failures** | Los eventos de seguridad (login fallido, token inválido, acceso denegado, conflicto de concurrencia) no dejaban ningún rastro en el log — solo la respuesta HTTP al cliente. Sin esto, no hay forma de detectar un patrón de ataque desde los logs de producción. | Medio — sin registro, un incidente (fuerza bruta, escaneo de rutas con JWT robado, etc.) pasa inadvertido hasta que ya causó daño. | `log.warn("evento_seguridad=...")` agregado en `AuthService` (login fallido, con el correo intentado — nunca la contraseña), `JwtAuthenticationFilter` (token inválido/expirado), `ProblemDetailAuthenticationEntryPoint` (401), `ProblemDetailAccessDeniedHandler` (403) y `GlobalExceptionHandler` (acceso denegado, conflicto de concurrencia). Además, `logback-spring.xml` nuevo: en el perfil `prod` cada línea sale como JSON estructurado a stdout (`logstash-logback-encoder`), indexable por cualquier agregador de logs. | `8a7508c` |
| 4 | **A01 — Broken Access Control** | *Sin hallazgo.* | — | **Cómo se verificó:** `ApiV1SecurityIT` (Lab 5) ya prueba 401 (sin token) y 403 (rol insuficiente) contra endpoints reales con la cadena de seguridad completa, no mocks. Revisé cada `@PostMapping/@PutMapping/@DeleteMapping` bajo `presentation.v1` y `presentation.admin` contra las reglas de `SecurityConfig` uno por uno: ninguno queda sin una regla explícita que lo cubra. La verificación de propiedad del recurso (`RecursoNoPropioException`, un usuario no puede ver el itinerario de otro) también tiene prueba dedicada desde el Lab 5. | — |
| 5 | **A03 — Software Supply Chain Failures** | *Sin hallazgo nuevo.* | — | **Cómo se verificó:** `npm audit --omit=dev` sobre `spa/` → 0 vulnerabilidades. En el backend, las versiones de dependencias de seguridad directa (Spring Boot 4.1.0, Spring Security 7.1.0, jjwt 0.12.6, mssql-jdbc) están fijadas explícitamente en `build.gradle` y se actualizaron durante los Laboratorios 5–7 sin encontrar advisories activos conocidos; el resto de transitivas las resuelve el BOM de Spring Boot, que mantiene versiones consistentes entre sí. No se corrió un escáner tipo OWASP Dependency-Check (requiere sincronizar la base de datos NVD, impráctico en el tiempo de este laboratorio) — queda como mejora pendiente, documentada acá en vez de callada. | — |
| 6 | **A04 — Cryptographic Failures** | *Sin hallazgo.* | — | **Cómo se verificó:** contraseñas con BCrypt (`BCryptPasswordEncoder`, costo por defecto), nunca en texto plano ni en los logs (confirmado leyendo cada `log.*` que toca `correo`/`password` en este laboratorio). JWT firmado con HMAC vía `Keys.hmacShaKeyFor(...)` — la librería (`jjwt`) elige el algoritmo según el tamaño de la clave; el secreto de prod lo genera Render (`generateValue`, 256 bits, suficiente para HS256). El secreto nunca está en el repo (`application-prod.properties` usa `${JWT_SECRET}` sin valor por defecto — si falta, el arranque falla en vez de usar un valor débil). HTTPS lo termina Render; con el hallazgo #2 corregido, Spring ahora lo reconoce correctamente. | — |
| 7 | **A05 — Injection** | *Sin hallazgo.* | — | **Cómo se verificó:** `grep` de `createNativeQuery`, `@Query(nativeQuery = true)` y `Statement` en todo `src/main` → ningún resultado. Todo el acceso a datos pasa por Spring Data JPA (derivación de métodos, `Specification`/Criteria API, JPQL vía nombres de método) — parametrizado por diseño, sin concatenar strings de SQL en ningún punto del código. | — |
| 8 | **A06 — Insecure Design** | Ver hallazgo #1 (fuerza bruta de login) — es tanto un problema de diseño (falta un control esperado) como de autenticación. | Ver #1. | Ver #1. | `8a7508c` |
| 9 | **A08 — Software or Data Integrity Failures** | La rama `main` no tenía ninguna protección: cualquier push directo (propio o de un colaborador) llegaba a producción sin pasar por las verificaciones de CI, y el repo (privado) no permitía activar branch protection en el plan gratuito de GitHub. | Medio — un push roto (o un force-push accidental) podía desplegarse sin que nada lo frenara. | Repo pasado a público (`gh repo edit --visibility public`) para habilitar branch protection; `main` ahora exige que pasen los checks "Backend (Spring Boot)" y "SPA (React + Vite)" antes de permitir el merge, y bloquea force-push/delete. Además, el `deploy` job de CI (ver más abajo) solo dispara con `needs: [backend-build, spa-build]` — el deploy en sí mismo también queda condicionado a que todo pase, no solo el merge. | `8a7508c` (configuración de GitHub, no un archivo del repo) |
| 10 | **A10 — Mishandling of Exceptional Conditions** | *Sin hallazgo.* | — | **Cómo se verificó:** `GlobalExceptionHandler` ya centraliza todas las excepciones de negocio y el catch-all (`Exception.class`) desde el Lab 5 — nunca se devuelve una traza al cliente, solo un mensaje genérico, con el detalle real solo en el log del servidor. Confirmé además que esto también cubre los controladores Thymeleaf (`presentation.admin`, que no son `@RestController` y por lo tanto no pasan por `@RestControllerAdvice`): el valor por defecto de Spring Boot para `server.error.include-stacktrace` es `never`, así que una excepción no capturada en esos controladores tampoco filtra una traza, aunque caiga en la página de error por defecto en vez de en un `ProblemDetail`. | — |

## Nota sobre las credenciales de la semilla

Los correos y la contraseña `Demo1234!` de los usuarios de prueba (Reachel/USUARIO,
Kimberley/ADMIN) están en el repositorio a propósito — son datos de demostración para que quien
corrija el curso pueda entrar, no credenciales reales. No se consideran un hallazgo de esta
auditoría: "corregirlas" iría en contra de lo que pide la rúbrica del Laboratorio 6
("credenciales de un usuario de prueba por cada rol").

## Pendientes conocidos (no corregidos en este laboratorio)

- **A03**: no se corrió un escáner automatizado de dependencias (tipo OWASP Dependency-Check o
  GitHub Dependabot) — ver hallazgo #5. Activar Dependabot en el repo (ahora público, así que es
  gratuito) es el siguiente paso natural.
- **A07**: el límite de intentos de login (hallazgo #1) es en memoria, por instancia del proceso.
  Con más de una instancia de `smartroute-api` corriendo a la vez, cada una llevaría su propio
  conteo — para compartirlo entre instancias haría falta un almacén centralizado (p. ej. Redis),
  fuera del alcance de este laboratorio.
