package br.edu.safeplace.backend.adapters.out.persistencia;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;
import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesAcaoUseCase;
import br.edu.safeplace.backend.application.port.in.EmitirAlertasPrazoUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarPlanoDeAcaoUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPlanoNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobre a persistência de planos de ação contra PostgreSQL (Issues #98 e #196; RF07, RNF05, UC07, US07):
 * responsável e situação garantidos pelo banco, alerta de prazo emitido uma vez por ação e prazo e aviso
 * de reatribuição gravado.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Persistência de Planos de Ação no PostgreSQL (Issue #98)")
class PlanoDeAcaoPersistenciaIntegracaoTest {

    private static final String DOMINIO_DE_TESTE = "@safeplace-plano98.local";
    private static final String CPF_COLABORADOR = "90000009801";
    private static final String CPF_GESTOR = "90000009802";
    private static final String CODIGO_AREA = "AR-PLANO-98";

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private PlanoDeAcaoRepository planoRepository;

    @Autowired
    private LogAuditoriaRepository auditoriaRepository;

    @Autowired
    private NotificadorAcao notificadorAcao;

    @Autowired
    private EmitirAlertasPrazoUseCase emitirAlertasPrazo;

    @Autowired
    private ConsultarNotificacoesAcaoUseCase consultarNotificacoesAcao;

    @Autowired
    private GerenciarPlanoDeAcaoUseCase gerenciarPlanoDeAcao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer ocorrenciaId;
    private Integer colaboradorId;
    private Integer gestorId;
    private Integer setorId;

    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @BeforeEach
    void preparar() {
        limparDados();

        colaboradorId = inserirUsuario(CPF_COLABORADOR, "Operador Teste",
                "operador" + DOMINIO_DE_TESTE, null, "COLABORADOR");
        gestorId = inserirUsuario(CPF_GESTOR, "Gestor Segurança Teste",
                "gestor" + DOMINIO_DE_TESTE, "$2a$10$hashDeTeste", "GESTOR_SEGURANCA");

        setorId = jdbcTemplate.queryForObject("""
                INSERT INTO setores (nome, descricao, localizacao, status)
                VALUES ('Usinagem e Prensas', 'Setor de usinagem pesada', 'Bloco B', 'ALTO')
                RETURNING id
                """, Integer.class);

        Integer areaId = jdbcTemplate.queryForObject("""
                INSERT INTO areas_risco (codigo, nome, nivel_perigo, setor_id)
                VALUES (?, 'Prensas hidráulicas', 'ALTO', ?)
                RETURNING id
                """, Integer.class, CODIGO_AREA, setorId);

        // Issue #195: toda ocorrência tem área de risco e colaborador envolvido.
        ocorrenciaId = jdbcTemplate.queryForObject("""
                INSERT INTO ocorrencias (data_ocorrencia, descricao, status_ocorrencia, area_risco_id, colaborador_id)
                VALUES (?, 'Vazamento de fluido hidráulico', 'ABERTA', ?, ?)
                RETURNING id_ocorrencia
                """, Integer.class, LocalDateTime.now().minusDays(1), areaId, colaboradorId);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor" + DOMINIO_DE_TESTE, "senha",
                        List.of(new SimpleGrantedAuthority("ROLE_GESTOR_SEGURANCA"))));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        limparDados();
    }

    private void limparDados() {
        jdbcTemplate.execute("UPDATE ocorrencias SET plano_de_acao_id = NULL");
        jdbcTemplate.execute("UPDATE planos_de_acao SET ocorrencia_id = NULL");
        jdbcTemplate.execute("DELETE FROM acidentes");
        jdbcTemplate.execute("DELETE FROM incidentes");
        jdbcTemplate.execute("DELETE FROM prorrogacoes_prazo");
        jdbcTemplate.execute("DELETE FROM notificacoes_acao");
        jdbcTemplate.execute("DELETE FROM planos_de_acao");
        jdbcTemplate.execute("DELETE FROM notificacoes_ocorrencia");
        jdbcTemplate.execute("DELETE FROM ocorrencias");
        jdbcTemplate.update("DELETE FROM areas_risco WHERE codigo = ?", CODIGO_AREA);
        jdbcTemplate.execute("DELETE FROM setores");
        jdbcTemplate.update("DELETE FROM usuarios WHERE email LIKE ? OR cpf IN (?, ?)",
                "%" + DOMINIO_DE_TESTE, CPF_COLABORADOR, CPF_GESTOR);
    }

    private Integer inserirUsuario(String cpf, String nome, String email, String senha, String perfil) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
                VALUES (?, ?, ?, ?, ?, ?, TRUE)
                RETURNING id
                """, Integer.class, cpf, nome, Date.valueOf(LocalDate.of(1990, 1, 1)), email, senha, perfil);
    }

    private Colaborador colaborador() {
        return new Colaborador(colaboradorId, CPF_COLABORADOR, "Operador Teste",
                LocalDate.of(1990, 1, 1), "operador" + DOMINIO_DE_TESTE, true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Setor setor() {
        return Setor.reconstituir(setorId, "Usinagem e Prensas", "Setor de usinagem pesada", "Bloco B", NivelPerigo.ALTO);
    }

    private GestorDeSeguranca gestor() {
        return new GestorDeSeguranca(gestorId, CPF_GESTOR, "Gestor Segurança Teste",
                LocalDate.of(1990, 1, 1), "gestor" + DOMINIO_DE_TESTE, "hash", true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Critério 1: Plano/ações, responsáveis e vínculo com ocorrência sobrevivem à nova consulta")
    void devePersistirEConsultarPlanoComResponsaveisEAlertas() {
        PlanoDeAcao acao = new PlanoDeAcao(
                null,
                ocorrenciaId,
                "Reparar válvula da prensa hidráulica",
                "Revisar cronograma de inspeções preventivas",
                hoje.plusDays(10),
                "ABERTA",
                colaborador(),
                null,
                true,
                3,
                gestor()
        );

        PlanoDeAcao salva = planoRepository.salvar(acao);
        assertThat(salva.getId()).isNotNull();

        // Nova consulta independente pelo repositório
        Optional<PlanoDeAcao> consultada = planoRepository.buscarPorId(salva.getId());
        assertThat(consultada).isPresent();

        PlanoDeAcao lida = consultada.get();
        assertThat(lida.getIdOcorrencia()).isEqualTo(ocorrenciaId);
        assertThat(lida.getMedidasCorretivas()).isEqualTo("Reparar válvula da prensa hidráulica");
        assertThat(lida.getMedidasPreventivas()).isEqualTo("Revisar cronograma de inspeções preventivas");
        assertThat(lida.getPrazo()).isEqualTo(hoje.plusDays(10));
        assertThat(lida.getStatus()).isEqualTo("ABERTA");
        assertThat(lida.getSituacao()).contains(StatusPlanoDeAcao.ABERTA);
        assertThat(lida.isAlertaAtivo()).isTrue();
        assertThat(lida.getAntecedenciaAlertaDias()).isEqualTo(3);
        assertThat(lida.getResponsavelColaborador()).isNotNull();
        assertThat(lida.getResponsavelColaborador().getId()).isEqualTo(colaboradorId);
        assertThat(lida.getResponsavelSetor()).isNull();

        // Consulta listando por ocorrência
        List<PlanoDeAcao> acoesOcorrencia = planoRepository.listarPorOcorrencia(ocorrenciaId);
        assertThat(acoesOcorrencia).hasSize(1);
        assertThat(acoesOcorrencia.get(0).getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Critério 2: Prazo inválido não grava; reatribuição e prorrogação preservam valores e histórico")
    void deveRejeitarPrazoInvalidoEPreservarHistoricoDeProrrogacao() {
        // Prazo nulo não grava
        PlanoDeAcao planoInvalido = new PlanoDeAcao(
                null, ocorrenciaId, "Medida", null, null, "ABERTA", colaborador(), null, false, null, null
        );
        assertThatThrownBy(() -> planoRepository.salvar(planoInvalido))
                .isInstanceOf(PrazoAcaoInvalidoException.class);

        // Grava ação válida inicialmente
        PlanoDeAcao acao = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Ajuste na linha", null, hoje.plusDays(5),
                "ABERTA", colaborador(), null, true, 2, gestor()
        ));

        // Reatribuição de responsável: de colaborador para setor
        acao.atualizarResponsavel(setor(), hoje);
        PlanoDeAcao reatribuida = planoRepository.salvar(acao);

        Optional<PlanoDeAcao> consultadaAposReatribuicao = planoRepository.buscarPorId(reatribuida.getId());
        assertThat(consultadaAposReatribuicao).isPresent();
        assertThat(consultadaAposReatribuicao.get().getResponsavelSetor()).isNotNull();
        assertThat(consultadaAposReatribuicao.get().getResponsavelSetor().getId()).isEqualTo(setorId);
        assertThat(consultadaAposReatribuicao.get().getResponsavelColaborador()).isNull();

        // Prorrogação com justificativa técnica
        ProrrogacaoPrazo prorrogacao = acao.prorrogarPrazo(hoje.plusDays(15), "Atraso no frete das peças", hoje);
        planoRepository.salvar(acao);
        planoRepository.salvarProrrogacao(prorrogacao);

        // Consulta da ação e do histórico de prorrogações
        Optional<PlanoDeAcao> consultadaProrrogada = planoRepository.buscarPorId(acao.getId());
        assertThat(consultadaProrrogada).isPresent();
        assertThat(consultadaProrrogada.get().getPrazo()).isEqualTo(hoje.plusDays(15));

        List<ProrrogacaoPrazo> historico = planoRepository.listarProrrogacoes(acao.getId());
        assertThat(historico).hasSize(1);
        assertThat(historico.get(0).prazoAnterior()).isEqualTo(hoje.plusDays(5));
        assertThat(historico.get(0).novoPrazo()).isEqualTo(hoje.plusDays(15));
        assertThat(historico.get(0).justificativa()).isEqualTo("Atraso no frete das peças");
    }

    @Test
    @DisplayName("Critério 3: Arquivamento de ocorrência conserva planos; sem exclusão física em cascata")
    void arquivamentoDeOcorrenciaConservaPlanos() {
        PlanoDeAcao acao = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Ação preventiva", null, hoje.plusDays(5),
                "ABERTA", colaborador(), null, false, null, null
        ));

        // Arquivamento da ocorrência (atualiza status para ARQUIVADA sem exclusão física)
        jdbcTemplate.update("UPDATE ocorrencias SET status_ocorrencia = 'ARQUIVADA' WHERE id_ocorrencia = ?", ocorrenciaId);

        // Plano permanece intacto e consultável
        Optional<PlanoDeAcao> planoConsultado = planoRepository.buscarPorId(acao.getId());
        assertThat(planoConsultado).isPresent();
        assertThat(planoConsultado.get().getIdOcorrencia()).isEqualTo(ocorrenciaId);

        List<PlanoDeAcao> acoes = planoRepository.listarPorOcorrencia(ocorrenciaId);
        assertThat(acoes).isNotEmpty();

        // Verifica que FK não permite exclusão física em cascata silenciosa
        assertThatThrownBy(() -> jdbcTemplate.execute("DELETE FROM ocorrencias WHERE id_ocorrencia = " + ocorrenciaId))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Critério 4: Migração V22 evolui tabela já existente sem recriar")
    void migracaoEvoluiTabelaExistente() {
        // Verifica que tabela planos_de_acao possui as colunas adicionadas pela V22
        List<String> colunas = jdbcTemplate.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name = 'planos_de_acao'
                """, String.class);

        assertThat(colunas).contains(
                "id",
                "medidas_corretivas",
                "prazo",
                "status",
                "medidas_preventivas",
                "ocorrencia_id",
                "colaborador_id",
                "setor_id",
                "gestor_id",
                "alerta_ativo",
                "antecedencia_alerta_dias"
        );

        // Verifica tabela de histórico de prorrogações
        List<String> colunasProrrogacoes = jdbcTemplate.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name = 'prorrogacoes_prazo'
                """, String.class);

        assertThat(colunasProrrogacoes).contains(
                "id",
                "plano_de_acao_id",
                "prazo_anterior",
                "novo_prazo",
                "justificativa",
                "criado_em"
        );
    }

    @Test
    @DisplayName("Banco exige exatamente um responsável e situação do vocabulário aprovado (V33, issue #196)")
    void bancoGaranteResponsavelUnicoESituacao() {
        String insercao = """
                INSERT INTO planos_de_acao (ocorrencia_id, medidas_corretivas, prazo, status, colaborador_id, setor_id)
                VALUES (?, 'Medida', ?, ?, ?, ?)
                """;

        assertThatThrownBy(() -> jdbcTemplate.update(insercao, ocorrenciaId, hoje, "ABERTA", null, null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_planos_responsavel");
        assertThatThrownBy(() -> jdbcTemplate.update(insercao, ocorrenciaId, hoje, "ABERTA", colaboradorId, setorId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_planos_responsavel");
        assertThatThrownBy(() -> jdbcTemplate.update(insercao, ocorrenciaId, hoje, "PENDENTE", colaboradorId, null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_planos_status");
        assertThatThrownBy(() -> jdbcTemplate.update(insercao, ocorrenciaId, hoje, null, null, setorId))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(jdbcTemplate.update(insercao, ocorrenciaId, hoje, "ABERTA", null, setorId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Alerta de prazo é emitido uma vez por ação e prazo, e a prorrogação o rearma (issue #196)")
    void alertaDePrazoEmitidoUmaVezPorPrazo() {
        PlanoDeAcao acao = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Trocar mangueira da prensa", null, hoje.plusDays(2),
                "ABERTA", colaborador(), null, true, 3, gestor()
        ));
        PlanoDeAcao semAlerta = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Ação sem alerta", null, hoje.plusDays(1),
                "ABERTA", null, setor(), false, null, gestor()
        ));

        assertThat(planoRepository.listarAbertasComAlertaAtivo()).extracting(PlanoDeAcao::getId)
                .contains(acao.getId())
                .doesNotContain(semAlerta.getId());

        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje.minusDays(2))).isZero();
        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje)).isEqualTo(1);
        // Repetir a verificação, no mesmo dia ou depois, não duplica o alerta do mesmo prazo.
        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje)).isZero();
        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje.plusDays(1))).isZero();

        // A prorrogação cria um novo prazo e rearma o alerta.
        ProrrogacaoPrazo prorrogacao = acao.prorrogarPrazo(hoje.plusDays(10), "Peça em falta no fornecedor", hoje);
        planoRepository.salvar(acao);
        planoRepository.salvarProrrogacao(prorrogacao);
        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje.plusDays(1))).isZero();
        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje.plusDays(7))).isEqualTo(1);

        List<NotificacaoAcao> avisos = consultarNotificacoesAcao.listar().stream()
                .filter(aviso -> acao.getId().equals(aviso.idAcao()))
                .toList();
        assertThat(avisos).extracting(NotificacaoAcao::motivo).containsOnly(MotivoAvisoAcao.PRAZO);
        assertThat(avisos).extracting(NotificacaoAcao::prazo).containsExactly(hoje.plusDays(10), hoje.plusDays(2));
        assertThat(avisos.get(1).responsavelTipo()).isEqualTo("COLABORADOR");
        assertThat(avisos.get(1).responsavelId()).isEqualTo(colaboradorId);
        assertThat(avisos.get(1).responsavelNome()).isEqualTo("Operador Teste");
        assertThat(avisos.get(1).idOcorrencia()).isEqualTo(ocorrenciaId);
        assertThat(avisos.get(1).mensagem()).contains("Ação #" + acao.getId(), "Operador Teste");
    }

    @Test
    @DisplayName("Ação concluída sai da verificação e não recebe alerta (issue #196)")
    void acaoConcluidaNaoRecebeAlerta() {
        PlanoDeAcao acao = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Ação concluída", null, hoje,
                "ABERTA", colaborador(), null, true, 0, gestor()
        ));
        acao.atualizarStatus(StatusPlanoDeAcao.CONCLUIDA);
        planoRepository.salvar(acao);

        assertThat(planoRepository.listarAbertasComAlertaAtivo()).extracting(PlanoDeAcao::getId)
                .doesNotContain(acao.getId());
        assertThat(emitirAlertasPrazo.emitirAlertasDePrazo(hoje.plusDays(5))).isZero();
    }

    @Test
    @DisplayName("Reatribuição grava o aviso ao novo encarregado a cada troca (UC07, alternativo II; issue #196)")
    void reatribuicaoGravaAvisoAoNovoResponsavel() {
        PlanoDeAcao acao = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Instalar guarda-corpo", null, LocalDate.now().plusDays(6),
                "ABERTA", colaborador(), null, false, null, gestor()
        ));

        gerenciarPlanoDeAcao.atualizarResponsavel(acao.getId(), new AtualizarResponsavelInputDTO(null, setorId));
        gerenciarPlanoDeAcao.atualizarResponsavel(acao.getId(), new AtualizarResponsavelInputDTO(colaboradorId, null));

        List<NotificacaoAcao> avisos = consultarNotificacoesAcao.listar().stream()
                .filter(aviso -> acao.getId().equals(aviso.idAcao()))
                .toList();
        assertThat(avisos).hasSize(2);
        assertThat(avisos).extracting(NotificacaoAcao::motivo).containsOnly(MotivoAvisoAcao.REATRIBUICAO);
        // Mais recente primeiro: voltou ao colaborador depois de passar pelo setor.
        assertThat(avisos.get(0).responsavelTipo()).isEqualTo("COLABORADOR");
        assertThat(avisos.get(1).responsavelTipo()).isEqualTo("SETOR");
        assertThat(avisos.get(1).responsavelId()).isEqualTo(setorId);
        assertThat(avisos.get(1).responsavelNome()).isEqualTo("Usinagem e Prensas");
        assertThat(avisos.get(1).dataAviso()).isEqualTo(LocalDate.now());
        assertThat(avisos.get(1).mensagem()).contains("reatribuída a setor Usinagem e Prensas");
    }

    @Test
    @DisplayName("Ação inexistente responde não encontrada (issue #196)")
    void acaoInexistenteNaoEncontrada() {
        assertThatThrownBy(() -> gerenciarPlanoDeAcao.buscarPorId(Integer.MAX_VALUE))
                .isInstanceOf(AcaoPlanoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Critério 5: Transações verificam plano, histórico e auditoria coerentes")
    void transacoesEAuditoriaCoerentes() {
        PlanoDeAcao acao = planoRepository.salvar(new PlanoDeAcao(
                null, ocorrenciaId, "Ação auditada", null, hoje.plusDays(4),
                "ABERTA", colaborador(), null, false, null, gestor()
        ));

        // Conclui a ação
        acao.atualizarStatus(StatusPlanoDeAcao.CONCLUIDA);
        planoRepository.salvar(acao);

        // Prorrogação com justificativa
        ProrrogacaoPrazo prorrogacao = new ProrrogacaoPrazo(acao.getId(), hoje.plusDays(4), hoje.plusDays(12), "Necessidade de peças importadas");
        planoRepository.salvarProrrogacao(prorrogacao);

        List<LogAuditoria> logs = auditoriaRepository.listarPorEntidade("PlanoDeAcao", acao.getId());
        assertThat(logs).isNotEmpty();

        assertThat(logs).anySatisfy(log -> {
            assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
            assertThat(log.getResponsavel()).isEqualTo("gestor" + DOMINIO_DE_TESTE);
        });

        assertThat(logs).anySatisfy(log -> {
            assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
            assertThat(log.getJustificativa()).contains("CONCLUIDA");
        });

        assertThat(logs).anySatisfy(log -> {
            assertThat(log.getTipoOperacao()).isEqualTo("PRORROGAR_PRAZO");
            assertThat(log.getJustificativa()).isEqualTo("Necessidade de peças importadas");
        });
    }
}
