package cr.ac.una.smartroute.data.mongo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Bitácora de recomendaciones: un documento por cada vez que el motor de recomendación
 * (Proceso 1/2 del dominio) le genera sugerencias a un usuario. Se incrusta una foto de los
 * criterios y de los lugares/planes sugeridos en ese momento (no debe cambiar si el lugar real se
 * edita después); {@code usuarioId} es una referencia plana hacia el usuario en SQL Server.
 *
 * <p>El índice {@code usuarioId + fecha} responde el patrón de lectura real: el historial de un
 * usuario, del más reciente al más antiguo. El script de arranque de Mongo
 * ({@code docker/mongo-init/}) ya lo crea junto con el validador de esquema; esta anotación es un
 * respaldo para cuando la app corre contra un Mongo que no pasó por ese script (p. ej. en CI).
 */
@Document(collection = "recomendaciones_log")
@CompoundIndex(name = "idx_usuario_fecha", def = "{'usuarioId': 1, 'fecha': -1}")
public class RecomendacionLog {

    @Id
    private String id;

    private Long usuarioId;

    private Instant fecha;

    private Criterios criterios;

    private List<LugarSugerido> lugaresSugeridos;

    private List<PlanGenerado> planesGenerados;

    public String getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public Criterios getCriterios() {
        return criterios;
    }

    public void setCriterios(Criterios criterios) {
        this.criterios = criterios;
    }

    public List<LugarSugerido> getLugaresSugeridos() {
        return lugaresSugeridos;
    }

    public void setLugaresSugeridos(List<LugarSugerido> lugaresSugeridos) {
        this.lugaresSugeridos = lugaresSugeridos;
    }

    public List<PlanGenerado> getPlanesGenerados() {
        return planesGenerados;
    }

    public void setPlanesGenerados(List<PlanGenerado> planesGenerados) {
        this.planesGenerados = planesGenerados;
    }

    public static class Criterios {
        private Long provinciaId;
        private List<String> categorias;
        private BigDecimal presupuestoMaximo;
        private boolean tieneTransportePropio;

        public Long getProvinciaId() {
            return provinciaId;
        }

        public void setProvinciaId(Long provinciaId) {
            this.provinciaId = provinciaId;
        }

        public List<String> getCategorias() {
            return categorias;
        }

        public void setCategorias(List<String> categorias) {
            this.categorias = categorias;
        }

        public BigDecimal getPresupuestoMaximo() {
            return presupuestoMaximo;
        }

        public void setPresupuestoMaximo(BigDecimal presupuestoMaximo) {
            this.presupuestoMaximo = presupuestoMaximo;
        }

        public boolean isTieneTransportePropio() {
            return tieneTransportePropio;
        }

        public void setTieneTransportePropio(boolean tieneTransportePropio) {
            this.tieneTransportePropio = tieneTransportePropio;
        }
    }

    public static class LugarSugerido {
        private Long lugarId;
        private String nombre;
        private String categoria;
        private BigDecimal costoEstimado;
        private double score;

        public Long getLugarId() {
            return lugarId;
        }

        public void setLugarId(Long lugarId) {
            this.lugarId = lugarId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public BigDecimal getCostoEstimado() {
            return costoEstimado;
        }

        public void setCostoEstimado(BigDecimal costoEstimado) {
            this.costoEstimado = costoEstimado;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public static class PlanGenerado {
        private List<ParadaPlan> paradas;
        private BigDecimal costoTotal;
        private int duracionMinutos;
        private double score;

        public List<ParadaPlan> getParadas() {
            return paradas;
        }

        public void setParadas(List<ParadaPlan> paradas) {
            this.paradas = paradas;
        }

        public BigDecimal getCostoTotal() {
            return costoTotal;
        }

        public void setCostoTotal(BigDecimal costoTotal) {
            this.costoTotal = costoTotal;
        }

        public int getDuracionMinutos() {
            return duracionMinutos;
        }

        public void setDuracionMinutos(int duracionMinutos) {
            this.duracionMinutos = duracionMinutos;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public static class ParadaPlan {
        private Long lugarId;
        private String nombre;
        private int orden;
        private String horaInicio;
        private String horaFin;

        public Long getLugarId() {
            return lugarId;
        }

        public void setLugarId(Long lugarId) {
            this.lugarId = lugarId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getOrden() {
            return orden;
        }

        public void setOrden(int orden) {
            this.orden = orden;
        }

        public String getHoraInicio() {
            return horaInicio;
        }

        public void setHoraInicio(String horaInicio) {
            this.horaInicio = horaInicio;
        }

        public String getHoraFin() {
            return horaFin;
        }

        public void setHoraFin(String horaFin) {
            this.horaFin = horaFin;
        }
    }
}
