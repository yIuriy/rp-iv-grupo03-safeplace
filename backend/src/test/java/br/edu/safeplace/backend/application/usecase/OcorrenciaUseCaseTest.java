package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OcorrenciaUseCaseTest {

    @Test
    void deveRegistrarAcidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository);

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
                "Ausencia de protecao adequada",
                "Corte",
                "Ferimento na mao direita",
                "ACD-2026-001",
                "Ambulatorio");

        OcorrenciaOutputDTO salvo = useCase.registrarAcidente(input);

        assertEquals(1, salvo.idOcorrencia());
        assertEquals("Setor de corte", salvo.local());
        assertEquals("Corte", salvo.tipo());
        assertEquals("ACIDENTE", salvo.tipoOcorrencia());
        assertEquals(1, repository.listar().size());
    }

    @Test
    void deveRegistrarIncidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository);

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
        assertEquals(1, repository.listar().size());
    }

    @Test
    void deveListarOcorrenciasRegistradas() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaUseCase useCase = new OcorrenciaUseCase(repository);

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
                "Falha no procedimento",
                "Prensamento",
                "Lesao no dedo",
                "ACD-2026-002",
                "Ambulatorio"));

        List<OcorrenciaOutputDTO> ocorrencias = useCase.listar();

        assertEquals(2, ocorrencias.size());
        assertEquals("INCIDENTE", ocorrencias.get(0).tipoOcorrencia());
        assertEquals("ACIDENTE", ocorrencias.get(1).tipoOcorrencia());
    }

    private static class OcorrenciaRepositoryFake implements OcorrenciaRepositoryPort {

        private final List<Ocorrencia> ocorrencias = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public Acidente salvarAcidente(Acidente acidente) {
            Acidente salvo = new Acidente(
                    proximoId++,
                    acidente.getDataOcorrencia(),
                    acidente.getLocal(),
                    acidente.getDescricao(),
                    acidente.getPlanoDeAcao(),
                    acidente.getCausaRaiz(),
                    acidente.getTipo(),
                    acidente.getDano(),
                    acidente.getNumeroProtocolo(),
                    acidente.getDestino());
            ocorrencias.add(salvo);
            return salvo;
        }

        @Override
        public Incidente salvarIncidente(Incidente incidente) {
            Incidente salvo = new Incidente(
                    proximoId++,
                    incidente.getDataOcorrencia(),
                    incidente.getLocal(),
                    incidente.getDescricao(),
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
    }
}
