package br.edu.safeplace.backend.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

@Service
@Transactional(readOnly = true)
public class ConsultarCapacitacaoService implements ConsultarCapacitacaoUseCase {

    private final CapacitacaoRepository capacitacaoRepository;
    private final Clock clock;

    public ConsultarCapacitacaoService(CapacitacaoRepository capacitacaoRepository) {
        this(capacitacaoRepository, Clock.systemDefaultZone());
    }

    @Autowired
    public ConsultarCapacitacaoService(CapacitacaoRepository capacitacaoRepository, Clock clock) {
        this.capacitacaoRepository = capacitacaoRepository;
        this.clock = java.util.Objects.requireNonNull(clock, "Clock é obrigatório.");
    }

    @Override
    public List<Certificacao> listarCertificacoesDoColaborador(Integer colaboradorId) {
        if (colaboradorId == null) {
            return List.of();
        }
        LocalDate hoje = LocalDate.now(clock);
        List<Certificacao> lista = capacitacaoRepository.listarCertificacoes(colaboradorId);
        lista.forEach(cert -> cert.reavaliarStatus(hoje));
        return lista;
    }

    @Override
    public List<Treinamento> listarTreinamentosDoColaborador(Integer colaboradorId) {
        if (colaboradorId == null) {
            return List.of();
        }
        return capacitacaoRepository.listarTreinamentos(colaboradorId);
    }

    @Override
    public List<ExigenciaCapacitacao> listarExigenciasPorTarefa(Integer tarefaId) {
        if (tarefaId == null) {
            return List.of();
        }
        return capacitacaoRepository.listarExigencias(ExigenciaCapacitacao.TipoAlvo.TAREFA, tarefaId);
    }

    @Override
    public List<ExigenciaCapacitacao> listarExigenciasPorEpi(Integer epiId) {
        if (epiId == null) {
            return List.of();
        }
        return capacitacaoRepository.listarExigencias(ExigenciaCapacitacao.TipoAlvo.EPI, epiId);
    }

    @Override
    public PendenciasColaboradorDTO consultarPendenciasColaborador(Integer colaboradorId) {
        List<Certificacao> certificacoes = listarCertificacoesDoColaborador(colaboradorId);
        List<Treinamento> treinamentos = listarTreinamentosDoColaborador(colaboradorId);
        return PendenciasColaboradorDTO.avaliar(colaboradorId, certificacoes, treinamentos);
    }

    @Override
    public List<String> verificarImpedimentosParaEpi(Integer colaboradorId, Integer epiId) {
        if (colaboradorId == null || epiId == null) {
            return List.of("identificadores de colaborador e EPI são obrigatórios");
        }

        List<ExigenciaCapacitacao> exigencias = listarExigenciasPorEpi(epiId);
        if (exigencias.isEmpty()) {
            return List.of();
        }

        List<Certificacao> certificacoes = listarCertificacoesDoColaborador(colaboradorId);
        List<Treinamento> treinamentos = listarTreinamentosDoColaborador(colaboradorId);

        return avaliarExigencias(exigencias, certificacoes, treinamentos);
    }

    @Override
    public List<String> verificarImpedimentosParaTarefa(Integer colaboradorId, Integer tarefaId) {
        if (colaboradorId == null || tarefaId == null) {
            return List.of("identificadores de colaborador e tarefa são obrigatórios");
        }

        List<ExigenciaCapacitacao> exigencias = listarExigenciasPorTarefa(tarefaId);
        if (exigencias.isEmpty()) {
            return List.of();
        }

        List<Certificacao> certificacoes = listarCertificacoesDoColaborador(colaboradorId);
        List<Treinamento> treinamentos = listarTreinamentosDoColaborador(colaboradorId);

        return avaliarExigencias(exigencias, certificacoes, treinamentos);
    }

    @Override
    public boolean colaboradorPossuiPendencias(Integer colaboradorId, Integer tarefaId) {
        return !verificarImpedimentosParaTarefa(colaboradorId, tarefaId).isEmpty();
    }

    private List<String> avaliarExigencias(List<ExigenciaCapacitacao> exigencias,
                                          List<Certificacao> certificacoes,
                                          List<Treinamento> treinamentos) {
        List<String> impedimentos = new ArrayList<>();
        LocalDate hoje = LocalDate.now(clock);

        for (ExigenciaCapacitacao exigencia : exigencias) {
            String nomeExigido = exigencia.getNomeCapacitacao();
            if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO) {
                boolean possuiValida = certificacoes.stream()
                        .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeExigido) && c.estaValidaEm(hoje));
                if (!possuiValida) {
                    impedimentos.add("certificação " + nomeExigido + " ausente ou vencida");
                }
            } else if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO) {
                boolean realizou = treinamentos.stream()
                        .anyMatch(t -> t.getNome().equalsIgnoreCase(nomeExigido)
                                && t.calcularStatus(hoje).atendeExigencia());
                if (!realizou) {
                    impedimentos.add("treinamento " + nomeExigido + " não registrado");
                }
            }
        }

        return impedimentos;
    }

    @Override
    public List<Certificacao> listarTodasCertificacoes() {
        LocalDate hoje = LocalDate.now(clock);
        List<Certificacao> lista = capacitacaoRepository.listarCertificacoes();
        lista.forEach(cert -> cert.reavaliarStatus(hoje));
        return lista;
    }

    @Override
    public List<Treinamento> listarTodosTreinamentos() {
        return capacitacaoRepository.listarTreinamentos();
    }

    @Override
    public List<Integer> listarColaboradoresPorStatusCertificacao(StatusCertificacao status) {
        if (status == null) {
            return List.of();
        }
        List<Certificacao> todas = listarTodasCertificacoes();
        java.util.Map<Integer, List<Certificacao>> porColaborador = todas.stream()
                .collect(java.util.stream.Collectors.groupingBy(Certificacao::getColaboradorId));

        List<Integer> resultado = new ArrayList<>();
        for (java.util.Map.Entry<Integer, List<Certificacao>> entry : porColaborador.entrySet()) {
            Integer colabId = entry.getKey();
            List<Certificacao> certs = entry.getValue();

            boolean temVencida = certs.stream().anyMatch(c -> c.getStatus() == StatusCertificacao.VENCIDA);
            boolean temProxima = certs.stream().anyMatch(c -> c.getStatus() == StatusCertificacao.PROXIMA_DO_VENCIMENTO);
            boolean todasEmDia = certs.stream().allMatch(c -> c.getStatus() == StatusCertificacao.EM_DIA);

            if (status == StatusCertificacao.VENCIDA && temVencida) {
                resultado.add(colabId);
            } else if (status == StatusCertificacao.PROXIMA_DO_VENCIMENTO && temProxima && !temVencida) {
                resultado.add(colabId);
            } else if (status == StatusCertificacao.EM_DIA && todasEmDia && !certs.isEmpty()) {
                resultado.add(colabId);
            }
        }
        resultado.sort(Integer::compareTo);
        return java.util.Collections.unmodifiableList(resultado);
    }
}
