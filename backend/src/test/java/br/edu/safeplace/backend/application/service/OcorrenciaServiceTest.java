package br.edu.safeplace.backend.application.service;

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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OcorrenciaServiceTest {

    @Test
    void deveRegistrarAcidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaService service = new OcorrenciaService(repository);

        Acidente acidente = new Acidente(
                null,
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

        Acidente salvo = service.registrarAcidente(acidente);

        assertEquals(1, salvo.getIdOcorrencia());
        assertEquals("Setor de corte", salvo.getLocal());
        assertEquals("Corte", salvo.getTipo());
        assertEquals(1, repository.listar().size());
        assertInstanceOf(Acidente.class, repository.listar().get(0));
    }

    @Test
    void deveRegistrarIncidente() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaService service = new OcorrenciaService(repository);

        Incidente incidente = new Incidente(
                null,
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

        Incidente salvo = service.registrarIncidente(incidente);

        assertEquals(1, salvo.getIdOcorrencia());
        assertEquals("Almoxarifado", salvo.getLocal());
        assertEquals("Caixas acima do limite permitido", salvo.getSituacaoRisco());
        assertEquals(1, repository.listar().size());
        assertInstanceOf(Incidente.class, repository.listar().get(0));
    }

    @Test
    void deveListarOcorrenciasRegistradas() {
        OcorrenciaRepositoryFake repository = new OcorrenciaRepositoryFake();
        OcorrenciaService service = new OcorrenciaService(repository);

        service.registrarIncidente(new Incidente(
                null,
                LocalDateTime.of(2026, 9, 6, 16, 0),
                "Corredor principal",
                "Piso molhado sem sinalizacao.",
                null,
                "Risco de queda",
                "Lesao leve"));

        service.registrarAcidente(new Acidente(
                null,
                LocalDateTime.of(2026, 9, 6, 17, 0),
                "Oficina",
                "Funcionario prensou o dedo.",
                null,
                "Falha no procedimento",
                "Prensamento",
                "Lesao no dedo",
                "ACD-2026-002",
                "Ambulatorio"));

        List<Ocorrencia> ocorrencias = service.listar();

        assertEquals(2, ocorrencias.size());
        assertInstanceOf(Incidente.class, ocorrencias.get(0));
        assertInstanceOf(Acidente.class, ocorrencias.get(1));
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
            return ocorrencias;
        }
    }
}