package cr.ac.una.smartroute.presentation.admin.dto;

import cr.ac.una.smartroute.data.entity.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Bean mutable para el formulario Thymeleaf (ver {@link LugarFormulario} para el porqué). */
public class CategoriaFormulario {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres.")
    private String descripcion;

    public static CategoriaFormulario desde(Categoria categoria) {
        CategoriaFormulario formulario = new CategoriaFormulario();
        formulario.nombre = categoria.getNombre();
        formulario.descripcion = categoria.getDescripcion();
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
}
