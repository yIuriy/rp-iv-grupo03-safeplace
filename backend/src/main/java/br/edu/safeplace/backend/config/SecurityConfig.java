/*
 * Configuração do Spring Security: JWT sem sessão de servidor, rotas públicas (RNF03) e a matriz
 * de papéis por rota. RF23 e issue #122: só o Gestor cria e atualiza supervisores; Supervisor e
 * Gestor criam e atualizam colaboradores.
 */

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
                        // RF23: so o Gestor de Seguranca provisiona supervisores; Supervisor e Gestor de
                        // Seguranca cadastram colaboradores.
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
                        // UC01, Gestão de Estoque e Manutenção (#132, #103): mutações exclusivas do Gestor de Segurança
                        .requestMatchers(HttpMethod.POST, "/api/epis", "/api/epis/*/movimentacoes", "/api/epis/*/manutencoes")
                                .hasRole("GESTOR_SEGURANCA")
                        // UC06: consulta de EPIs e movimentações permitida para Gestor e Supervisor
                        // UC05, UC07, UC09: Gestão de Ocorrências e Planos de Ação (#99)
                        .requestMatchers(HttpMethod.PATCH, "/api/ocorrencias/*/triagem", "/api/ocorrencias/*/arquivar", "/api/ocorrencias/*/consolidar-cat")
                                .hasRole("GESTOR_SEGURANCA")
                        .requestMatchers(HttpMethod.POST, "/api/ocorrencias/*/planos-acao")
                                .hasRole("GESTOR_SEGURANCA")
                        .requestMatchers(HttpMethod.GET, "/api/ocorrencias/**", "/api/planos-acao/**")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/ocorrencias")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/planos-acao/**")
                                .hasRole("GESTOR_SEGURANCA")
                        .requestMatchers("/api/ocorrencias/**")
                                .hasRole("GESTOR_SEGURANCA")
                        // UC12 (#106): Supervisor é o ator principal e o Gestor o secundário do empréstimo
                        .requestMatchers("/api/emprestimos/**").hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        // Issue #114: o historico de alteracoes de areas e tarefas e consulta do Gestor de
                        // Seguranca; precisa vir antes da regra geral de leitura abaixo.
                        .requestMatchers(HttpMethod.GET, "/api/areas-risco/*/historico", "/api/tarefas/*/historico")
                                .hasRole("GESTOR_SEGURANCA")
                        // Issue #134 (RF09, US09): o Gestor de Seguranca registra e consulta inspecoes; a
                        // consulta pelo Supervisor aguarda decisao do grupo.
                        .requestMatchers("/api/areas-risco/*/inspecoes").hasRole("GESTOR_SEGURANCA")
                        // UC03 e UC11: o Gestor de Seguranca mantem o cadastro; o Supervisor e ator
                        // secundario e apenas consulta as diretrizes.
                        .requestMatchers(HttpMethod.GET, "/api/areas-risco/**", "/api/tarefas/**", "/api/setores/**")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        // UC10 (Issue #108, RF21): Planejar Substituição Inteligente de EPI
                        .requestMatchers(HttpMethod.POST, "/api/projecoes/**").hasRole("GESTOR_SEGURANCA")
                        .requestMatchers(HttpMethod.GET, "/api/projecoes/**").hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/areas-risco/**", "/api/tarefas/**", "/api/setores/**").hasRole("GESTOR_SEGURANCA")
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
