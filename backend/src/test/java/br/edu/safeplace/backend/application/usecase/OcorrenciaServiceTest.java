/*
 * Testes unitários do OcorrenciaService com repositório em memória: relato de acidente e incidente,
 * validação do relato no backend (issue #195), protocolo CAT e relator definidos pelo servidor,
 * transições de status, edição e visibilidade dos relatos para o Supervisor.
 */
package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import br.edu.safeplace.backend.application.dto.input.AtualizarOcorrenciaInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OcorrenciaServiceTest {

    private static final int AREA_ID = 10;
    private static final int COLABORADOR_ID = 20;

    private final Setor setor = new Setor(5, "Setor L", null, "Galpão", NivelPerigo.ALTO);
    private final AreaRisco area = new AreaRisco(AREA_ID, "AR-10", "Laminação", "Setor L", NivelPerigo.ALTO, setor,
            List.of(), "Galpão", List.of());
    private final Colaborador colaborador = new Colaborador(COLABORADOR_ID, "11122233344", "Carlos",
            LocalDate.of(1995, 2, 2), "carlos@email.com", true, LocalDateTime.now(), LocalDateTime.now());
    private final GestorDeSeguranca gestor = new GestorDeSeguranca(30, "55566677788", "Paula",
            LocalDate.of(1989, 4, 4), "paula@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
    private final Supervisor supervisor = new Supervisor(40, "99988877766", "Sérgio",
            LocalDate.of(1987, 6, 6), "sergio@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

    private OcorrenciaRepositoryFake repository;
    private AreaRiscoRepository areaRepo;
    private UsuarioRepository usuarioRepo;
    private NotificadorOcorrencia notificacaoPort;
    private OcorrenciaService useCase;

    @BeforeEach
    void configurar() {
        repository = new OcorrenciaRepositoryFake();
        areaRepo = mock(AreaRiscoRepository.class);
        usuarioRepo = mock(UsuarioRepository.class);
        notificacaoPort = mock(NotificadorOcorrencia.class);

        when(areaRepo.buscarPorId(AREA_ID)).thenReturn(Optional.of(area));
        when(usuarioRepo.buscarPorId(COLABORADOR_ID)).thenReturn(Optional.of(colaborador));
        when(usuarioRepo.buscarPorEmail("paula@email.com")).thenReturn(Optional.of(gestor));
        when(usuarioRepo.buscarPorEmail("sergio@email.com")).thenReturn(Optional.of(supervisor));

        useCase = novoUseCase(new TrilhaAuditoriaEmMemoria());
    }

    @AfterEach
    void limparSessao() {
        SecurityContextHolder.clearContext();
    }

    private OcorrenciaService novoUseCase(LogAuditoriaRepository auditoria) {
        return new OcorrenciaService(repository, areaRepo, usuarioRepo, auditoria, notificacaoPort);
    }

    private static void autenticar(String email, String papel) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                email, "senha", List.of(new SimpleGrantedAuthority(papel))));
    }

    private static RegistrarAcidenteInputDTO acidente(String causaRaiz, String gravidade) {
        return new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30),
                "Funcionario sofreu corte durante operacao de maquina.",
                causaRaiz,
                gravidade,
                "Corte",
                "Ferimento na mao direita",
                "Ambulatorio",
                AREA_ID,
                COLABORADOR_ID);
    }

    private static RegistrarIncidenteInputDTO incidente(String descricao) {
        return new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                descricao,
                "Piso escorregadio",
                "Queda",
                AREA_ID,
                COLABORADOR_ID);
    }

    @Test
    @DisplayName("Deve registrar acidente resolvendo área e colaborador pelos IDs")
    void deveRegistrarAcidente() {
        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(acidente("FALHA_EPI", "MEDIO"));

        assertEquals(1, salvo.idOcorrencia());
        assertEquals("ACIDENTE", salvo.tipoOcorrencia());
        assertEquals("ABERTA", salvo.statusOcorrencia());
        assertThat(salvo.areaId()).isEqualTo(AREA_ID);
        assertThat(salvo.areaNome()).isEqualTo("Laminação");
        assertThat(salvo.local()).isEqualTo("Laminação");
        assertThat(salvo.colaboradorId()).isEqualTo(COLABORADOR_ID);
        assertThat(salvo.colaboradorNome()).isEqualTo("Carlos");
        assertThat(salvo.causaRaiz()).isEqualTo("FALHA_EPI");
        assertThat(salvo.gravidade()).isEqualTo("MEDIO");
        assertThat(salvo.tipo()).isEqualTo("Corte");
        assertEquals(1, repository.listar().size());
    }

    @Test
    @DisplayName("Deve registrar incidente com área e colaborador")
    void deveRegistrarIncidente() {
        OcorrenciaOutputDTO salvo = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 15, 10),
                "Empilhamento irregular quase causou queda de material.",
                "Caixas acima do limite permitido",
                "Queda de material sobre funcionario",
                AREA_ID,
                COLABORADOR_ID));

        assertEquals(1, salvo.idOcorrencia());
        assertEquals("Laminação", salvo.local());
        assertEquals("Caixas acima do limite permitido", salvo.situacaoRisco());
        assertEquals("INCIDENTE", salvo.tipoOcorrencia());
        assertEquals("ABERTA", salvo.statusOcorrencia());
        assertThat(salvo.numeroProtocoloCAT()).isNull();
    }

    @Test
    @DisplayName("Relato sem área de risco é recusado e nada é gravado (issue #195)")
    void deveRecusarRelatoSemArea() {
        RegistrarAcidenteInputDTO semArea = new RegistrarAcidenteInputDTO(LocalDateTime.of(2026, 9, 6, 14, 30),
                "Corte", "FALHA_EPI", "BAIXO", null, null, null, null, COLABORADOR_ID);
        RegistrarIncidenteInputDTO incidenteSemArea = new RegistrarIncidenteInputDTO(LocalDateTime.of(2026, 9, 6, 14, 30),
                "Oleo", "Piso", "Queda", null, COLABORADOR_ID);

        assertThatThrownBy(() -> useCase.registrarAcidente(semArea))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessage("Área de risco é obrigatória.");
        assertThatThrownBy(() -> useCase.registrarIncidente(incidenteSemArea))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessage("Área de risco é obrigatória.");
        assertThat(repository.listar()).isEmpty();
        verify(notificacaoPort, never()).notificarGestor(any());
    }

    @Test
    @DisplayName("Relato sem colaborador envolvido é recusado e nada é gravado (issue #195)")
    void deveRecusarRelatoSemColaborador() {
        RegistrarAcidenteInputDTO semColaborador = new RegistrarAcidenteInputDTO(LocalDateTime.of(2026, 9, 6, 14, 30),
                "Corte", "FALHA_EPI", "BAIXO", null, null, null, AREA_ID, null);
        RegistrarIncidenteInputDTO incidenteSemColaborador = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), "Oleo", "Piso", "Queda", AREA_ID, null);

        assertThatThrownBy(() -> useCase.registrarAcidente(semColaborador))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessage("Colaborador envolvido é obrigatório.");
        assertThatThrownBy(() -> useCase.registrarIncidente(incidenteSemColaborador))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessage("Colaborador envolvido é obrigatório.");
        assertThat(repository.listar()).isEmpty();
    }

    @Test
    @DisplayName("Área ou colaborador inexistentes são recusados como dados inválidos")
    void deveRecusarAreaOuColaboradorInexistentes() {
        RegistrarIncidenteInputDTO areaInexistente = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), "Oleo", "Piso", "Queda", 999, COLABORADOR_ID);
        RegistrarIncidenteInputDTO colaboradorInexistente = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), "Oleo", "Piso", "Queda", AREA_ID, 999);

        assertThatThrownBy(() -> useCase.registrarIncidente(areaInexistente))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Área de risco não encontrada");
        assertThatThrownBy(() -> useCase.registrarIncidente(colaboradorInexistente))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Colaborador não encontrado");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"GRAVE", "LEVE", "xyz"})
    @DisplayName("Gravidade ausente ou inválida é recusada, sem virar BAIXO (issue #195)")
    void deveRecusarGravidadeInvalida(String gravidade) {
        assertThatThrownBy(() -> useCase.registrarAcidente(acidente("FALHA_EPI", gravidade)))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Gravidade");
        assertThat(repository.listar()).isEmpty();
        verify(notificacaoPort, never()).notificarGestor(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"QUEDA", "xyz"})
    @DisplayName("Causa raiz ausente ou inválida é recusada, sem virar OUTRO (issue #195)")
    void deveRecusarCausaRaizInvalida(String causaRaiz) {
        assertThatThrownBy(() -> useCase.registrarAcidente(acidente(causaRaiz, "ALTO")))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Causa raiz");
        assertThat(repository.listar()).isEmpty();
    }

    @Test
    @DisplayName("Gravidade e causa raiz aceitam qualquer combinação de maiúsculas")
    void deveAceitarGravidadeECausaSemDiferenciarMaiusculas() {
        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(acidente("fator_humano", "critico"));

        assertThat(salvo.causaRaiz()).isEqualTo("FATOR_HUMANO");
        assertThat(salvo.gravidade()).isEqualTo("CRITICO");
    }

    @Test
    @DisplayName("Protocolo CAT do acidente é sempre gerado pelo servidor (US13, issue #195)")
    void deveGerarProtocoloNoServidor() {
        OcorrenciaOutputDTO primeiro = useCase.registrarAcidente(acidente("OUTRO", "BAIXO"));
        OcorrenciaOutputDTO segundo = useCase.registrarAcidente(acidente("OUTRO", "BAIXO"));

        assertThat(primeiro.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");
        assertThat(segundo.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0002");
    }

    @Test
    @DisplayName("Gestor autenticado fica como relator do relato (issue #195)")
    void gestorAutenticadoDeveSerRelator() {
        autenticar("paula@email.com", "ROLE_GESTOR_SEGURANCA");

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(acidente("FATOR_HUMANO", "MEDIO"));

        assertThat(salvo.relatorId()).isEqualTo(30);
        assertThat(salvo.relatorNome()).isEqualTo("Paula");
        assertThat(salvo.relatorPerfil()).isEqualTo("GESTOR_SEGURANCA");
    }

    @Test
    @DisplayName("Supervisor autenticado fica como relator, sem deixar o relator nulo (issue #195)")
    void supervisorAutenticadoDeveSerRelator() {
        autenticar("sergio@email.com", "ROLE_SUPERVISOR");

        OcorrenciaOutputDTO salvo = useCase.registrarIncidente(incidente("Oleo no piso"));

        assertThat(salvo.relatorId()).isEqualTo(40);
        assertThat(salvo.relatorNome()).isEqualTo("Sérgio");
        assertThat(salvo.relatorPerfil()).isEqualTo("SUPERVISOR");
    }

    @Test
    @DisplayName("Sem sessão, o relato fica sem relator")
    void semSessaoNaoHaRelator() {
        OcorrenciaOutputDTO salvo = useCase.registrarIncidente(incidente("Oleo no piso"));

        assertThat(salvo.relatorId()).isNull();
    }

    @Test
    @DisplayName("Deve listar ocorrências registradas")
    void deveListarOcorrenciasRegistradas() {
        useCase.registrarIncidente(incidente("Piso molhado sem sinalizacao."));
        useCase.registrarAcidente(acidente("FATOR_HUMANO", "BAIXO"));

        List<OcorrenciaOutputDTO> ocorrencias = useCase.listar();

        assertEquals(2, ocorrencias.size());
        assertEquals("INCIDENTE", ocorrencias.get(0).tipoOcorrencia());
        assertEquals("ACIDENTE", ocorrencias.get(1).tipoOcorrencia());
    }

    @Test
    @DisplayName("Deve buscar ocorrência por ID com sucesso")
    void deveBuscarOcorrenciaPorId() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Fio desencapado"));

        OcorrenciaOutputDTO buscado = useCase.buscarPorId(cadastrado.idOcorrencia());

        assertThat(buscado.idOcorrencia()).isEqualTo(cadastrado.idOcorrencia());
        assertThat(buscado.descricao()).isEqualTo("Fio desencapado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ocorrência inexistente")
    void deveLancarExcecaoAoBuscarOcorrenciaInexistente() {
        assertThatThrownBy(() -> useCase.buscarPorId(999))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Deve transicionar ocorrência para EM_TRIAGEM")
    void deveEnviarOcorrenciaParaTriagem() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Quase queda"));

        OcorrenciaOutputDTO emTriagem = useCase.enviarParaTriagem(cadastrado.idOcorrencia());

        assertThat(emTriagem.statusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM.name());
    }

    @Test
    @DisplayName("Deve arquivar ocorrência após triagem")
    void deveArquivarOcorrenciaAposTriagem() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Desgaste estrutural"));

        useCase.enviarParaTriagem(cadastrado.idOcorrencia());
        OcorrenciaOutputDTO arquivada = useCase.arquivar(cadastrado.idOcorrencia());

        assertThat(arquivada.statusOcorrencia()).isEqualTo(StatusOcorrencia.ARQUIVADA.name());
    }

    @Test
    @DisplayName("Deve proibir arquivamento direto de ocorrência ABERTA")
    void deveProibirArquivamentoDiretoDeOcorrenciaAberta() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Falta de iluminacao"));

        assertThatThrownBy(() -> useCase.arquivar(cadastrado.idOcorrencia()))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Deve consolidar CAT de acidente legado gerando protocolo sequencial único e imutável")
    void deveConsolidarCATDeAcidente() {
        Acidente acidenteSemProtocolo = new Acidente(
                null,
                "Queimadura leve por respingo",
                LocalDateTime.of(2026, 9, 12, 9, 0),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                null,
                null,
                null,
                CausaRaizAcidente.FALHA_EPI,
                "Queimadura",
                "Braco esquerdo",
                null,
                "Posto de enfermagem",
                NivelPerigo.MEDIO
        );
        Acidente salvo = repository.salvarAcidente(acidenteSemProtocolo);

        assertThat(salvo.getNumeroProtocoloCAT()).isNull();

        OcorrenciaOutputDTO consolidado = useCase.consolidarCAT(salvo.getIdOcorrencia());

        assertThat(consolidado.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");

        assertThatThrownBy(() -> useCase.consolidarCAT(salvo.getIdOcorrencia()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Protocolo CAT já emitido e não pode ser alterado.");
    }

    @Test
    @DisplayName("Deve acionar porta de notificação interna ao registrar acidente e incidente")
    void deveNotificarGestorAoRegistrarAcidenteEIncidente() {
        useCase.registrarAcidente(acidente("OUTRO", "BAIXO"));

        verify(notificacaoPort).notificarGestor(any(Acidente.class));

        useCase.registrarIncidente(incidente("Cabo exposto no chão"));

        verify(notificacaoPort).notificarGestor(any(Incidente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao consolidar CAT de identificador inexistente")
    void deveLancarExcecaoAoConsolidarCATDeIdInexistente() {
        assertThatThrownBy(() -> useCase.consolidarCAT(404))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve buscar ocorrencia por protocolo CAT gerado no registro")
    void deveBuscarPorProtocoloComSucesso() {
        OcorrenciaOutputDTO registrado = useCase.registrarAcidente(acidente("OUTRO", "BAIXO"));

        OcorrenciaOutputDTO encontrada = useCase.buscarPorProtocolo(registrado.numeroProtocoloCAT());

        assertThat(encontrada.idOcorrencia()).isEqualTo(registrado.idOcorrencia());
        assertThat(encontrada.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar por protocolo inexistente")
    void deveLancarExcecaoAoBuscarPorProtocoloInexistente() {
        assertThatThrownBy(() -> useCase.buscarPorProtocolo("CAT-2026-09-9999"))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar por protocolo nulo ou vazio")
    void deveLancarExcecaoAoBuscarPorProtocoloVazio() {
        assertThatThrownBy(() -> useCase.buscarPorProtocolo("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve filtrar relatos do Supervisor ao listar")
    void deveFiltrarRelatosDoSupervisorAoListar() {
        LogAuditoriaRepository logRepo = mock(LogAuditoriaRepository.class);
        OcorrenciaService useCaseComLog = novoUseCase(logRepo);

        Acidente ac1 = new Acidente(1, "Desc 1", LocalDateTime.of(2026, 9, 1, 10, 0), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, colaborador, area, null, null, null,
                CausaRaizAcidente.OUTRO, null, null, NumeroProtocoloCAT.de("CAT-2026-09-0001"), null, NivelPerigo.BAIXO);
        Acidente ac2 = new Acidente(2, "Desc 2", LocalDateTime.of(2026, 9, 2, 10, 0), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, colaborador, area, null, null, null,
                CausaRaizAcidente.OUTRO, null, null, NumeroProtocoloCAT.de("CAT-2026-09-0002"), null, NivelPerigo.BAIXO);
        repository.salvarAcidente(ac1);
        repository.salvarAcidente(ac2);

        autenticar("supervisor@safeplace.com", "ROLE_SUPERVISOR");
        when(logRepo.buscarIdsPorResponsavelETipoOperacao("supervisor@safeplace.com", "CADASTRO", OcorrenciaService.ENTIDADES_DE_RELATO))
                .thenReturn(List.of(1));

        List<OcorrenciaOutputDTO> listaSupervisor = useCaseComLog.listar();
        assertThat(listaSupervisor).hasSize(1);
        assertThat(listaSupervisor.get(0).idOcorrencia()).isEqualTo(1);
    }

    @Test
    @DisplayName("Edição de acidente altera os dados e preserva tipo, registro, situação, relator e protocolo CAT")
    void deveEditarAcidentePreservandoIdentidade() {
        autenticar("paula@email.com", "ROLE_GESTOR_SEGURANCA");
        OcorrenciaOutputDTO cadastrado = useCase.registrarAcidente(acidente("FATOR_HUMANO", "BAIXO"));
        useCase.enviarParaTriagem(cadastrado.idOcorrencia());

        OcorrenciaOutputDTO editado = useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 6, 13, 0), "Corte profundo na mao direita.", AREA_ID, COLABORADOR_ID,
                "FALHA_EPI", "ALTO", "Laceracao", "Moderado", "Hospital", null, null));

        assertThat(editado.idOcorrencia()).isEqualTo(cadastrado.idOcorrencia());
        assertThat(editado.tipoOcorrencia()).isEqualTo("ACIDENTE");
        assertThat(editado.descricao()).isEqualTo("Corte profundo na mao direita.");
        assertThat(editado.dataOcorrencia()).isEqualTo(LocalDateTime.of(2026, 9, 6, 13, 0));
        assertThat(editado.dataRegistro()).isEqualTo(cadastrado.dataRegistro());
        assertThat(editado.statusOcorrencia()).isEqualTo("EM_TRIAGEM");
        assertThat(editado.numeroProtocoloCAT()).isEqualTo(cadastrado.numeroProtocoloCAT());
        assertThat(editado.relatorId()).isEqualTo(30);
        assertThat(editado.causaRaiz()).isEqualTo("FALHA_EPI");
        assertThat(editado.gravidade()).isEqualTo("ALTO");
        assertThat(editado.destino()).isEqualTo("Hospital");
    }

    @Test
    @DisplayName("Edição mantém gravidade e causa em branco e recusa valores inválidos (issue #195)")
    void edicaoDeveRecusarGravidadeECausaInvalidas() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarAcidente(acidente("FALHA_EPI", "ALTO"));

        OcorrenciaOutputDTO mantido = useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), "Corte", AREA_ID, COLABORADOR_ID,
                " ", null, null, null, null, null, null));
        assertThat(mantido.causaRaiz()).isEqualTo("FALHA_EPI");
        assertThat(mantido.gravidade()).isEqualTo("ALTO");

        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), "Corte", AREA_ID, COLABORADOR_ID,
                "QUEDA", null, null, null, null, null, null)))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);
        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), "Corte", AREA_ID, COLABORADOR_ID,
                null, "GRAVE", null, null, null, null, null)))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);
    }

    @Test
    @DisplayName("Edição sem área ou colaborador é recusada (issue #195)")
    void edicaoDeveExigirAreaEColaborador() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Oleo no piso"));

        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo", null, COLABORADOR_ID,
                null, null, null, null, null, "Piso", "Queda")))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);
        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo", AREA_ID, null,
                null, null, null, null, null, "Piso", "Queda")))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class);
    }

    @Test
    @DisplayName("Edição de incidente exige situação de risco e potencial de dano")
    void deveEditarIncidenteValidandoCamposProprios() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Oleo no piso"));

        OcorrenciaOutputDTO editado = useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo no piso da doca", AREA_ID, COLABORADOR_ID,
                null, null, null, null, null, "Vazamento de empilhadeira", "Queda com fratura"));

        assertThat(editado.situacaoRisco()).isEqualTo("Vazamento de empilhadeira");
        assertThat(editado.potencialDano()).isEqualTo("Queda com fratura");
        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo", AREA_ID, COLABORADOR_ID,
                null, null, null, null, null, " ", "Queda")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Ocorrência arquivada não é editada e inexistente responde não encontrada")
    void deveRecusarEdicaoDeArquivadaOuInexistente() {
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(incidente("Oleo no piso"));
        useCase.enviarParaTriagem(cadastrado.idOcorrencia());
        useCase.arquivar(cadastrado.idOcorrencia());
        AtualizarOcorrenciaInputDTO dados = new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Nova descricao", AREA_ID, COLABORADOR_ID,
                null, null, null, null, null, "Piso", "Queda");

        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), dados))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("arquivada");
        assertThatThrownBy(() -> useCase.atualizar(999, dados))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Edição registra na auditoria o operador e os campos alterados")
    void deveAuditarCamposAlteradosNaEdicao() {
        LogAuditoriaRepository logRepo = mock(LogAuditoriaRepository.class);
        OcorrenciaService useCaseComLog = novoUseCase(logRepo);
        OcorrenciaOutputDTO cadastrado = useCaseComLog.registrarIncidente(incidente("Oleo no piso"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor@safeplace.com", "senha"));
        useCaseComLog.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo no piso da doca", AREA_ID, COLABORADOR_ID,
                null, null, null, null, null, "Piso escorregadio", "Queda grave"));

        org.mockito.ArgumentCaptor<LogAuditoria> captor = org.mockito.ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logRepo, org.mockito.Mockito.times(2)).salvar(captor.capture());
        LogAuditoria edicao = captor.getAllValues().get(1);
        assertThat(edicao.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(edicao.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(edicao.getJustificativa()).contains("descricao", "potencialDano")
                .doesNotContain("situacaoRisco", "area", "colaborador");
    }

    private static class OcorrenciaRepositoryFake implements OcorrenciaRepository {

        private final List<Ocorrencia> ocorrencias = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public Acidente salvarAcidente(Acidente acidente) {
            if (acidente.getIdOcorrencia() != null) {
                ocorrencias.removeIf(o -> o.getIdOcorrencia().equals(acidente.getIdOcorrencia()));
                ocorrencias.add(acidente);
                return acidente;
            }
            Acidente salvo = new Acidente(
                    proximoId++,
                    acidente.getDescricao(),
                    acidente.getDataOcorrencia(),
                    acidente.getDataRegistro(),
                    acidente.getStatusOcorrencia(),
                    acidente.getColaborador(),
                    acidente.getArea(),
                    acidente.getRelator(),
                    acidente.getLocalInformado(),
                    acidente.getPlanoDeAcao(),
                    acidente.getCausaRaiz(),
                    acidente.getTipo(),
                    acidente.getDano(),
                    acidente.getProtocoloCAT(),
                    acidente.getDestino(),
                    acidente.getGravidade());
            ocorrencias.add(salvo);
            return salvo;
        }

        @Override
        public Incidente salvarIncidente(Incidente incidente) {
            if (incidente.getIdOcorrencia() != null) {
                ocorrencias.removeIf(o -> o.getIdOcorrencia().equals(incidente.getIdOcorrencia()));
                ocorrencias.add(incidente);
                return incidente;
            }
            Incidente salvo = new Incidente(
                    proximoId++,
                    incidente.getDescricao(),
                    incidente.getDataOcorrencia(),
                    incidente.getDataRegistro(),
                    incidente.getStatusOcorrencia(),
                    incidente.getColaborador(),
                    incidente.getArea(),
                    incidente.getRelator(),
                    incidente.getLocalInformado(),
                    incidente.getPlanoDeAcao(),
                    incidente.getSituacaoRisco(),
                    incidente.getPotencialDano());
            ocorrencias.add(salvo);
            return salvo;
        }

        @Override
        public List<Ocorrencia> listar() {
            return new ArrayList<>(ocorrencias);
        }

        @Override
        public Optional<Ocorrencia> buscarPorId(Integer id) {
            return ocorrencias.stream()
                    .filter(o -> o.getIdOcorrencia().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Ocorrencia> buscarPorProtocolo(String protocolo) {
            if (protocolo == null || protocolo.isBlank()) {
                return Optional.empty();
            }
            return ocorrencias.stream()
                    .filter(Acidente.class::isInstance)
                    .map(Acidente.class::cast)
                    .filter(a -> protocolo.equalsIgnoreCase(a.getNumeroProtocoloCAT()))
                    .map(Ocorrencia.class::cast)
                    .findFirst();
        }

        @Override
        public long proximoSequencialCAT(int ano, int mes) {
            return ocorrencias.stream()
                    .filter(Acidente.class::isInstance)
                    .map(Acidente.class::cast)
                    .filter(a -> a.getNumeroProtocoloCAT() != null)
                    .count() + 1;
        }
    }
}
