/*
 * Integração com PostgreSQL da consulta de ocorrências por critérios (issue #128): período pela data
 * do fato, setor pela área de risco, colaborador envolvido, combinação e visibilidade do Supervisor,
 * incluindo o caso em que o id de outro cadastro dele coincide com o de uma ocorrência alheia.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Integração: consulta de ocorrências por critérios (#128)")
class OcorrenciaConsultaPersistenciaIntegracaoTest {

    // Marcadores exclusivos desta classe: a limpeza apaga só o que eles identificam.
    private static final String DOMINIO = "@consulta128.test";
    private static final String PREFIXO_AREA = "AR-C128-";
    private static final String PREFIXO_SETOR = "Setor C128 ";
    private static final String GESTOR = "gestora" + DOMINIO;
    private static final String SUPERVISOR = "supervisor" + DOMINIO;

    // Fatos em 2025: outros testes da suíte deixam ocorrências com a data do momento em que rodam, e
    // nenhuma delas pode cair nos períodos consultados aqui.
    private static final LocalDate SETEMBRO_1 = LocalDate.of(2025, 9, 1);
    private static final LocalDate SETEMBRO_30 = LocalDate.of(2025, 9, 30);
    private static final FiltroOcorrenciasDTO ANO_2025 =
            new FiltroOcorrenciasDTO(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null, null);

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private RegistrarOcorrenciaUseCase registrarUseCase;

    @Autowired
    private GerenciarOcorrenciaUseCase gerenciarUseCase;

    @Autowired
    private JdbcTemplate jdbc;

    private Integer setorA;
    private Integer setorB;
    private Integer joao;
    private Integer maria;
    // o1 e o2 foram relatados pelo Supervisor; o3 e o4, pela Gestora.
    private Integer o1;
    private Integer o2;
    private Integer o3;
    private Integer o4;

    @BeforeEach
    void preparar() {
        limpar();
        setorA = inserirSetor(PREFIXO_SETOR + "A", "ALTO");
        setorB = inserirSetor(PREFIXO_SETOR + "B", "BAIXO");
        Integer areaA = inserirArea(PREFIXO_AREA + "A", setorA);
        Integer areaB = inserirArea(PREFIXO_AREA + "B", setorB);
        joao = inserirUsuario("12800000155", "João C128", "joao" + DOMINIO, null, "COLABORADOR");
        maria = inserirUsuario("12800000236", "Maria C128", "maria" + DOMINIO, null, "COLABORADOR");
        inserirUsuario("12800000317", "Gestora C128", GESTOR, "$2a$10$hashDeTeste", "GESTOR_SEGURANCA");
        inserirUsuario("12800000406", "Supervisor C128", SUPERVISOR, "$2a$10$hashDeTeste", "SUPERVISOR");

        autenticar(SUPERVISOR, "ROLE_SUPERVISOR");
        o1 = relatar(LocalDateTime.of(2025, 9, 1, 0, 0), areaA, joao);
        o2 = relatar(LocalDateTime.of(2025, 9, 30, 23, 59), areaB, maria);
        autenticar(GESTOR, "ROLE_GESTOR_SEGURANCA");
        o3 = relatar(LocalDateTime.of(2025, 10, 1, 0, 0), areaA, maria);
        o4 = relatar(LocalDateTime.of(2025, 8, 31, 23, 59), null, joao);
    }

    @AfterEach
    void limparAoFinal() {
        SecurityContextHolder.clearContext();
        limpar();
    }

    @Test
    @DisplayName("Período inclui o primeiro e o último dia inteiros da data do fato")
    void periodoPelaDataDoFato() {
        assertThat(ids(new FiltroOcorrenciasDTO(SETEMBRO_1, SETEMBRO_30, null, null))).containsExactly(o1, o2);
    }

    @Test
    @DisplayName("Setor é resolvido pela área de risco; ocorrência sem área fica de fora")
    void setorPelaArea() {
        assertThat(ids(new FiltroOcorrenciasDTO(null, null, setorA, null))).containsExactly(o1, o3);
        assertThat(ids(new FiltroOcorrenciasDTO(null, null, setorB, null))).containsExactly(o2);
    }

    @Test
    @DisplayName("Colaborador envolvido filtra pelo vínculo gravado")
    void colaboradorEnvolvido() {
        assertThat(ids(new FiltroOcorrenciasDTO(null, null, null, joao))).containsExactly(o1, o4);
    }

    @Test
    @DisplayName("Critérios combinados e combinação sem resultado")
    void criteriosCombinados() {
        assertThat(ids(new FiltroOcorrenciasDTO(SETEMBRO_1, SETEMBRO_30, setorA, joao))).containsExactly(o1);
        assertThat(ids(new FiltroOcorrenciasDTO(SETEMBRO_1, SETEMBRO_30, setorB, joao))).isEmpty();
    }

    @Test
    @DisplayName("Supervisor vê só os próprios relatos, mesmo quando outro cadastro dele tem o id de um relato alheio")
    void supervisorVeSoOsProprios() {
        // Cadastro de colaborador feito pelo Supervisor com o mesmo id da ocorrência o3.
        jdbc.update("INSERT INTO logs_auditoria (tipo_operacao, responsavel, entidade, entidade_id) VALUES (?, ?, ?, ?)",
                "CADASTRO", SUPERVISOR, "Colaborador", o3);
        autenticar(SUPERVISOR, "ROLE_SUPERVISOR");

        assertThat(ids(ANO_2025)).containsExactly(o1, o2);
        assertThat(ids(new FiltroOcorrenciasDTO(null, null, setorA, null))).containsExactly(o1);
        assertThat(gerenciarUseCase.buscarPorId(o1).idOcorrencia()).isEqualTo(o1);
        assertThatThrownBy(() -> gerenciarUseCase.buscarPorId(o3)).isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    private List<Integer> ids(FiltroOcorrenciasDTO filtro) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(GESTOR, "ROLE_GESTOR_SEGURANCA");
        }
        return registrarUseCase.listar(filtro).stream()
                .map(OcorrenciaOutputDTO::idOcorrencia)
                .filter(List.of(o1, o2, o3, o4)::contains)
                .toList();
    }

    private Integer relatar(LocalDateTime fato, Integer areaId, Integer colaboradorId) {
        return registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                fato, null, "Relato C128", null, "Piso escorregadio", "Queda", areaId, colaboradorId, null))
                .idOcorrencia();
    }

    private static void autenticar(String email, String papel) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                email, "senha", List.of(new SimpleGrantedAuthority(papel))));
    }

    private Integer inserirSetor(String nome, String status) {
        return jdbc.queryForObject("INSERT INTO setores (nome, status) VALUES (?, ?) RETURNING id",
                Integer.class, nome, status);
    }

    private Integer inserirArea(String codigo, Integer setorId) {
        return jdbc.queryForObject("""
                INSERT INTO areas_risco (codigo, nome, nivel_perigo, setor_id)
                VALUES (?, ?, 'ALTO', ?)
                RETURNING id
                """, Integer.class, codigo, "Área " + codigo, setorId);
    }

    private Integer inserirUsuario(String cpf, String nome, String email, String senha, String perfil) {
        return jdbc.queryForObject("""
                INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
                VALUES (?, ?, ?, ?, ?, ?, TRUE)
                RETURNING id
                """, Integer.class, cpf, nome, Date.valueOf(LocalDate.of(1990, 1, 1)), email, senha, perfil);
    }

    private void limpar() {
        String minhasOcorrencias = "SELECT o.id_ocorrencia FROM ocorrencias o JOIN usuarios u ON u.id = o.colaborador_id"
                + " WHERE u.email LIKE '%" + DOMINIO + "'";
        jdbc.update("DELETE FROM logs_auditoria WHERE responsavel LIKE ? OR (entidade IN ('Acidente', 'Incidente')"
                + " AND entidade_id IN (" + minhasOcorrencias + "))", "%" + DOMINIO);
        jdbc.update("DELETE FROM incidentes WHERE id_ocorrencia IN (" + minhasOcorrencias + ")");
        jdbc.update("DELETE FROM ocorrencias WHERE id_ocorrencia IN (" + minhasOcorrencias + ")");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE ?", "%" + DOMINIO);
        jdbc.update("DELETE FROM areas_risco WHERE codigo LIKE ?", PREFIXO_AREA + "%");
        jdbc.update("DELETE FROM setores WHERE nome LIKE ?", PREFIXO_SETOR + "%");
    }
}
