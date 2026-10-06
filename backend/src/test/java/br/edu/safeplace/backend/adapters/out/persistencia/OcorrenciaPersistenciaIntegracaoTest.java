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
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Cobre, contra um PostgreSQL real, a persistência de ocorrências exigida pela issue #96: cada
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
    private JdbcTemplate jdbcTemplate;

    private Integer areaId;
    private Integer colaboradorId;
    private Integer gestorId;

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
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        limparDados();
    }

    private void limparDados() {
        jdbcTemplate.execute("DELETE FROM acidentes");
        jdbcTemplate.execute("DELETE FROM incidentes");
        jdbcTemplate.execute("DELETE FROM ocorrencias");
        jdbcTemplate.execute("DELETE FROM prorrogacoes_prazo");
        jdbcTemplate.execute("DELETE FROM planos_de_acao");
        jdbcTemplate.execute("DELETE FROM sequencias_protocolo_cat");
        jdbcTemplate.update("DELETE FROM usuarios WHERE email LIKE ? OR cpf IN (?, ?)",
                "%" + DOMINIO_DE_TESTE, CPF_COLABORADOR, CPF_GESTOR);
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

        OcorrenciaOutputDTO criado = registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                dataFato, null, "Prensa atingiu a mão do operador.", null, "FALHA_EPI",
                "Esmagamento", "Lesão na mão direita", null, "Hospital Municipal",
                NivelPerigo.CRITICO, areaId, colaboradorId, gestorId));

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
        assertThat(lido.gestorId()).isEqualTo(gestorId);
        assertThat(lido.gestorNome()).isEqualTo("Maria Gestora");

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
                LocalDateTime.of(2026, 9, 9, 8, 15), null, "Piso oleoso próximo à prensa.",
                null, "Piso escorregadio", "Queda de mesmo nível", areaId, colaboradorId, gestorId));

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
                LocalDateTime.of(2026, 9, 8, 11, 5), null, "Queda de ferramenta da passarela.",
                null, "FATOR_HUMANO", "Impacto", "Contusão no ombro", null, "Ambulatório",
                NivelPerigo.ALTO, areaId, colaboradorId, gestorId));

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
        for (Method metodo : OcorrenciaRepositoryPort.class.getMethods()) {
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
        List<Integer> acidentes = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            acidentes.add(registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                    LocalDateTime.of(2026, 9, 7, 9, 0), null, "Acidente concorrente " + i, null,
                    "FATOR_HUMANO", "Impacto", "Escoriação", null, "Ambulatório",
                    NivelPerigo.MEDIO, areaId, colaboradorId, gestorId)).idOcorrencia());
        }

        CyclicBarrier largada = new CyclicBarrier(quantidade);
        ExecutorService executor = Executors.newFixedThreadPool(quantidade);
        List<String> protocolos = new ArrayList<>();
        try {
            List<Callable<String>> emissoes = acidentes.stream()
                    .map(id -> (Callable<String>) () -> {
                        largada.await(20, TimeUnit.SECONDS);
                        return gerenciarUseCase.consolidarCAT(id).numeroProtocoloCAT();
                    })
                    .toList();

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
        Integer primeiro = registrarUseCase.registrarAcidente(acidenteSimples("Primeiro acidente")).idOcorrencia();
        Integer segundo = registrarUseCase.registrarAcidente(acidenteSimples("Segundo acidente")).idOcorrencia();

        String protocolo = gerenciarUseCase.consolidarCAT(primeiro).numeroProtocoloCAT();
        assertThat(protocolo).isNotNull();

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE acidentes SET numero_protocolo = ? WHERE id_ocorrencia = ?", protocolo, segundo))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("Deve gravar como relator o gestor autenticado, mesmo sem gestorId na requisição")
    void devePersistirRelatorAutenticado() {
        autenticarComo("maria" + DOMINIO_DE_TESTE, "GESTOR_SEGURANCA");

        OcorrenciaOutputDTO criado = registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 16, 45), null, "Ferramenta sem trava de segurança.",
                null, "FALHA_EPI", "Corte", "Corte superficial", null, "Ambulatório",
                NivelPerigo.BAIXO, areaId, colaboradorId, null));

        OcorrenciaOutputDTO lido = gerenciarUseCase.buscarPorId(criado.idOcorrencia());

        assertThat(lido.gestorId()).isEqualTo(gestorId);
        assertThat(contar("SELECT COUNT(*) FROM ocorrencias WHERE id_ocorrencia = ? AND gestor_id = ?",
                criado.idOcorrencia(), gestorId)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve preservar o texto livre de local sem inventar uma área de risco")
    void naoDeveDerivarAreaFicticiaDoTextoLocal() {
        OcorrenciaOutputDTO criado = registrarUseCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 5, 10, 20), "Pátio externo", "Tropeço em cabo solto.",
                null, "FATOR_HUMANO", "Queda", "Torção no tornozelo", null, "Ambulatório",
                NivelPerigo.BAIXO, null, colaboradorId, gestorId));

        OcorrenciaOutputDTO lido = gerenciarUseCase.buscarPorId(criado.idOcorrencia());

        assertThat(lido.areaId()).isNull();
        assertThat(lido.areaNome()).isNull();
        assertThat(lido.local()).isEqualTo("Pátio externo");
        assertThat(contar("SELECT COUNT(*) FROM ocorrencias WHERE id_ocorrencia = ? AND area_risco_id IS NULL",
                criado.idOcorrencia())).isEqualTo(1);
        assertThat(contar("SELECT COUNT(*) FROM areas_risco WHERE nome = ?", "Pátio externo")).isZero();
    }

    @Test
    @DisplayName("Listagem reconstrói acidentes e incidentes já gravados, com datas e vínculos")
    void deveListarOcorrenciasGravadas() {
        registrarUseCase.registrarAcidente(acidenteSimples("Acidente listado"));
        registrarUseCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 4, 7, 30), null, "Incidente listado.", null,
                "Sinalização ausente", "Colisão com empilhadeira", areaId, colaboradorId, gestorId));

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
                LocalDateTime.of(2026, 9, 3, 13, 10), null, descricao, null, "FATOR_HUMANO",
                "Impacto", "Escoriação", null, "Ambulatório", NivelPerigo.BAIXO,
                areaId, colaboradorId, gestorId);
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
