# ADR-001 · Arquitectura en capas con dependencia unidireccional

## Estado

Aceptada · Fecha: 30/07/2026 · Responsable: Reachel Paola Valverde Pérez

## Contexto

SmartRoute se construye en solitario, a lo largo de todo un ciclo lectivo, entregándose por
laboratorios incrementales (esqueleto en el Lab 1, capa de datos en el Lab 2, transacciones más
adelante, autenticación y roles después). Eso impone dos restricciones concretas: el código tiene
que poder crecer laboratorio a laboratorio sin reescribirse, y como no hay una segunda persona
revisando el diseño en tiempo real, la estructura del proyecto tiene que ser lo bastante obvia como
para que cualquiera (incluida yo misma, meses después) entienda dónde va cada cosa sin tener que
releer todo el código primero.

El dominio en sí (Usuario, PerfilPreferencias, Lugar, Categoria, Provincia, Itinerario,
ItinerarioParada, Reseña) tiene una separación de responsabilidades bastante natural: hay peticiones
HTTP que atender, reglas de negocio que aplicar (los filtros de recomendación del Proceso 1, las
validaciones del Proceso 2) y datos que persistir en dos motores distintos (SQL Server y MongoDB).
Había que decidir cómo organizar el código fuente del backend para reflejar esa separación.

## Decisión

El backend se organiza en cuatro paquetes con una única dirección de dependencia permitida:
**`presentation` → `business` → `data`**, más `config` como paquete transversal que no participa en
esa cadena. Un controlador en `presentation` puede invocar un servicio de `business`; un servicio
puede invocar un repositorio de `data`; nunca al revés, y `presentation` nunca accede a `data`
directamente saltándose `business` (ver `RecomendacionService`, que es quien consulta
`LugarRepository`, no `RecomendacionController`).

Es una organización **por capa técnica** (qué hace cada clase: recibir HTTP, aplicar reglas, leer o
guardar datos), no **por funcionalidad** (agrupar todo lo de "lugares" en una sola carpeta, todo lo
de "itinerarios" en otra, etc.).

## Alternativas consideradas

- **Organizar por funcionalidad (feature folders)**, por ejemplo `lugares/`, `itinerarios/`,
  `usuarios/`, cada una con su propio controlador, servicio y repositorio adentro. Se descartó
  porque, aunque escala mejor cuando el equipo crece (evita que muchas personas choquen editando el
  mismo paquete `business`), acá el equipo es una sola persona: ese problema no existe todavía. A
  cambio, agrupar por funcionalidad diluye la regla de dependencia unidireccional — nada impide que,
  dentro de la carpeta `lugares/`, alguien llame al repositorio directo desde el controlador porque
  "total, ya está ahí al lado" — y eso es justo lo que se quiere prevenir desde el Lab 1.

- **Sin capa de negocio explícita** (controladores llamando directo a los repositorios de Spring
  Data, sin `business` en el medio). Se descartó porque los procesos del dominio (los filtros de
  recomendación del Proceso 1, las validaciones "todo o nada" del Proceso 2) no son operaciones
  triviales de CRUD: tienen reglas, cálculos y validaciones que no le corresponden ni a un
  controlador HTTP ni a un repositorio JPA. Sin ese punto intermedio, esas reglas terminarían
  esparcidas entre controladores (difíciles de probar sin levantar un servidor) o filtradas hacia
  consultas SQL ad hoc (difíciles de probar sin una base de datos).

## Consecuencias

**Positivas**

- Las reglas de negocio (por ejemplo, `RecomendacionFilters`) quedan como funciones puras de
  `business`, sin depender de Spring ni de una base de datos real, así que se prueban con JUnit
  simple — más rápido y más confiable que una prueba de integración para cada regla.
- La regla "nunca al revés" es fácil de verificar con solo mirar los imports de un archivo: si un
  controlador importa algo de `data`, algo está mal.
- Los laboratorios siguientes (transacciones, autenticación) tienen un lugar obvio dónde ir: una
  transacción es lógica de `business` que coordina varias llamadas a `data`; un `@PreAuthorize` es
  de `presentation` o `config`, no de `data`.

**Negativas**

- Para agregar un endpoint nuevo hay que tocar mínimo tres paquetes (controlador, servicio,
  repositorio) en vez de un solo archivo — más ceremonia para cambios simples tipo CRUD.
- Si el proyecto creciera mucho (muchas entidades, equipo grande), esta organización por capa
  técnica se volvería menos cómoda que agrupar por funcionalidad, porque cada paquete (`business`,
  `data`) terminaría con decenas de archivos sin relación obvia entre sí más que "son servicios" o
  "son repositorios".

**Neutras**

- El nombre del paquete no dice nada sobre a qué entidad del dominio pertenece una clase (hay que
  abrir `LugarService.java` para saber que es de Lugar); en una organización por funcionalidad el
  nombre de la carpeta ya lo diría.

## Referencias

- Guía del Laboratorio 1 (EIF509, II Ciclo 2026): "Regla de oro: las dependencias van en una sola
  dirección: presentación → negocio → datos. Nunca al revés."
- [docs/diagrama-arquitectura.md](../diagrama-arquitectura.md)
