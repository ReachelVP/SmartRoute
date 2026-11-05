package cr.ac.una.smartroute.presentation.admin.dto;

import cr.ac.una.smartroute.data.entity.Lugar;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * Bean mutable (no un {@code record}) a propósito: Thymeleaf ({@code th:object}/{@code th:field})
 * necesita setters para poder re-poblar el formulario con los valores enviados cuando la
 * validación falla, y para precargarlo al editar. Los DTO de la API v1 ({@code LugarCreateRequest})
 * son records inmutables por diseño — este es un formulario, no un contrato HTTP, así que tiene su
 * propia clase en vez de reusar esa.
 */
public class LugarFormulario {

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Seleccioná una provincia.")
    private Long provinciaId;

    @NotNull(message = "Seleccioná una categoría.")
    private Long categoriaId;

    @NotNull(message = "El costo estimado es obligatorio.")
    @DecimalMin(value = "0.0", message = "El costo estimado no puede ser negativo.")
    private BigDecimal costoEstimado;

    @NotNull(message = "Seleccioná un nivel de accesibilidad.")
    @Pattern(regexp = "ALTA|MEDIA|BAJA", message = "El nivel de accesibilidad debe ser ALTA, MEDIA o BAJA.")
    private String nivelAccesibilidad;

    private boolean requiereTransportePropio;

    // Bloqueo optimista (Laboratorio 7): la versión que este formulario traía cuando se abrió,
    // viaja oculta en el POST y se usa para detectar si alguien más editó el lugar mientras tanto
    // (ver LugarAdminController y LugarAdminService.actualizar). Null en el formulario de "nuevo",
    // donde no aplica.
    private Long version;

    public static LugarFormulario desde(Lugar lugar) {
        LugarFormulario formulario = new LugarFormulario();
        formulario.nombre = lugar.getNombre();
        formulario.descripcion = lugar.getDescripcion();
        formulario.provinciaId = lugar.getProvincia().getId();
        formulario.categoriaId = lugar.getCategoria().getId();
        formulario.costoEstimado = lugar.getCostoEstimado();
        formulario.nivelAccesibilidad = lugar.getNivelAccesibilidad();
        formulario.requiereTransportePropio = lugar.isRequiereTransportePropio();
        formulario.version = lugar.getVersion();
        return formulario;
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

    public Long getProvinciaId() {
        return provinciaId;
    }

    public void setProvinciaId(Long provinciaId) {
        this.provinciaId = provinciaId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
