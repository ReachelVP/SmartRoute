# Propuesta de dominio — SmartRoute

Laboratorio 1 · EIF509 · II Ciclo 2026

## 1 · Identificación

**Equipo y sistema**

Reachel Paola Valverde Pérez. Smart Route.

## 2 · El negocio

**Descripción del negocio**

Es una aplicación web orientada a apoyar a los usuarios en la planificación de actividades
recreativas y turismo interno durante los fines de semana en Costa Rica. El sistema recomendará
lugares turísticos y recreativos según la provincia, categoría y preferencias del usuario. Ya que
hoy en día muchos costarricenses no conocen o no saben turistear dentro del país conociendo rutas
o actividades nuevas.

La aplicación incluirá información sobre destinos naturales, culturales, históricos y sociales,
facilitando la búsqueda de opciones según criterios como presupuesto, accesibilidad y
disponibilidad de transporte. Además, el sistema incorporará funcionalidades de recomendación
inteligente para sugerir actividades y lugares recreativos acordes con las preferencias del
usuario, mejorando la experiencia de planificación y exploración turística.

**Actores**

Los usuarios a utilizar el sistema serían cualquier persona que desee realizar planes fuera de su
casa en el territorio costarricense un fin de semana, sea sábado o domingo. El usuario podría crear
una cuenta que guarde sus preferencias anteriores e histórico de itinerarios.

## 3 · Entidades de negocio

**Listado de entidades**

- **Usuario** — persona registrada que usa la plataforma para planificar sus salidas. Datos
  principales: id, nombre, correo, contraseña (hash), provincia de residencia, fecha de registro.
- **PerfilPreferencias** — conjunto de preferencias que el usuario configura para recibir
  recomendaciones personalizadas. Datos principales: id, presupuesto (económico/medio/alto),
  categorías de interés, indica si tiene transporte propio, radio de distancia máximo.
- **Provincia** — división geográfica de Costa Rica usada para ubicar lugares y filtrar búsquedas.
  Datos principales: id, nombre.
- **Categoria** — clasifica los lugares recreativos (natural, cultural, histórica, social). Datos
  principales: id, nombre, descripción.
- **Lugar** — sitio turístico o recreativo disponible para visitar. Datos principales: id, nombre,
  descripción, costo estimado, nivel de accesibilidad, requiere transporte propio (booleano),
  ubicación/coordenadas.
- **Itinerario** — plan de paseo que arma o recibe un usuario para una fecha específica (ej. un fin
  de semana). Datos principales: id, fecha, presupuesto total estimado, estado
  (borrador/confirmado).
- **ItinerarioParada** — línea de detalle que asocia un Lugar a un Itinerario, con su orden y
  horario dentro del plan. Datos principales: id, orden, hora de inicio, hora de fin, costo
  estimado.
- **Reseña** — opinión que un usuario deja sobre un Lugar luego de visitarlo. Datos principales:
  id, calificación (1–5), comentario, fecha.

**Relaciones**

- Un Usuario tiene un PerfilPreferencias.
- Un Usuario tiene muchos Itinerarios.
- Un Itinerario tiene muchas ItinerarioParada.
- Una ItinerarioParada referencia un Lugar.
- Un Lugar pertenece a una Provincia.
- Un Lugar pertenece a una Categoria.
- Un Usuario escribe muchas Reseñas.
- Un Lugar tiene muchas Reseñas.

**Subdominio documental candidato**

Las Reseñas (comentarios de texto libre, longitud variable, sin estructura rígida) son buena
candidata para MongoDB. También podés considerar una Bitácora de recomendaciones generadas — un
historial tipo log donde cada vez que el módulo de árbol de decisión sugiere lugares a un usuario,
se guarda qué criterios usó y qué le recomendó; es información de esquema flexible y de solo
lectura/append, típica de un catálogo/histórico documental. No hace falta programar ninguna
todavía — se implementa en el Laboratorio 2.

> Implementado en el Laboratorio 2: se eligió la **Bitácora de recomendaciones** para MongoDB, y
> las Reseñas se quedaron en el modelo relacional (necesitan agregación — promedio de calificación
> por lugar). Ver el [modelo de datos](modelo-datos.md) para el detalle y la justificación.

## 4 · Procesos de negocio

### Proceso 1 — Generar recomendación personalizada de lugares

El usuario solicita recomendaciones a partir de su PerfilPreferencias. El sistema filtra y ordena
los Lugar disponibles usando un árbol de decisión.

**Reglas**

- Solo se consideran lugares cuya Categoria esté entre las categorías de interés del perfil.
- Solo se consideran lugares de la Provincia seleccionada por el usuario para ese paseo.
- Si el usuario indicó que no tiene transporte propio, se excluyen los lugares marcados como
  "requiere transporte propio".
- Se excluyen lugares cuyo costo estimado supere el presupuesto máximo definido en el perfil.

**Cálculos**

- Puntaje (score) de cada lugar según el árbol de decisión, ponderando categoría, accesibilidad y
  qué tan cerca está del presupuesto ideal del usuario.
- Ordenamiento de mayor a menor puntaje y selección del top N (ej. 5) para mostrar como sugerencia.

**Validaciones**

- El usuario debe tener un PerfilPreferencias completo antes de pedir recomendaciones; si no lo
  tiene, se le pide completarlo primero.
- Si ningún lugar cumple todos los filtros, el sistema no devuelve una lista vacía sin más — le
  indica al usuario qué criterio está limitando demasiado (ej. presupuesto muy bajo) y sugiere
  relajarlo.

> Implementado parcialmente en el código: los cuatro filtros (`RecomendacionFilters`) y la
> validación de "no devolver una lista vacía sin explicación" ya están programados y probados con
> pruebas unitarias — ver [diagrama de arquitectura](diagrama-arquitectura.md). El puntaje/árbol de
> decisión y el top N quedan para un laboratorio posterior.

### Proceso 2 — Confirmar itinerario

El usuario elige uno o varios lugares (de las recomendaciones o de una búsqueda manual) y arma su
plan para una fecha específica. El sistema crea el Itinerario junto con todas sus
ItinerarioParada.

**Reglas**

- El itinerario debe tener entre 1 y 5 paradas (ItinerarioParada) por día.
- Los horarios de las paradas no pueden solaparse: la hora de inicio de una parada debe ser
  posterior a la hora de fin de la anterior, dejando además un margen mínimo de traslado entre
  lugares.
- Si el usuario no tiene transporte propio, todos los lugares elegidos deben ser accesibles entre
  sí sin transporte propio (a pie o transporte público).

**Cálculos**

- Costo total del itinerario = suma del costo estimado de cada parada.
- Duración total = diferencia entre la hora de fin de la última parada y la hora de inicio de la
  primera.

**Validaciones**

- El costo total calculado no puede superar el presupuesto máximo del PerfilPreferencias del
  usuario, salvo que confirme explícitamente que quiere excederlo.
- Todos los lugares referenciados deben existir y estar activos al momento de confirmar.
- **Todo o nada:** el Itinerario y todas sus ItinerarioParada se crean en una sola operación — si
  una sola parada falla una validación (horario, lugar inexistente, presupuesto excedido sin
  confirmación), no se guarda ningún registro. Este es el punto que se implementó como transacción
  en el Laboratorio 4.

> Implementado en el Laboratorio 4: `ConfirmarItinerarioService` (`@Transactional`) valida
> existencia/actividad del lugar, transporte propio, solapamiento de horarios (con margen mínimo)
> y presupuesto excedido, cada una con su excepción propia; una prueba de integración con
> Testcontainers verifica que el fallo por presupuesto deshace también la escritura ya hecha — ver
> [docs/capa-negocio.md](capa-negocio.md). Lo que sigue pendiente: que el sistema *sugiera* 1–2
> itinerarios candidatos (hoy el cliente ya elige las paradas que quiere confirmar).

## 5 · Alcance

**Dentro del alcance**

- Catálogo administrable de Lugares, Categorías y Provincias (altas, bajas, ediciones) por un
  usuario administrador.
- Registro y autenticación de Usuarios, con roles (usuario regular / administrador) — se implementa
  en el Laboratorio 5.
- Gestión del PerfilPreferencias de cada usuario (presupuesto, categorías de interés, transporte
  propio, radio de distancia).
- Motor de recomendación basado en árbol de decisión con reglas definidas por mí (filtros +
  puntaje), no aprendizaje automático entrenado con datos reales.
- Generación de 1 a 2 itinerarios sugeridos completos por solicitud, y confirmación/persistencia
  del que el usuario elija (proceso transaccional, Laboratorio 4).
- Reseñas de lugares y una bitácora de recomendaciones generadas, como subdominio documental en
  MongoDB (Laboratorio 2).
- Backend REST en capas (Spring Boot) + frontend web (React) que consume esa API — aplicación 100%
  web, sin app móvil.
- Documentación del proceso: ADRs, diagrama de arquitectura, CI.

**Fuera del alcance**

- Facturación electrónica ante Hacienda.
- Integraciones en tiempo real con APIs externas de mapas/tránsito (Google Maps, Waze) para
  calcular rutas o tiempos de traslado reales; el costo y tiempo estimado se basan en datos
  precargados por el administrador, no en cálculos en vivo.
- Reservas o gestión de cupos/disponibilidad en tiempo real con terceros (hoteles, tours,
  restaurantes).
- Portal para que los propios lugares/negocios administren su información — solo el administrador
  del sistema carga y mantiene el catálogo.
- Notificaciones push o por correo/SMS.
- Soporte multi-idioma.
- Recomendación basada en machine learning entrenado con datos reales de usuarios (queda para un
  trabajo futuro fuera del curso).
