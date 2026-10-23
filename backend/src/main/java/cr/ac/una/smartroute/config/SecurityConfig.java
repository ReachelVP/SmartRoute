package cr.ac.una.smartroute.config;

import cr.ac.una.smartroute.config.security.JwtAuthenticationFilter;
import cr.ac.una.smartroute.config.security.ProblemDetailAccessDeniedHandler;
import cr.ac.una.smartroute.config.security.ProblemDetailAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Dos cadenas de filtros independientes, cada una con el mecanismo de autenticación que le
 * corresponde — nunca se mezclan:
 *
 * <ul>
 *   <li><b>{@code adminSecurityFilterChain}</b> ({@code @Order(1)}, más específica): el módulo
 *       MVC/Thymeleaf bajo {@code /admin/**} (Laboratorio 6). Sesión + formLogin + CSRF
 *       <b>activo</b> (el valor por defecto de Spring Security; no se desactiva), porque es
 *       exactamente el escenario — formularios HTML con cookie de sesión — para el que CSRF
 *       protege.
 *   <li><b>{@code apiSecurityFilterChain}</b> ({@code @Order(2)}, catch-all): la API REST
 *       (heredada en {@code /api/**} y versionada en {@code /api/v1/**}, Laboratorio 5).
 *       Stateless, JWT en el header {@code Authorization}, CSRF desactivado a propósito: sin
 *       cookie de sesión de por medio, un JWT en un header que un navegador no adjunta
 *       automáticamente entre orígenes no es vulnerable a CSRF.
 * </ul>
 *
 * Con varias {@code SecurityFilterChain}, Spring Security evalúa los {@code securityMatcher} en
 * orden hasta encontrar el primero que haga match — por eso la de {@code /admin/**} necesita
 * {@code @Order(1)}: sin eso, el {@code securityMatcher("/**")} implícito de la cadena de la API
 * (que no declara uno propio) la capturaría primero.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/login", "/logout")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/admin/lugares", true).permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                // Registra el CorsConfigurationSource de CorsConfig en el propio chain de Spring
                // Security: así el preflight OPTIONS se resuelve ANTES de exigir autenticación (ver
                // el javadoc de CorsConfig para el porqué de esto, no de WebMvcConfigurer).
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // El CorsFilter de .cors(...) agrega las cabeceras, pero la decisión de
                        // autorización sigue pasando por acá -- sin este permitAll explícito, un
                        // preflight (que nunca manda el header Authorization real) se rechazaría
                        // igual antes de que el navegador llegue a intentar la petición de verdad.
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        // Cuando el chain de /admin (CSRF) rechaza una petición, Spring reenvía
                        // internamente a /error para renderizar la página de error -- ese reenvío
                        // vuelve a pasar por los filtros de seguridad y, como /error no hace match
                        // con ningún securityMatcher del chain de /admin, cae en este catch-all. Sin
                        // permitAll acá, el 403 original de CSRF se pisa con un 401 JSON de la API.
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lugares/**", "/api/v1/resenas/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/lugares")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/lugares/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/lugares/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/v1/**")
                        .authenticated()
                        .requestMatchers("/api/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
