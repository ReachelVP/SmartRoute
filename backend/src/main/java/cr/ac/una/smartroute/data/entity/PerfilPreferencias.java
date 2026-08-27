package cr.ac.una.smartroute.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "perfil_preferencias")
public class PerfilPreferencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "presupuesto_maximo", nullable = false, precision = 10, scale = 2)
    private BigDecimal presupuestoMaximo;

    @Column(name = "tiene_transporte_propio", nullable = false)
    private boolean tieneTransportePropio;

    @Column(name = "radio_distancia_km")
    private Integer radioDistanciaKm;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "perfil_preferencias_categoria",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categoriasInteres = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    public Integer getRadioDistanciaKm() {
        return radioDistanciaKm;
    }

    public void setRadioDistanciaKm(Integer radioDistanciaKm) {
        this.radioDistanciaKm = radioDistanciaKm;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public Set<Categoria> getCategoriasInteres() {
        return categoriasInteres;
    }
}
