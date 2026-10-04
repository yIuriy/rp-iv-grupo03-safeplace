/*
 * Testes unitários do OcorrenciaUseCase com repositório em memória: relato de acidente e incidente,
 * protocolo CAT, transições de status e visibilidade dos relatos para o Supervisor.
 */
package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarOcorrenciaInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.NotificacaoOcorrenciaPort;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OcorrenciaUseCaseTest {

    @Test
    @DisplayName("Deve registrar acidente com sucesso")
    void deveRegistrarAcidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30),
                "Setor de corte",
                "Funcionario sofreu corte durante operacao de maquina.",
                new PlanoDeAcao(
                        null,
                        "Isolar maquina.",
                        LocalDate.of(2026, 9, 10),
                        "PENDENTE",
                        "Treinamento de seguranca."),
                "FALHA_EPI",
                "Corte",
                "Ferimento na mao direita",
                null,
                "Ambulatorio");

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(input);

        assertEquals(1, salvo.idOcorrencia());
        assertEquals("Setor de corte", salvo.local());
        assertEquals("Corte", salvo.tipo());
        assertEquals("ACIDENTE", salvo.tipoOcorrencia());
        assertEquals("ABERTA", salvo.statusOcorrencia());
        assertEquals(1, repository.listar().size());
    }

    @Test
    @DisplayName("Deve registrar acidente com gravidade informada")
    void deveRegistrarAcidenteComGravidade() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30),
                "Setor de corte",
                "Funcionario sofreu corte durante operacao de maquina.",
                null,
                "FALHA_EPI",
                "Corte",
                "Ferimento na mao direita",
                null,
                "Ambulatorio",
                NivelPerigo.ALTO);

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(input);

        assertEquals("ALTO", salvo.gravidade());
    }

    @Test
    @DisplayName("Deve registrar acidente resolvendo area, colaborador e gestor pelos IDs")
    void deveRegistrarAcidenteComAreaColaboradorEGestor() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        AreaRiscoRepositoryPort areaRepo = mock(AreaRiscoRepositoryPort.class);
        UsuarioRepositorioPorta usuarioRepo = mock(UsuarioRepositorioPorta.class);
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, areaRepo, usuarioRepo, null);

        AreaRisco area = new AreaRisco(10, "AR-10", "Laminação", "Setor L", NivelPerigo.ALTO, 5, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(20, "11122233344", "Carlos", LocalDate.of(1995, 2, 2), "carlos@email.com", true, LocalDateTime.now(), LocalDateTime.now());
        GestorDeSeguranca gestor = new GestorDeSeguranca(30, "55566677788", "Paula", LocalDate.of(1989, 4, 4), "paula@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        when(areaRepo.buscarPorId(10)).thenReturn(Optional.of(area));
        when(usuarioRepo.buscarPorId(20)).thenReturn(Optional.of(colaborador));
        when(usuarioRepo.buscarPorId(30)).thenReturn(Optional.of(gestor));

        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 15, 10, 0),
                null,
                "Queda no piso de laminacao",
                null,
                "FATOR_HUMANO",
                "Queda",
                "Entorse",
                null,
                "Enfermaria",
                NivelPerigo.MEDIO,
                10,
                20,
                30
        );

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(input);

        assertThat(salvo.idOcorrencia()).isEqualTo(1);
        assertThat(salvo.areaId()).isEqualTo(10);
        assertThat(salvo.areaNome()).isEqualTo("Laminação");
        assertThat(salvo.colaboradorId()).isEqualTo(20);
        assertThat(salvo.colaboradorNome()).isEqualTo("Carlos");
        assertThat(salvo.gestorId()).isEqualTo(30);
        assertThat(salvo.gestorNome()).isEqualTo("Paula");
        assertThat(salvo.gravidade()).isEqualTo("MEDIO");
    }

    @Test
    @DisplayName("Deve registrar incidente com sucesso")
    void deveRegistrarIncidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        RegistrarIncidenteInputDTO input = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 15, 10),
                "Almoxarifado",
                "Empilhamento irregular quase causou queda de material.",
                new PlanoDeAcao(
                        null,
                        "Reorganizar estoque.",
                        LocalDate.of(2026, 9, 7),
                        "EM_ANDAMENTO",
                        "Definir limite visual."),
                "Caixas acima do limite permitido",
                "Queda de material sobre funcionario");

        OcorrenciaOutputDTO salvo = useCase.registrarIncidente(input);

        assertEquals(1, salvo.idOcorrencia());
        assertEquals("Almoxarifado", salvo.local());
        assertEquals("Caixas acima do limite permitido", salvo.situacaoRisco());
        assertEquals("INCIDENTE", salvo.tipoOcorrencia());
        assertEquals("ABERTA", salvo.statusOcorrencia());
        assertEquals(1, repository.listar().size());
    }

    @Test
    @DisplayName("Deve listar ocorrências registradas")
    void deveListarOcorrenciasRegistradas() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 16, 0),
                "Corredor principal",
                "Piso molhado sem sinalizacao.",
                null,
                "Risco de queda",
                "Lesao leve"));

        useCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 17, 0),
                "Oficina",
                "Funcionario prensou o dedo.",
                null,
                "FATOR_HUMANO",
                "Prensamento",
                "Lesao no dedo",
                "CAT-2026-09-0002",
                "Ambulatorio"));

        List<OcorrenciaOutputDTO> ocorrencias = useCase.listar();

        assertEquals(2, ocorrencias.size());
        assertEquals("INCIDENTE", ocorrencias.get(0).tipoOcorrencia());
        assertEquals("ACIDENTE", ocorrencias.get(1).tipoOcorrencia());
    }

    @Test
    @DisplayName("Deve buscar ocorrência por ID com sucesso")
    void deveBuscarOcorrenciaPorId() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Estoque",
                "Fio desencapado",
                null,
                "Fio eletrico exposto",
                "Choque eletrico"
        ));

        OcorrenciaOutputDTO buscado = useCase.buscarPorId(cadastrado.idOcorrencia());

        assertThat(buscado.idOcorrencia()).isEqualTo(cadastrado.idOcorrencia());
        assertThat(buscado.descricao()).isEqualTo("Fio desencapado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ocorrência inexistente")
    void deveLancarExcecaoAoBuscarOcorrenciaInexistente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        assertThatThrownBy(() -> useCase.buscarPorId(999))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Deve transicionar ocorrência para EM_TRIAGEM")
    void deveEnviarOcorrenciaParaTriagem() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Setor A",
                "Quase queda",
                null,
                "Chao escorregadio",
                "Queda de nivel"
        ));

        OcorrenciaOutputDTO emTriagem = useCase.enviarParaTriagem(cadastrado.idOcorrencia());

        assertThat(emTriagem.statusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM.name());
    }

    @Test
    @DisplayName("Deve arquivar ocorrência após triagem")
    void deveArquivarOcorrenciaAposTriagem() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Setor B",
                "Desgaste estrutural",
                null,
                "Rachadura em suporte",
                "Desabamento parcial"
        ));

        useCase.enviarParaTriagem(cadastrado.idOcorrencia());
        OcorrenciaOutputDTO arquivada = useCase.arquivar(cadastrado.idOcorrencia());

        assertThat(arquivada.statusOcorrencia()).isEqualTo(StatusOcorrencia.ARQUIVADA.name());
    }

    @Test
    @DisplayName("Deve proibir arquivamento direto de ocorrência ABERTA")
    void deveProibirArquivamentoDiretoDeOcorrenciaAberta() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Setor C",
                "Falta de iluminacao",
                null,
                "Lampadas queimadas",
                "Queda por falta de visibilidade"
        ));

        assertThatThrownBy(() -> useCase.arquivar(cadastrado.idOcorrencia()))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Deve consolidar CAT de acidente gerando protocolo sequencial único e imutável")
    void deveConsolidarCATDeAcidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        Acidente acidenteSemProtocolo = new Acidente(
                null,
                "Queimadura leve por respingo",
                LocalDateTime.of(2026, 9, 12, 9, 0),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of(),
                null,
                List.of(),
                null,
                null,
                "Fundicao",
                null,
                CausaRaizAcidente.FALHA_EPI,
                "Queimadura",
                "Braco esquerdo",
                null,
                "Posto de enfermagem"
        );
        Acidente salvo = repository.salvarAcidente(acidenteSemProtocolo);

        assertThat(salvo.getNumeroProtocoloCAT()).isNull();

        OcorrenciaOutputDTO consolidado = useCase.consolidarCAT(salvo.getIdOcorrencia());

        assertThat(consolidado.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");

        // Tentativa de consolidar novamente deve ser proibida
        assertThatThrownBy(() -> useCase.consolidarCAT(salvo.getIdOcorrencia()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Protocolo CAT já emitido e não pode ser alterado.");
    }

    @Test
    @DisplayName("Deve gerar automaticamente protocolo CAT quando omitido no registro de acidente")
    void deveGerarProtocoloAutomaticoQuandoOmitido() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Galpão",
                "Queda de ferramenta",
                null,
                "OUTRO",
                "Impacto",
                "Contusão no pé",
                null,
                "Ambulatório"
        );

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(input);

        assertThat(salvo.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");
    }

    @Test
    @DisplayName("Deve preservar protocolo fornecido no registro de acidente se válido")
    void devePreservarProtocoloFornecido() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Galpão",
                "Queda de ferramenta",
                null,
                "OUTRO",
                "Impacto",
                "Contusão no pé",
                "CAT-2026-09-9999",
                "Ambulatório"
        );

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(input);

        assertThat(salvo.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-9999");
    }

    @Test
    @DisplayName("Deve acionar porta de notificação interna ao registrar acidente e incidente")
    void deveNotificarGestorAoRegistrarAcidenteEIncidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        NotificacaoOcorrenciaPort notificacaoPort = mock(NotificacaoOcorrenciaPort.class);
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null, notificacaoPort);

        RegistrarAcidenteInputDTO inputAcidente = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Galpão",
                "Queda de ferramenta",
                null,
                "OUTRO",
                "Impacto",
                "Contusão no pé",
                null,
                "Ambulatório"
        );
        useCase.registrarAcidente(inputAcidente);

        verify(notificacaoPort).notificarNovoRelato(any(Acidente.class));

        RegistrarIncidenteInputDTO inputIncidente = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 11, 0),
                "Galpão",
                "Cabo exposto no chão",
                null,
                "Cabo energizado",
                "Choque elétrico"
        );
        useCase.registrarIncidente(inputIncidente);

        verify(notificacaoPort).notificarNovoRelato(any(Incidente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao consolidar CAT de identificador inexistente")
    void deveLancarExcecaoAoConsolidarCATDeIdInexistente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        assertThatThrownBy(() -> useCase.consolidarCAT(404))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve buscar ocorrencia por protocolo CAT com sucesso")
    void deveBuscarPorProtocoloComSucesso() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0),
                "Galpão",
                "Queda de ferramenta",
                null,
                "OUTRO",
                "Impacto",
                "Contusão no pé",
                "CAT-2026-09-0010",
                "Ambulatório"
        );
        useCase.registrarAcidente(input);

        OcorrenciaOutputDTO encontrada = useCase.buscarPorProtocolo("CAT-2026-09-0010");
        assertThat(encontrada).isNotNull();
        assertThat(encontrada.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0010");
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar por protocolo inexistente")
    void deveLancarExcecaoAoBuscarPorProtocoloInexistente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        assertThatThrownBy(() -> useCase.buscarPorProtocolo("CAT-2026-09-9999"))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao ao buscar por protocolo nulo ou vazio")
    void deveLancarExcecaoAoBuscarPorProtocoloVazio() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        assertThatThrownBy(() -> useCase.buscarPorProtocolo("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve filtrar relatos do Supervisor ao listar")
    void deveFiltrarRelatosDoSupervisorAoListar() {
        try {
            OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
            LogAuditoriaRepositoryPort logRepo = mock(LogAuditoriaRepositoryPort.class);
            OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, logRepo);

            Acidente ac1 = new Acidente(1, "Desc 1", LocalDateTime.of(2026, 9, 1, 10, 0), LocalDateTime.now(),
                    StatusOcorrencia.ABERTA, List.of(), null, List.of(), null, null, "Local 1", null,
                    CausaRaizAcidente.OUTRO, null, null, "CAT-2026-09-0001", null, null);
            Acidente ac2 = new Acidente(2, "Desc 2", LocalDateTime.of(2026, 9, 2, 10, 0), LocalDateTime.now(),
                    StatusOcorrencia.ABERTA, List.of(), null, List.of(), null, null, "Local 2", null,
                    CausaRaizAcidente.OUTRO, null, null, "CAT-2026-09-0002", null, null);
            repository.salvarAcidente(ac1);
            repository.salvarAcidente(ac2);

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            "supervisor@safeplace.com",
                            "senha",
                            List.of(new SimpleGrantedAuthority("ROLE_SUPERVISOR"))
                    )
            );

            when(logRepo.buscarIdsPorResponsavelETipoOperacao("supervisor@safeplace.com", "CADASTRO", OcorrenciaUseCase.ENTIDADES_DE_RELATO))
                    .thenReturn(List.of(1));

            List<OcorrenciaOutputDTO> listaSupervisor = useCase.listar();
            assertThat(listaSupervisor).hasSize(1);
            assertThat(listaSupervisor.get(0).idOcorrencia()).isEqualTo(1);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Edição de acidente altera os dados e preserva tipo, registro, situação e protocolo CAT")
    void deveEditarAcidentePreservandoIdentidade() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);
        OcorrenciaOutputDTO cadastrado = useCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 6, 14, 30), null, "Corte na mao.", null,
                "FATOR_HUMANO", "Corte", "Leve", "CAT-2026-09-0007", "Ambulatorio", NivelPerigo.BAIXO));
        useCase.enviarParaTriagem(cadastrado.idOcorrencia());

        OcorrenciaOutputDTO editado = useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 6, 13, 0), "Corte profundo na mao direita.", null, null,
                "FALHA_EPI", "ALTO", "Laceracao", "Moderado", "Hospital", null, null));

        assertThat(editado.idOcorrencia()).isEqualTo(cadastrado.idOcorrencia());
        assertThat(editado.tipoOcorrencia()).isEqualTo("ACIDENTE");
        assertThat(editado.descricao()).isEqualTo("Corte profundo na mao direita.");
        assertThat(editado.dataOcorrencia()).isEqualTo(LocalDateTime.of(2026, 9, 6, 13, 0));
        assertThat(editado.dataRegistro()).isEqualTo(cadastrado.dataRegistro());
        assertThat(editado.statusOcorrencia()).isEqualTo("EM_TRIAGEM");
        assertThat(editado.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0007");
        assertThat(editado.causaRaiz()).isEqualTo("FALHA_EPI");
        assertThat(editado.gravidade()).isEqualTo("ALTO");
        assertThat(editado.destino()).isEqualTo("Hospital");
    }

    @Test
    @DisplayName("Edição de incidente exige situação de risco e potencial de dano")
    void deveEditarIncidenteValidandoCamposProprios() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), null, "Oleo no piso", null, "Piso escorregadio", "Queda"));

        OcorrenciaOutputDTO editado = useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo no piso da doca", null, null,
                null, null, null, null, null, "Vazamento de empilhadeira", "Queda com fratura"));

        assertThat(editado.situacaoRisco()).isEqualTo("Vazamento de empilhadeira");
        assertThat(editado.potencialDano()).isEqualTo("Queda com fratura");
        assertThatThrownBy(() -> useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo", null, null,
                null, null, null, null, null, " ", "Queda")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Ocorrência arquivada não é editada e inexistente responde não encontrada")
    void deveRecusarEdicaoDeArquivadaOuInexistente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), null, "Oleo no piso", null, "Piso escorregadio", "Queda"));
        useCase.enviarParaTriagem(cadastrado.idOcorrencia());
        useCase.arquivar(cadastrado.idOcorrencia());
        AtualizarOcorrenciaInputDTO dados = new AtualizarOcorrenciaInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Nova descricao", null, null,
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
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        LogAuditoriaRepositoryPort logRepo = mock(LogAuditoriaRepositoryPort.class);
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, logRepo);
        OcorrenciaOutputDTO cadastrado = useCase.registrarIncidente(new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), null, "Oleo no piso", null, "Piso escorregadio", "Queda"));
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("gestor@safeplace.com", "senha"));
            useCase.atualizar(cadastrado.idOcorrencia(), new AtualizarOcorrenciaInputDTO(
                    LocalDateTime.of(2026, 9, 10, 10, 0), "Oleo no piso da doca", null, null,
                    null, null, null, null, null, "Piso escorregadio", "Queda grave"));

            org.mockito.ArgumentCaptor<LogAuditoria> captor = org.mockito.ArgumentCaptor.forClass(LogAuditoria.class);
            verify(logRepo, org.mockito.Mockito.times(2)).salvar(captor.capture());
            LogAuditoria edicao = captor.getAllValues().get(1);
            assertThat(edicao.getTipoOperacao()).isEqualTo("ALTERACAO");
            assertThat(edicao.getResponsavel()).isEqualTo("gestor@safeplace.com");
            assertThat(edicao.getJustificativa()).contains("descricao", "potencialDano").doesNotContain("situacaoRisco");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static class OcorrenciaRepositoryFake implements OcorrenciaRepositoryPort {

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
                    acidente.getTestemunhas(),
                    acidente.getColaborador(),
                    acidente.getMidias(),
                    acidente.getArea(),
                    acidente.getGestor(),
                    acidente.getLocal(),
                    acidente.getPlanoDeAcao(),
                    acidente.getCausaRaiz(),
                    acidente.getTipo(),
                    acidente.getDano(),
                    acidente.getNumeroProtocoloCAT(),
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
                    incidente.getTestemunhas(),
                    incidente.getColaborador(),
                    incidente.getMidias(),
                    incidente.getArea(),
                    incidente.getGestor(),
                    incidente.getLocal(),
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
