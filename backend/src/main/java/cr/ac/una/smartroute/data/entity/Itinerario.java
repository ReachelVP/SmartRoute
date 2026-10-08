package cr.ac.una.smartroute.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itinerario")
public class Itinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "presupuesto_total_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal presupuestoTotalEstimado;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    // LAZY a propósito (ver ItinerarioRepositoryIT, Lab 3): se queda así. ItinerarioConsultaService
    // (Lab 5) inicializa esta colección explícitamente con Hibernate.initialize() dentro de la
    // transacción antes de que el controller la mapee a DTO, en vez de volverla EAGER aquí —
    // EAGER en una colección usa por defecto un SELECT separado (no un JOIN), lo que habría vuelto
    // a introducir el N+1 que ese test verifica que no vuelva a aparecer.
    @OneToMany(mappedBy = "itinerario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<ItinerarioParada> paradas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getPresupuestoTotalEstimado() {
        return presupuestoTotalEstimado;
    }

    public void setPresupuestoTotalEstimado(BigDecimal presupuestoTotalEstimado) {
        this.presupuestoTotalEstimado = presupuestoTotalEstimado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public List<ItinerarioParada> getParadas() {
        return paradas;
    }
}
