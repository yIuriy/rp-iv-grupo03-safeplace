package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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

        OcorrenciaOutputDTO acidenteCadastrado = useCase.registrarAcidente(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 12, 9, 0),
                "Fundicao",
                "Queimadura leve por respingo",
                null,
                "FALHA_EPI",
                "Queimadura",
                "Braco esquerdo",
                null,
                "Posto de enfermagem"
        ));

        assertThat(acidenteCadastrado.numeroProtocoloCAT()).isNull();

        OcorrenciaOutputDTO consolidado = useCase.consolidarCAT(acidenteCadastrado.idOcorrencia());

        assertThat(consolidado.numeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");

        // Tentativa de consolidar novamente deve ser proibida
        assertThatThrownBy(() -> useCase.consolidarCAT(acidenteCadastrado.idOcorrencia()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Protocolo CAT já emitido e não pode ser alterado.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao consolidar CAT de identificador inexistente")
    void deveLancarExcecaoAoConsolidarCATDeIdInexistente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository, null, null, null);

        assertThatThrownBy(() -> useCase.consolidarCAT(404))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
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
        public long proximoSequencialCAT(int ano, int mes) {
            return ocorrencias.stream()
                    .filter(Acidente.class::isInstance)
                    .map(Acidente.class::cast)
                    .filter(a -> a.getNumeroProtocoloCAT() != null)
                    .count() + 1;
        }
    }
}
