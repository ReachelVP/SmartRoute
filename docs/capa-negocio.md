# Capa de negocio — SmartRoute (Laboratorio 4)

Los dos procesos de la propuesta de dominio implementados como casos de uso completos: DTO de
entrada validado → reglas de negocio → repositorios orquestados (en transacción cuando hay
escritura) → DTO de salida. Ninguna entidad JPA cruza hacia `presentation`.

## Proceso 1 — Generar recomendación personalizada de lugares

`RecomendacionService.recomendar(usuarioId, provinciaId)` — solo lectura
(`@Transactional(readOnly = true)`).

- **DTO de entrada:** `RecomendacionRequest(usuarioId, provinciaId)`, con `@NotNull` en ambos
  campos (Bean Validation, verificada en `RecomendacionController`). Las categorías de interés, el
  presupuesto máximo y si tiene transporte propio **no** se piden en la petición: se leen del
  `PerfilPreferencias` real del usuario, tal como describe la propuesta de dominio ("El usuario
  solicita recomendaciones a partir de su PerfilPreferencias").
- **Validación de negocio:** si el usuario no tiene un `PerfilPreferencias`, se lanza
  `PerfilPreferenciasIncompletoException` (`422`) — la validación explícita que pide el dominio en
  vez de fallar con un error genérico.
- **Reglas:** los cuatro filtros de `RecomendacionFilters` (Lab 1/2), sin cambios.
- **DTO de salida:** `RecomendacionResponse` (lista de `LugarResponse` o el criterio limitante si
  no hubo coincidencias).

## Proceso 2 — Confirmar itinerario

`ConfirmarItinerarioService.confirmar(ConfirmarItinerarioRequest)` — el proceso multi-paso, bajo
`@Transactional`.

- **DTO de entrada:** `ConfirmarItinerarioRequest(usuarioId, fecha, paradas, confirmarExcesoPresupuesto)`,
  con Bean Validation: `@NotNull`, `@FutureOrPresent` en la fecha, `@NotEmpty`/`@Size(max = 5)` en
  las paradas (la cantidad 1–5 es una restricción de forma, no una regla que necesite excepción
  propia) y `@Valid` en cascada sobre cada `ParadaConfirmacionRequest`.
- **Reglas y validaciones** (con excepción propia cada una):
  - `LugarInexistenteOInactivoException` — todos los lugares deben existir y estar activos.
  - `TransportePropioRequeridoException` — si el usuario no tiene transporte propio, ningún lugar
    elegido puede requerirlo.
  - `HorariosSolapadosException` — cada parada debe empezar después del fin de la anterior más un
    margen mínimo de traslado (15 min), y su propio fin debe ser posterior a su inicio.
  - `PresupuestoExcedidoException` — el costo total no puede superar el presupuesto máximo del
    perfil, salvo que `confirmarExcesoPresupuesto` sea `true`.
- **DTO de salida:** `ItinerarioResponse` (con sus `ParadaResponse`), mapeado manualmente desde la
  entidad guardada — nunca se devuelve `Itinerario` directamente desde el controlador.

**Alcance:** este laboratorio implementa la **confirmación** transaccional (el punto que la
propuesta de dominio marca explícitamente como "todo o nada"). La generación automática de 1–2
itinerarios candidatos por el motor de recomendación —descrita en la propuesta— sigue pendiente
(ver "Próximos pasos" en [docs/diagrama-arquitectura.md](diagrama-arquitectura.md)); hoy el cliente
envía directamente las paradas que quiere confirmar.

### La garantía transaccional, verificada

`ConfirmarItinerarioService.confirmar` guarda el `Itinerario` y todas sus `ItinerarioParada`
(`itinerarioRepository.save(itinerario)`, con cascada) **antes** de revisar el presupuesto. Si lo
excede sin confirmación, la excepción se lanza *después* de esa escritura — es `@Transactional`
quien deshace lo ya guardado, no una validación previa que evitó escribir. Sin el `@Transactional`
en el método de servicio, ese `save()` se habría confirmado en su propia mini-transacción (el
comportamiento por defecto de los repositorios de Spring Data) y la excepción posterior ya no
podría deshacerlo.

`ConfirmarItinerarioServiceRollbackIT` (Testcontainers, SQL Server real) prueba exactamente esto:
arma un itinerario de Kimberley (usuario 2, con transporte propio, presupuesto máximo 35 000) con
tres lugares reales que suman 41 000, espera `PresupuestoExcedidoException`, y confirma que
`itinerarioRepository.findAll()` no devuelve ningún itinerario para ese usuario y esa fecha después
del fallo.

## Frontera DTO

Ningún controlador ni servicio de negocio devuelve una entidad JPA directamente:
`presentation.dto` tiene un record de entrada y uno de salida por proceso (más
`ParadaConfirmacionRequest`/`ParadaResponse` para el detalle), y el mapeo es manual (métodos
estáticos `from(...)` en cada DTO de salida, como ya se hacía en `LugarResponse` desde el Lab 3).

## Patrones de diseño aplicados

### 1 · Specification

**Señal que lo justificó:** ya en el Laboratorio 3, `LugarSpecifications` e
`ItinerarioSpecifications` necesitaban combinar criterios de búsqueda **opcionales** (provincia,
categoría, presupuesto, estado, rango de fechas) sin escribir un método de repositorio por cada
combinación posible.

**Justificación:** encapsular cada condición como un `Specification<T>` componible permite que
`BaseRepository<T, ID>` le dé a *cualquier* entidad soporte de consultas dinámicas con una sola
línea (`extends JpaSpecificationExecutor<T>`), en vez de repetir la lógica de armar el `WHERE` en
cada repositorio que la necesite.

### 2 · Factory

**Señal que lo justificó:** construir un `Itinerario` confirmado no es copiar campos del DTO —
`ItinerarioFactory.crear` necesita asignar el `orden` de cada parada según su posición en la lista,
fijar `costoEstimado` desde el `Lugar` ya resuelto (no desde el request, que no lo trae) y sumar el
presupuesto total; ese cálculo no le corresponde al servicio (que orquesta reglas y repositorios)
ni al controlador.

**Justificación:** aislar esa construcción en `ItinerarioFactory.crear(usuario, fecha, paradas)`
mantiene a `ConfirmarItinerarioService` enfocado en las reglas y la transacción, y deja un único
lugar que cambiar si el día de mañana un itinerario se puede armar de otra forma (por ejemplo, a
partir de un plan ya generado por el motor de recomendación).

## Pruebas unitarias (Mockito) y cobertura

24 pruebas puramente unitarias en `cr.ac.una.smartroute.business` (sin Spring ni base de datos):
10 de `RecomendacionFilters` (Lab 3, JUnit puro) + 4 de `RecomendacionServiceTest` + 9 de
`ConfirmarItinerarioServiceTest` + 1 de `LugarServiceTest` (estas 14 últimas con Mockito,
`@ExtendWith(MockitoExtension.class)`), cubriendo camino feliz y cada camino de regla de ambos
procesos. Cobertura JaCoCo verificada en `cr.ac.una.smartroute.business*` (`build.gradle`,
`jacocoTestCoverageVerification`, mínimo 70 % de líneas, corre como parte de `./gradlew check` —
y por lo tanto de `./gradlew build` y del CI).
