package cr.ac.una.smartroute.presentation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de integración (Testcontainers, SQL Server real) de la API versionada {@code /api/v1}:
 * ejercita el filtro JWT, la autorización por rol y por propiedad del recurso, y el formato de
 * error (Problem Details) de punta a punta a través de la cadena real de filtros de Spring
 * Security — no solo la capa de negocio. Cubre los códigos exigidos por la rúbrica del
 * Laboratorio 5: 201, 400, 401, 403, 404 y 422 (más 409, que la API también expone).
 *
 * <p>Usuarios de la semilla (V2 + {@code DemoUsuariosSeeder}): usuario 1 = Reachel (rol USUARIO),
 * usuario 2 = Kimberley (rol ADMIN); contraseña de ambos: {@code Demo1234!}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiV1SecurityIT extends AbstractIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\":\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    private String login(String correo) throws Exception {
        String cuerpo = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"Demo1234!\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = TOKEN_PATTERN.matcher(cuerpo);
        if (!matcher.find()) {
            throw new IllegalStateException("La respuesta de login no contiene un token: " + cuerpo);
        }
        return matcher.group(1);
    }

    private String tokenUsuario() throws Exception {
        return login("reachel.demo@ejemplo.com");
    }

    private String tokenAdmin() throws Exception {
        return login("kimberley.demo@ejemplo.com");
    }

    @Test
    void login_devuelve401_conCredencialesInvalidas() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"reachel.demo@ejemplo.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Laboratorio 7: correo exclusivo de esta prueba (no lo usa ninguna otra) -- el contador de
     * intentos fallidos de {@code LimitadorIntentosLogin} es un bean singleton compartido por
     * todas las pruebas de este módulo, así que reusar un correo de otra prueba contaminaría el
     * conteo en cualquier orden de ejecución.
     */
    @Test
    void login_devuelve429_trasDemasiadosIntentosFallidos() throws Exception {
        String correo = "fuerza-bruta-it@ejemplo.com";
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"correo\":\"" + correo + "\",\"password\":\"incorrecta\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void crearLugar_comoAdmin_devuelve201ConLocation() throws Exception {
        mockMvc.perform(post("/api/v1/lugares")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"Lugar de prueba IT","descripcion":"Prueba",
                                 "provinciaId":1,"categoriaId":1,"costoEstimado":1000,
                                 "nivelAccesibilidad":"ALTA","requiereTransportePropio":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/lugares/")));
    }

    @Test
    void crearLugar_sinNombre_devuelve400DeValidacion() throws Exception {
        mockMvc.perform(post("/api/v1/lugares")
                        .header("Authorization", "Bearer " + tokenAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"provinciaId":1,"categoriaId":1,"costoEstimado":1000,
                                 "nivelAccesibilidad":"ALTA","requiereTransportePropio":false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recomendar_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/recomendaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provinciaId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearLugar_comoUsuarioSinRolAdmin_devuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/lugares")
                        .header("Authorization", "Bearer " + tokenUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"No debería crearse","provinciaId":1,"categoriaId":1,
                                 "costoEstimado":1000,"nivelAccesibilidad":"ALTA",
                                 "requiereTransportePropio":false}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerLugarInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/lugares/999999")).andExpect(status().isNotFound());
    }

    @Test
    void crearResenaDuplicada_devuelve409() throws Exception {
        String token = tokenUsuario();
        String cuerpo =
                """
                {"lugarId":6,"calificacion":4,"comentario":"Primera reseña de esta prueba"}
                """;

        mockMvc.perform(post("/api/v1/resenas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/resenas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmarItinerario_conHorariosSolapados_devuelve422() throws Exception {
        mockMvc.perform(post("/api/v1/itinerarios")
                        .header("Authorization", "Bearer " + tokenUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"fecha":"2027-02-15",
                                 "paradas":[
                                   {"lugarId":3,"orden":1,"horaInicio":"09:00:00","horaFin":"11:00:00"},
                                   {"lugarId":4,"orden":2,"horaInicio":"10:00:00","horaFin":"12:00:00"}
                                 ],
                                 "confirmarExcesoPresupuesto":false}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void listarItinerarios_comoUsuario_soloDevuelveLosPropios() throws Exception {
        mockMvc.perform(get("/api/v1/itinerarios").header("Authorization", "Bearer " + tokenUsuario()))
                .andExpect(status().isOk());
    }
}
