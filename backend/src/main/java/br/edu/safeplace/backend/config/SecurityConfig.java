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

    private final br.edu.safeplace.backend.application.port.out.GerenciadorToken gerenciadorToken;
    private final RestSegurancaExceptionHandler restSegurancaExceptionHandler;

    public SecurityConfig(
            @Autowired(required = false) br.edu.safeplace.backend.application.port.out.GerenciadorToken gerenciadorToken,
            @Autowired(required = false) RestSegurancaExceptionHandler restSegurancaExceptionHandler) {
        this.gerenciadorToken = gerenciadorToken;
        this.restSegurancaExceptionHandler = restSegurancaExceptionHandler;
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
                        // RF23: Consulta de colaboradores e perfil próprio permitida a Gestor e Supervisor
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/colaboradores")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me", "/api/usuarios/me/**")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        // RF23: Listagem geral de usuários (supervisores e gestores) exclusiva do Gestor de Segurança
                        .requestMatchers(HttpMethod.GET, "/api/usuarios")
                                .hasRole("GESTOR_SEGURANCA")
                        // Consulta individual por ID: validada granularmente no caso de uso para Supervisor
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/*")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/usuarios/**").denyAll()
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
                        // UC09, passo 12 (#195) e UC07 (#196): notificações de novos relatos e avisos das ações são do Gestor
                        .requestMatchers("/api/notificacoes", "/api/notificacoes/**")
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
                        // UC10 (Issue #108, RF21): Planejar Substituição Inteligente de EPI exclusivo do Gestor de Segurança
                        .requestMatchers("/api/projecoes/**").hasRole("GESTOR_SEGURANCA")
                        // RF10, UC02 e UC11 (#117): Alocação operacional de tarefas permitida para Gestor e Supervisor
                        .requestMatchers(HttpMethod.POST, "/api/tarefas/*/alocacoes")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        // RF12, UC08 / US12 (#135): Vinculação de EPIs a tarefas exclusiva do Gestor de Segurança
                        .requestMatchers(HttpMethod.PUT, "/api/tarefas/*/epis").hasRole("GESTOR_SEGURANCA")
                        .requestMatchers("/api/areas-risco/**", "/api/tarefas/**", "/api/setores/**").hasRole("GESTOR_SEGURANCA")
                        // RF10, UC02 e US10 (#119): Gestor e Supervisor consultam capacitações. O cadastro
                        // (#192) é exclusivo do Gestor de Segurança; outros métodos permanecem negados.
                        .requestMatchers(HttpMethod.GET, "/api/capacitacoes/**")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/api/capacitacoes/**").hasRole("GESTOR_SEGURANCA")
                        .requestMatchers("/api/capacitacoes/**").denyAll()
                        // RF18 (#299): Gestor e Supervisor emitem relatórios estatísticos; só leitura.
                        .requestMatchers(HttpMethod.GET, "/api/relatorios/**")
                                .hasAnyRole("GESTOR_SEGURANCA", "SUPERVISOR")
                        .requestMatchers("/api/relatorios/**").denyAll()
                        .anyRequest().authenticated()
                );

        if (restSegurancaExceptionHandler != null) {
            http.exceptionHandling(ex -> ex
                    .authenticationEntryPoint(restSegurancaExceptionHandler)
                    .accessDeniedHandler(restSegurancaExceptionHandler)
            );
        }

        if (gerenciadorToken != null) {
            http.addFilterBefore(new JwtFiltroAutenticacao(gerenciadorToken), UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
