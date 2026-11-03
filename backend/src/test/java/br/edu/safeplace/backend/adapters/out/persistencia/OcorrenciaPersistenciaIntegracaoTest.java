package br.edu.safeplace.backend.adapters.out.persistencia;

import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Cobre, contra um PostgreSQL real, a persistência de ocorrências exigida pelas issues #96 e #195: cada
 * operação roda em sua própria transação de caso de uso, de modo que a leitura seguinte reconstrói
 * a ocorrência a partir do banco, e não de um contexto de persistência ainda aberto.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Testes de Integração de Persistência de Ocorrências")
class OcorrenciaPersistenciaIntegracaoTest {

    private static final String DOMINIO_DE_TESTE = "@safeplace-teste.local";
    private static final String CODIGO_AREA = "AR-OCO-96";
    // CPFs exclusivos desta classe: a suíte compartilha o banco e usuarios.cpf é único.
    private static final String CPF_COLABORADOR = "90000009601";
    private static final String CPF_GESTOR = "90000009602";
    private static final String CPF_SUPERVISOR = "90000009603";

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
    private ConsultarNotificacoesOcorrenciaUseCase consultarNotificacoesUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer areaId;
    private Integer colaboradorId;
    private Integer gestorId;
    private Integer supervisorId;

    @BeforeEach
    void preparar() {
        limparDados();

        areaId = jdbcTemplate.queryForObject("""
                INSERT INTO areas_risco (codigo, nome, descricao, nivel_perigo)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """, Integer.class, CODIGO_AREA, "Linha de Prensas", "Prensas hidráulicas", "ALTO");

        colaboradorId = inserirUsuario(CPF_COLABORADOR, "João da Linha",
                "joao" + DOMINIO_DE_TESTE, null, "COLABORADOR");
        gestorId = inserirUsuario(CPF_GESTOR, "Maria Gestora",
                "maria" + DOMINIO_DE_TESTE, "$2a$10$hashDeTeste", "GESTOR_SEGURANCA");
        supervisorId = inserirUsuario(CPF_SUPERVISOR, "Sérgio Supervisor",
                "sergio" + DOMINIO_DE_TESTE, "$2a$10$hashDeTeste", "SUPERVISOR");
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        limparDados();
    }

    private void limparDados() {
        jdbcTemplate.execute("DELETE FROM notificacoes_ocorrencia");
        jdbcTemplate.execute("UPDATE ocorrencias SET plano_de_acao_id = NULL");
        jdbcTemplate.execute("UPDATE planos_de_acao SET ocorrencia_id = NULL");
        jdbcTemplate.execute("DELETE FROM acidentes");
        jdbcTemplate.execute("DELETE FROM incidentes");
        jdbcTemplate.execute("DELETE FROM ocorrencias");
        jdbcTemplate.execute("DELETE FROM prorrogacoes_prazo");
        jdbcTemplate.execute("DELETE FROM notificacoes_acao");
        jdbcTemplate.execute("DELETE FROM planos_de_acao");
        jdbcTemplate.execute("DELETE FROM sequencias_protocolo_cat");
        jdbcTemplate.update("DELETE FROM usuarios WHERE email LIKE ? OR cpf IN (?, ?, ?)",
                "%" + DOMINIO_DE_TESTE, CPF_COLABORADOR, CPF_GESTOR, CPF_SUPERVISOR);
        jdbcTemplate.update("DELETE FROM areas_risco WHERE codigo = ?", CODIGO_AREA);
    }

    private Integer inserirUsuario(String cpf, String nome, String email, String senha, String perfil) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
                VALUES (?, ?, ?, ?, ?, ?, TRUE)
                RETURNING id
                """, Integer.class, cpf, nome, Date.valueOf(LocalDate.of(1990, 1, 1)), email, senha, perfil);
    }

    @Test
    @DisplayName("Deve reconstruir acidente preservando dataRegistro, status e vínculos após encerrar o contexto de persistência")
    void devePreservarDataRegistroStatusEVinculos() {
        LocalDateTime dataFato = LocalDateTime.of(2026, 9, 10, 14, 37, 12);
        autenticarComo("maria" + DOMINIO_DE_TESTE, "GESTOR_SEGURANCA");

        OcorrenciaOutputDTO criado = registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                dataFato, "Prensa atingiu a mão do operador.", "FALHA_EPI", "CRITICO",
                "Esmagamento", "Lesão na mão direita", "Hospital Municipal", areaId, colaboradorId));

        LocalDateTime depoisDoRegistro = LocalDateTime.now();

        OcorrenciaOutputDTO lido = gerenciarUseCase.buscarPorId(criado.idOcorrencia());

        // A consulta não reinicia o status nem carimba um novo horário de registro.
        assertThat(lido.statusOcorrencia()).isEqualTo("ABERTA");
        assertThat(lido.dataRegistro()).isCloseTo(criado.dataRegistro(), within(1, ChronoUnit.MILLIS));
        assertThat(lido.dataRegistro()).isBeforeOrEqualTo(depoisDoRegistro);

        // A hora do fato sobrevive à ida e volta: nada é reduzido a data.
        assertThat(lido.dataOcorrencia()).isEqualTo(dataFato);

        // Vínculos aprovados, reconstruídos a partir das chaves estrangeiras.
        assertThat(lido.areaId()).isEqualTo(areaId);
        assertThat(lido.areaNome()).isEqualTo("Linha de Prensas");
        assertThat(lido.colaboradorId()).isEqualTo(colaboradorId);
        assertThat(lido.colaboradorNome()).isEqualTo("João da Linha");
        assertThat(lido.relatorId()).isEqualTo(gestorId);
        assertThat(lido.relatorNome()).isEqualTo("Maria Gestora");
        assertThat(lido.relatorPerfil()).isEqualTo("GESTOR_SEGURANCA");

        // Atributos próprios de Acidente.
        assertThat(lido.tipoOcorrencia()).isEqualTo("ACIDENTE");
        assertThat(lido.causaRaiz()).isEqualTo("FALHA_EPI");
        assertThat(lido.gravidade()).isEqualTo("CRITICO");
        assertThat(lido.destino()).isEqualTo("Hospital Municipal");

        // Uma segunda leitura devolve exatamente o mesmo instante de registro.
        assertThat(gerenciarUseCase.buscarPorId(criado.idOcorrencia()).dataRegistro())
                .isEqualTo(lido.dataRegistro());
    }

    @Test
    @DisplayName("Deve manter data_ocorrencia como TIMESTAMP, sem reduzir a data do fato a DATE")
    void deveManterDataOcorrenciaComoTimestamp() {
        String tipoDataOcorrencia = jdbcTemplate.queryForObject("""
                SELECT data_type FROM information_schema.columns
                WHERE table_name = 'ocorrencias' AND column_name = 'data_ocorrencia'
                """, String.class);

        assertThat(tipoDataOcorrencia).isEqualTo("timestamp without time zone");
    }

    @Test
    @DisplayName("Deve reconstruir incidente com área e colaborador, sem emitir protocolo")
    void deveReconstruirIncidente() {
        OcorrenciaOutputDTO criado = registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 9, 8, 15), "Piso oleoso próximo à prensa.",
                "Piso escorregadio", "Queda de mesmo nível", areaId, colaboradorId));

        OcorrenciaOutputDTO lido = gerenciarUseCase.buscarPorId(criado.idOcorrencia());

        assertThat(lido.tipoOcorrencia()).isEqualTo("INCIDENTE");
        assertThat(lido.situacaoRisco()).isEqualTo("Piso escorregadio");
        assertThat(lido.potencialDano()).isEqualTo("Queda de mesmo nível");
        assertThat(lido.numeroProtocoloCAT()).isNull();
        assertThat(lido.areaId()).isEqualTo(areaId);
        assertThat(lido.colaboradorId()).isEqualTo(colaboradorId);
        assertThat(lido.statusOcorrencia()).isEqualTo("ABERTA");
    }

    @Test
    @DisplayName("Deve preservar dataRegistro e a própria linha ao triar e arquivar, sem exclusão física")
    void devePreservarRegistroAoTriarEArquivar() {
        OcorrenciaOutputDTO criado = registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 8, 11, 5), "Queda de ferramenta da passarela.",
                "FATOR_HUMANO", "ALTO", "Impacto", "Contusão no ombro", "Ambulatório", areaId, colaboradorId));

        OcorrenciaOutputDTO emTriagem = gerenciarUseCase.enviarParaTriagem(criado.idOcorrencia());
        assertThat(emTriagem.statusOcorrencia()).isEqualTo("EM_TRIAGEM");
        assertThat(gerenciarUseCase.buscarPorId(criado.idOcorrencia()).statusOcorrencia())
                .isEqualTo("EM_TRIAGEM");

        OcorrenciaOutputDTO arquivada = gerenciarUseCase.arquivar(criado.idOcorrencia());
        assertThat(arquivada.statusOcorrencia()).isEqualTo("ARQUIVADA");

        OcorrenciaOutputDTO lida = gerenciarUseCase.buscarPorId(criado.idOcorrencia());
        assertThat(lida.statusOcorrencia()).isEqualTo("ARQUIVADA");
        assertThat(lida.dataRegistro()).isCloseTo(criado.dataRegistro(), within(1, ChronoUnit.MILLIS));
        assertThat(lida.dataOcorrencia()).isEqualTo(criado.dataOcorrencia());
        assertThat(lida.areaId()).isEqualTo(areaId);
        assertThat(lida.colaboradorId()).isEqualTo(colaboradorId);

        // Arquivar é transição de estado, não remoção: as linhas continuam no banco.
        assertThat(contar("SELECT COUNT(*) FROM ocorrencias WHERE id_ocorrencia = ?", criado.idOcorrencia()))
                .isEqualTo(1);
        assertThat(contar("SELECT COUNT(*) FROM acidentes WHERE id_ocorrencia = ?", criado.idOcorrencia()))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Porta de ocorrências não expõe nenhuma operação de exclusão física")
    void portaNaoExpoeExclusaoFisica() {
        List<String> operacoesDeExclusao = new ArrayList<>();
        for (Method metodo : OcorrenciaRepository.class.getMethods()) {
            String nome = metodo.getName().toLowerCase();
            if (nome.startsWith("excluir") || nome.startsWith("remover")
                    || nome.startsWith("delete") || nome.startsWith("apagar")) {
                operacoesDeExclusao.add(metodo.getName());
            }
        }

        assertThat(operacoesDeExclusao).isEmpty();
    }

    @Test
    @DisplayName("Deve emitir protocolos CAT distintos sob concorrência, sem depender de contagem")
    void deveEmitirProtocolosDistintosSobConcorrencia() throws Exception {
        int quantidade = 8;
        CyclicBarrier largada = new CyclicBarrier(quantidade);
        ExecutorService executor = Executors.newFixedThreadPool(quantidade);
        List<String> protocolos = new ArrayList<>();
        try {
            List<Callable<String>> emissoes = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) {
                final int index = i;
                emissoes.add(() -> {
                    largada.await(20, TimeUnit.SECONDS);
                    return registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                            LocalDateTime.of(2026, 9, 7, 9, 0), "Acidente concorrente " + index,
                            "FATOR_HUMANO", "MEDIO", "Impacto", "Escoriação", "Ambulatório",
                            areaId, colaboradorId)).numeroProtocoloCAT();
                });
            }

            for (Future<String> emissao : executor.invokeAll(emissoes, 60, TimeUnit.SECONDS)) {
                protocolos.add(emissao.get());
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(protocolos).hasSize(quantidade).doesNotContainNull().doesNotHaveDuplicates();
        assertThat(protocolos).allSatisfy(protocolo -> assertThat(protocolo).matches("CAT-2026-09-\\d{4,}"));
        assertThat(contar("SELECT COUNT(DISTINCT numero_protocolo) FROM acidentes WHERE numero_protocolo IS NOT NULL"))
                .isEqualTo(quantidade);
    }

    @Test
    @DisplayName("Banco rejeita protocolo CAT duplicado")
    void bancoRejeitaProtocoloDuplicado() {
        OcorrenciaOutputDTO primeiro = registrarUseCase.registrarAcidente(acidenteSimples("Primeiro acidente"));
        Integer segundoId = registrarUseCase.registrarAcidente(acidenteSimples("Segundo acidente")).idOcorrencia();

        String protocolo = primeiro.numeroProtocoloCAT();
        assertThat(protocolo).isNotNull();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE acidentes SET numero_protocolo = ? WHERE id_ocorrencia = ?", protocolo, segundoId))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("Deve gravar como relator o gestor autenticado (issue #195)")
    void devePersistirGestorComoRelator() {
        autenticarComo("maria" + DOMINIO_DE_TESTE, "GESTOR_SEGURANCA");

        OcorrenciaOutputDTO criado = registrarUseCase.registrarAcidente(acidenteSimples("Ferramenta sem trava."));

        assertThat(gerenciarUseCase.buscarPorId(criado.idOcorrencia()).relatorId()).isEqualTo(gestorId);
        assertThat(contar("SELECT COUNT(*) FROM ocorrencias WHERE id_ocorrencia = ? AND relator_id = ?",
                criado.idOcorrencia(), gestorId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Relato do Supervisor grava o Supervisor como relator, sem deixar o vínculo nulo (issue #195)")
    void devePersistirSupervisorComoRelator() {
        autenticarComo("sergio" + DOMINIO_DE_TESTE, "SUPERVISOR");

        OcorrenciaOutputDTO criado = registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 16, 45), "Cabo solto no corredor.", "Cabo atravessado",
                "Tropeço", areaId, colaboradorId));

        autenticarComo("maria" + DOMINIO_DE_TESTE, "GESTOR_SEGURANCA");
        OcorrenciaOutputDTO lido = gerenciarUseCase.buscarPorId(criado.idOcorrencia());

        assertThat(lido.relatorId()).isEqualTo(supervisorId);
        assertThat(lido.relatorNome()).isEqualTo("Sérgio Supervisor");
        assertThat(lido.relatorPerfil()).isEqualTo("SUPERVISOR");
    }

    @Test
    @DisplayName("Banco exige área de risco e colaborador envolvido (colunas NOT NULL, issue #195)")
    void bancoExigeAreaEColaborador() {
        assertThat(jdbcTemplate.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name = 'ocorrencias' AND is_nullable = 'NO'
                  AND column_name IN ('area_risco_id', 'colaborador_id')
                """, String.class)).containsExactlyInAnyOrder("area_risco_id", "colaborador_id");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO ocorrencias (data_ocorrencia, descricao, colaborador_id) VALUES (?, 'Sem área', ?)",
                LocalDateTime.of(2026, 9, 5, 10, 0), colaboradorId))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO ocorrencias (data_ocorrencia, descricao, area_risco_id) VALUES (?, 'Sem colaborador', ?)",
                LocalDateTime.of(2026, 9, 5, 10, 0), areaId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Relato sem área é recusado pelo caso de uso e nada é gravado (issue #195)")
    void relatoSemAreaNaoGrava() {
        assertThatThrownBy(() -> registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 5, 10, 20), "Tropeço em cabo solto.", "Cabo", "Queda",
                null, colaboradorId)))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);

        assertThat(contar("SELECT COUNT(*) FROM ocorrencias")).isZero();
    }

    @Test
    @DisplayName("Banco recusa gravidade e causa raiz fora do vocabulário aprovado (issue #195)")
    void bancoRecusaGravidadeECausaInvalidas() {
        Integer id = registrarUseCase.registrarAcidente(acidenteSimples("Acidente válido")).idOcorrencia();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE acidentes SET gravidade = 'GRAVE' WHERE id_ocorrencia = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE acidentes SET causa_raiz = 'QUEDA' WHERE id_ocorrencia = ?", id))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Notificação ao Gestor fica gravada e pode ser consultada (UC09, passo 12; issue #195)")
    void deveGravarNotificacaoConsultavel() {
        OcorrenciaOutputDTO acidente = registrarUseCase.registrarAcidente(acidenteSimples("Acidente notificado"));
        OcorrenciaOutputDTO incidente = registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 4, 7, 30), "Incidente notificado.", "Sinalização ausente",
                "Colisão", areaId, colaboradorId));

        List<NotificacaoOcorrencia> notificacoes = consultarNotificacoesUseCase.listar();

        assertThat(notificacoes).hasSize(2);
        NotificacaoOcorrencia maisRecente = notificacoes.get(0);
        assertThat(maisRecente.ocorrenciaId()).isEqualTo(incidente.idOcorrencia());
        assertThat(maisRecente.tipoOcorrencia()).isEqualTo(TipoOcorrencia.INCIDENTE);
        assertThat(maisRecente.numeroProtocoloCAT()).isNull();
        NotificacaoOcorrencia doAcidente = notificacoes.get(1);
        assertThat(doAcidente.ocorrenciaId()).isEqualTo(acidente.idOcorrencia());
        assertThat(doAcidente.numeroProtocoloCAT().getValor()).isEqualTo(acidente.numeroProtocoloCAT());
        assertThat(doAcidente.areaNome()).isEqualTo("Linha de Prensas");
        assertThat(doAcidente.mensagem()).contains(acidente.numeroProtocoloCAT(), "Linha de Prensas");
    }

    @Test
    @DisplayName("Gravar a ocorrência não regrava o plano embutido legado (issue #196)")
    void naoRegravaPlanoEmbutidoLegado() {
        Integer ocorrenciaId = registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 2, 9, 0), "Relato com plano antigo.", "Piso", "Queda",
                areaId, colaboradorId)).idOcorrencia();
        Integer planoId = jdbcTemplate.queryForObject("""
                INSERT INTO planos_de_acao (ocorrencia_id, medidas_corretivas, prazo, status, colaborador_id,
                                            alerta_ativo, antecedencia_alerta_dias)
                VALUES (?, 'Plano do relato antigo', ?, 'ABERTA', ?, TRUE, 2)
                RETURNING id
                """, Integer.class, ocorrenciaId, LocalDate.of(2026, 9, 30), colaboradorId);
        jdbcTemplate.update("UPDATE ocorrencias SET plano_de_acao_id = ? WHERE id_ocorrencia = ?", planoId, ocorrenciaId);

        // Antes, a cascata regravava o plano com ocorrência, responsável e alerta nulos.
        gerenciarUseCase.enviarParaTriagem(ocorrenciaId);

        assertThat(gerenciarUseCase.buscarPorId(ocorrenciaId).statusOcorrencia()).isEqualTo("EM_TRIAGEM");
        assertThat(jdbcTemplate.queryForMap(
                "SELECT ocorrencia_id, colaborador_id, alerta_ativo, antecedencia_alerta_dias FROM planos_de_acao WHERE id = ?",
                planoId))
                .containsEntry("ocorrencia_id", ocorrenciaId)
                .containsEntry("colaborador_id", colaboradorId)
                .containsEntry("alerta_ativo", true)
                .containsEntry("antecedencia_alerta_dias", 2);
        assertThat(contar("SELECT COUNT(*) FROM ocorrencias WHERE id_ocorrencia = ? AND plano_de_acao_id = ?",
                ocorrenciaId, planoId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Listagem reconstrói acidentes e incidentes já gravados, com datas e vínculos")
    void deveListarOcorrenciasGravadas() {
        registrarUseCase.registrarAcidente(acidenteSimples("Acidente listado"));
        registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 4, 7, 30), "Incidente listado.",
                "Sinalização ausente", "Colisão com empilhadeira", areaId, colaboradorId));

        List<OcorrenciaOutputDTO> listadas = registrarUseCase.listar();

        assertThat(listadas).hasSize(2);
        assertThat(listadas).extracting(OcorrenciaOutputDTO::tipoOcorrencia)
                .containsExactlyInAnyOrder("ACIDENTE", "INCIDENTE");
        assertThat(listadas).allSatisfy(ocorrencia -> {
            assertThat(ocorrencia.dataRegistro()).isNotNull();
            assertThat(ocorrencia.statusOcorrencia()).isEqualTo("ABERTA");
            assertThat(ocorrencia.areaId()).isEqualTo(areaId);
            assertThat(ocorrencia.colaboradorId()).isEqualTo(colaboradorId);
        });
    }

    private RegistrarAcidenteInputDTO acidenteSimples(String descricao) {
        return new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 3, 13, 10), descricao, "FATOR_HUMANO", "BAIXO",
                "Impacto", "Escoriação", "Ambulatório", areaId, colaboradorId);
    }

    private void autenticarComo(String email, String perfil) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + perfil))));
    }

    private long contar(String sql, Object... parametros) {
        Long total = jdbcTemplate.queryForObject(sql, Long.class, parametros);
        return total != null ? total : 0L;
    }
}
