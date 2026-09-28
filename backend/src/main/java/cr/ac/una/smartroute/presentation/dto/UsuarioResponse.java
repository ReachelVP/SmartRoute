package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.data.entity.Usuario;

/** Nunca se expone {@code passwordHash} ni {@code correo} fuera del servicio. */
public record UsuarioResponse(Long id, String nombre) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre());
    }
}
