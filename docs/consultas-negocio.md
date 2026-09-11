# Consultas de negocio y el problema N+1 — SmartRoute (Laboratorio 3)

Las 4 consultas de negocio (2 JPQL + 2 Specifications) y la evidencia del problema N+1, con el SQL
real que genera Hibernate contra SQL Server. Capturado corriendo las pruebas de integración con
`logging.level.org.hibernate.SQL=DEBUG` (`backend/src/test/resources/application.properties`).

## El problema N+1 (Itinerario → ItinerarioParada)

`Itinerario.paradas` es una colección `LAZY` (criterio por defecto del proyecto — ver
[docs/adr/ADR-001](adr/ADR-001-arquitectura-en-capas.md) y el comentario de excepción en
`Lugar.java`). Pedir "todos los itinerarios" y después leer las paradas de cada uno en un bucle
dispara una consulta por itinerario, además de la que trae los itinerarios:

```sql
-- 1) trae los itinerarios (1 consulta)
select
    i1_0.id, i1_0.creado_en, i1_0.estado, i1_0.fecha,
    i1_0.presupuesto_total_estimado, i1_0.usuario_id
from
    itinerario i1_0

-- 2) una consulta más POR CADA itinerario, al acceder a getParadas()
select
    p1_0.itinerario_id, p1_0.id, p1_0.costo_estimado, p1_0.hora_fin,
    p1_0.hora_inicio, p1_0.lugar_id, p1_0.orden
from
    itinerario_parada p1_0
where
    p1_0.itinerario_id = ?
order by
    p1_0.orden
-- (se repite una vez por cada itinerario: 3 itinerarios → 3 consultas más)
```

Con 3 itinerarios en la base, `findAll()` + `getParadas()` en un bucle ejecuta **4 consultas** (1 +
3), en vez de 1. Prueba que lo demuestra:
`ItinerarioRepositoryIT.findAll_generaUnaConsultaPorItinerarioAlAccederParadas_evidenciaDelNMasUno`
(usa `SessionFactory.getStatistics().getPrepareStatementCount()` para contarlas, no solo mirar el
log).

### La corrección: `JOIN FETCH`

`ItinerarioRepository.itinerariosConfirmadosDeUsuario` (ver más abajo) trae itinerario y paradas en
**una sola consulta**:

```sql
select
    distinct i1_0.id, i1_0.creado_en, i1_0.estado, i1_0.fecha,
    p1_0.itinerario_id, p1_0.id, p1_0.costo_estimado, p1_0.hora_fin,
    p1_0.hora_inicio, p1_0.lugar_id, p1_0.orden,
    i1_0.presupuesto_total_estimado, i1_0.usuario_id
from
    itinerario i1_0
join
    itinerario_parada p1_0
        on i1_0.id = p1_0.itinerario_id
where
    i1_0.usuario_id = ?
    and i1_0.estado = 'CONFIRMADO'
order by
    i1_0.fecha desc,
    p1_0.orden
```

Prueba que lo confirma:
`ItinerarioRepositoryIT.itinerariosConfirmadosDeUsuario_traeParadasEnUnaSolaConsulta_correccionDelNMasUno`
— asegura `getPrepareStatementCount() == 1` después de recorrer todos los itinerarios y sus paradas.

## Las 4 consultas de negocio

### 1 · JPQL — `ItinerarioRepository.itinerariosConfirmadosDeUsuario(usuarioId)`

Es la misma consulta de la corrección del N+1 (arriba): el historial de itinerarios confirmados de
un usuario, con sus paradas ya cargadas.

```java
@Query("SELECT DISTINCT i FROM Itinerario i JOIN FETCH i.paradas "
        + "WHERE i.usuario.id = :usuarioId AND i.estado = 'CONFIRMADO' "
        + "ORDER BY i.fecha DESC")
List<Itinerario> itinerariosConfirmadosDeUsuario(@Param("usuarioId") Long usuarioId);
```

### 2 · JPQL — `ResenaRepository.promedioCalificacionPorLugar(lugarId)`

La agregación que justificó, en el Lab 2, dejar las Reseñas en SQL Server en vez de moverlas a
MongoDB.

```java
@Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.lugar.id = :lugarId")
Optional<Double> promedioCalificacionPorLugar(@Param("lugarId") Long lugarId);
```

SQL generado:

```sql
select
    avg(cast(r1_0.calificacion as float(53)))
from
    resena r1_0
where
    r1_0.lugar_id = ?
```

### 3 · Specification — `LugarSpecifications.conFiltrosOpcionales(...)`

Búsqueda dinámica para el catálogo administrable (todos los filtros son opcionales — a diferencia
de `RecomendacionFilters`, que exige los cuatro criterios completos para el motor de
recomendación). Ejemplo con provincia + presupuesto (sin categoría ni transporte):

```sql
select
    l1_0.id, l1_0.activo, l1_0.categoria_id, l1_0.costo_estimado, l1_0.creado_en,
    l1_0.descripcion, l1_0.nivel_accesibilidad, l1_0.nombre, l1_0.provincia_id,
    l1_0.requiere_transporte_propio
from
    lugar l1_0
where
    l1_0.activo = 1
    and l1_0.provincia_id = ?
    and l1_0.costo_estimado <= ?
```

Nótese que `activo = 1` siempre se incluye (no es opcional) y que la Specification solo agrega las
condiciones de los filtros que de verdad se pidieron — sin `categoria_id` ni
`requiere_transporte_propio` en el `WHERE` cuando no se filtró por ellos.

### 4 · Specification — `ItinerarioSpecifications.conFiltrosOpcionales(...)`

"Mis itinerarios" con filtros opcionales de usuario, estado y rango de fechas. Ejemplo con usuario +
estado:

```sql
select
    i1_0.id, i1_0.creado_en, i1_0.estado, i1_0.fecha,
    i1_0.presupuesto_total_estimado, i1_0.usuario_id
from
    itinerario i1_0
where
    i1_0.usuario_id = ?
    and i1_0.estado = ?
```

## Repositorios generalizados

`BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T>` (y su equivalente
Mongo, `BaseMongoRepository`) es lo que le da a `LugarRepository` e `ItinerarioRepository` el método
`findAll(Specification<T>)` usado arriba, sin que cada repositorio tenga que declarar
`JpaSpecificationExecutor` por su cuenta.
