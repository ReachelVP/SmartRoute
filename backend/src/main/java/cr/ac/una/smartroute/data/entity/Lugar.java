package cr.ac.una.smartroute.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lugar")
public class Lugar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String descripcion;

    // EAGER a propósito: el nombre de la provincia y la categoría son parte de cómo se muestra un
    // Lugar en casi cualquier respuesta (LugarResponse, RecomendacionResponse); con LAZY y
    // spring.jpa.open-in-view=false, el proxy ya no tiene sesión abierta para resolverse al mapear
    // la entidad a DTO en la capa de presentación.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "costo_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "nivel_accesibilidad", nullable = false, length = 20)
    private String nivelAccesibilidad;

    @Column(name = "requiere_transporte_propio", nullable = false)
    private boolean requiereTransportePropio;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public Long getId() {
        return id;
    }

    /** Solo para construir instancias en pruebas unitarias; JPA asigna el id real al persistir. */
    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getCostoEstimado() {
        return costoEstimado;
    }

    public void setCostoEstimado(BigDecimal costoEstimado) {
        this.costoEstimado = costoEstimado;
    }

    public String getNivelAccesibilidad() {
        return nivelAccesibilidad;
    }

    public void setNivelAccesibilidad(String nivelAccesibilidad) {
        this.nivelAccesibilidad = nivelAccesibilidad;
    }

    public boolean isRequiereTransportePropio() {
        return requiereTransportePropio;
    }

    public void setRequiereTransportePropio(boolean requiereTransportePropio) {
        this.requiereTransportePropio = requiereTransportePropio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
