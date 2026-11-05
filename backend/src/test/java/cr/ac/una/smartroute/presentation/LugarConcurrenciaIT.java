package cr.ac.una.smartroute.presentation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cr.ac.una.smartroute.AbstractIntegrationTest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Laboratorio 7 — bloqueo optimista: Lugar es el dato del dominio con contención real (varios
 * administradores pueden editar el mismo lugar del catálogo al mismo tiempo, desde
 * {@code /admin/lugares} o desde {@code /api/v1/lugares}; ver la oración de justificación en el
 * README). Esta prueba provoca el conflicto de verdad contra SQL Server real (Testcontainers):
 * dos "ediciones" que parten de la misma versión, la segunda debe perder con 409.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LugarConcurrenciaIT extends AbstractIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":(\\d+)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    private String tokenAdmin() throws Exception {
        String cuerpo = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"kimberley.demo@ejemplo.com\",\"password\":\"Demo1234!\"}"))
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

    private static Long extraerNumero(Pattern patron, String cuerpo) {
        Matcher matcher = patron.matcher(cuerpo);
        if (!matcher.find()) {
            throw new IllegalStateException("No se encontró el patrón " + patron + " en: " + cuerpo);
        }
        return Long.valueOf(matcher.group(1));
    }

    @Test
    void segundaEdicionConLaMismaVersionLeida_pierdeConConflicto409() throws Exception {
        String token = tokenAdmin();

        // 1. Crea un lugar nuevo (aislado de los de la semilla) para no interferir con otras
        // pruebas que corren contra la misma base.
        String cuerpoCreado = mockMvc.perform(post("/api/v1/lugares")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"Lugar para probar concurrencia","provinciaId":1,"categoriaId":1,
                                 "costoEstimado":1000,"nivelAccesibilidad":"ALTA","requiereTransportePropio":false}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = extraerNumero(ID_PATTERN, cuerpoCreado);
        Long versionInicial = extraerNumero(VERSION_PATTERN, cuerpoCreado);

        // 2. Dos administradores "abren" el mismo lugar para editar -- ambos ven versionInicial.
        // El administrador A guarda primero: éxito, la versión en la base avanza.
        mockMvc.perform(put("/api/v1/lugares/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"Editado por A","provinciaId":1,"categoriaId":1,
                                 "costoEstimado":1500,"nivelAccesibilidad":"ALTA","requiereTransportePropio":false,
                                 "version":%d}
                                """
                                        .formatted(versionInicial)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Editado por A"));

        // 3. El administrador B guarda después, pero su formulario todavía trae versionInicial
        // (la que vio al abrirlo, ahora obsoleta) -- debe perder con 409, no pisar el cambio de A.
        mockMvc.perform(put("/api/v1/lugares/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"Editado por B","provinciaId":1,"categoriaId":1,
                                 "costoEstimado":2000,"nivelAccesibilidad":"MEDIA","requiereTransportePropio":false,
                                 "version":%d}
                                """
                                        .formatted(versionInicial)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de concurrencia"))
                .andExpect(jsonPath("$.detail", containsString("modificado por otra persona")));
    }

    @Test
    void actualizarSinVersion_devuelve400() throws Exception {
        String token = tokenAdmin();
        String cuerpoCreado = mockMvc.perform(post("/api/v1/lugares")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"Otro lugar de prueba","provinciaId":1,"categoriaId":1,
                                 "costoEstimado":500,"nivelAccesibilidad":"BAJA","requiereTransportePropio":false}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = extraerNumero(ID_PATTERN, cuerpoCreado);

        mockMvc.perform(put("/api/v1/lugares/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"nombre":"Sin version","provinciaId":1,"categoriaId":1,
                                 "costoEstimado":500,"nivelAccesibilidad":"BAJA","requiereTransportePropio":false}
                                """))
                .andExpect(status().isBadRequest());
    }
}
