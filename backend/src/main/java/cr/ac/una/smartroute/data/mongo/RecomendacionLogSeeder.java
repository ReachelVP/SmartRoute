package cr.ac.una.smartroute.data.mongo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra la colección {@code recomendaciones_log} con documentos de ejemplo la primera vez que la
 * aplicación arranca contra una base vacía. Reemplaza el uso de un script de inicialización de
 * Docker: la siembra vive versionada junto al resto del código, como las migraciones de Flyway.
 */
@Component
public class RecomendacionLogSeeder implements CommandLineRunner {

    private final RecomendacionLogRepository repository;

    public RecomendacionLogSeeder(RecomendacionLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(recomendacionReachel());
        repository.save(recomendacionKimberley());
    }

    private RecomendacionLog recomendacionReachel() {
        RecomendacionLog log = new RecomendacionLog();
        log.setUsuarioId(1L);
        log.setFecha(Instant.now().minus(2, ChronoUnit.DAYS));

        RecomendacionLog.Criterios criterios = new RecomendacionLog.Criterios();
        criterios.setProvinciaId(1L);
        criterios.setCategorias(List.of("Natural", "Cultural"));
        criterios.setPresupuestoMaximo(new BigDecimal("20000.00"));
        criterios.setTieneTransportePropio(false);
        log.setCriterios(criterios);

        RecomendacionLog.LugarSugerido museo = new RecomendacionLog.LugarSugerido();
        museo.setLugarId(3L);
        museo.setNombre("Museo Nacional de Costa Rica");
        museo.setCategoria("Histórica");
        museo.setCostoEstimado(new BigDecimal("3000.00"));
        museo.setScore(0.91);

        RecomendacionLog.LugarSugerido mercado = new RecomendacionLog.LugarSugerido();
        mercado.setLugarId(5L);
        mercado.setNombre("Mercado Central de San José");
        mercado.setCategoria("Social");
        mercado.setCostoEstimado(new BigDecimal("5000.00"));
        mercado.setScore(0.78);

        log.setLugaresSugeridos(List.of(museo, mercado));

        RecomendacionLog.ParadaPlan paradaMuseo = new RecomendacionLog.ParadaPlan();
        paradaMuseo.setLugarId(3L);
        paradaMuseo.setNombre("Museo Nacional de Costa Rica");
        paradaMuseo.setOrden(1);
        paradaMuseo.setHoraInicio("09:00");
        paradaMuseo.setHoraFin("11:00");

        RecomendacionLog.ParadaPlan paradaMercado = new RecomendacionLog.ParadaPlan();
        paradaMercado.setLugarId(5L);
        paradaMercado.setNombre("Mercado Central de San José");
        paradaMercado.setOrden(2);
        paradaMercado.setHoraInicio("11:30");
        paradaMercado.setHoraFin("13:00");

        RecomendacionLog.PlanGenerado plan = new RecomendacionLog.PlanGenerado();
        plan.setParadas(List.of(paradaMuseo, paradaMercado));
        plan.setCostoTotal(new BigDecimal("8000.00"));
        plan.setDuracionMinutos(240);
        plan.setScore(0.85);

        log.setPlanesGenerados(List.of(plan));
        return log;
    }

    private RecomendacionLog recomendacionKimberley() {
        RecomendacionLog log = new RecomendacionLog();
        log.setUsuarioId(2L);
        log.setFecha(Instant.now().minus(1, ChronoUnit.DAYS));

        RecomendacionLog.Criterios criterios = new RecomendacionLog.Criterios();
        criterios.setProvinciaId(6L);
        criterios.setCategorias(List.of("Natural"));
        criterios.setPresupuestoMaximo(new BigDecimal("35000.00"));
        criterios.setTieneTransportePropio(true);
        log.setCriterios(criterios);

        RecomendacionLog.LugarSugerido manuelAntonio = new RecomendacionLog.LugarSugerido();
        manuelAntonio.setLugarId(9L);
        manuelAntonio.setNombre("Parque Nacional Manuel Antonio");
        manuelAntonio.setCategoria("Natural");
        manuelAntonio.setCostoEstimado(new BigDecimal("18000.00"));
        manuelAntonio.setScore(0.95);

        log.setLugaresSugeridos(List.of(manuelAntonio));
        log.setPlanesGenerados(List.of());
        return log;
    }
}
