package cr.ac.una.smartroute.presentation.dto;

import cr.ac.una.smartroute.business.LoginResultado;

public record LoginResponse(String token, String tipo, Long usuarioId, String nombre, String rol) {

    public static LoginResponse from(LoginResultado resultado) {
        return new LoginResponse(resultado.token(), "Bearer", resultado.usuarioId(), resultado.nombre(), resultado.rol());
    }
}
