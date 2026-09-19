package br.edu.safeplace.backend.config;

import br.edu.safeplace.backend.adapters.out.seguranca.JwtFiltroAutenticacao;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final br.edu.safeplace.backend.application.port.out.TokenPorta tokenPorta;
    private final RestSegurancaTratadorExcecoes restSegurancaTratadorExcecoes;

    public SecurityConfig(
            @Autowired(required = false) br.edu.safeplace.backend.application.port.out.TokenPorta tokenPorta,
            @Autowired(required = false) RestSegurancaTratadorExcecoes restSegurancaTratadorExcecoes) {
        this.tokenPorta = tokenPorta;
        this.restSegurancaTratadorExcecoes = restSegurancaTratadorExcecoes;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        // Rotas públicas (RNF03)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health",
                                "/actuator/health",
                                "/docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Rotas protegidas por RBAC (RNF03)
                        // RF23: so o Gestor de Seguranca provisiona supervisores; o Supervisor
                        // cadastra colaboradores.
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/supervisores")
                                .hasRole("GESTOR_SEGURANCA")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/colaboradores")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        // Issue #122: a atualizacao cadastral segue a mesma matriz do cadastro.
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/supervisores/*")
                                .hasRole("GESTOR_SEGURANCA")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/colaboradores/*")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/usuarios/**").hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/epis/**").hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/ocorrencias/**").hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        // UC03 e UC11: o Gestor de Seguranca mantem o cadastro; o Supervisor e ator
                        // secundario e apenas consulta as diretrizes.
                        .requestMatchers(HttpMethod.GET, "/api/areas-risco/**", "/api/tarefas/**")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/areas-risco/**", "/api/tarefas/**").hasRole("GESTOR_SEGURANCA")
                        .anyRequest().authenticated()
                );

        if (restSegurancaTratadorExcecoes != null) {
            http.exceptionHandling(ex -> ex
                    .authenticationEntryPoint(restSegurancaTratadorExcecoes)
                    .accessDeniedHandler(restSegurancaTratadorExcecoes)
            );
        }

        if (tokenPorta != null) {
            http.addFilterBefore(new JwtFiltroAutenticacao(tokenPorta), UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
