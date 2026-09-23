package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

@Service
public class ConsultarCapacitacaoService implements ConsultarCapacitacaoUseCase {

    private final CapacitacaoRepositoryPort capacitacaoRepository;

    public ConsultarCapacitacaoService(CapacitacaoRepositoryPort capacitacaoRepository) {
        this.capacitacaoRepository = capacitacaoRepository;
    }

    @Override
    public List<Certificacao> listarCertificacoesDoColaborador(Integer colaboradorId) {
        if (colaboradorId == null) {
            return List.of();
        }
        LocalDate hoje = LocalDate.now();
        List<Certificacao> lista = capacitacaoRepository.buscarCertificacoesPorColaborador(colaboradorId);
        lista.forEach(cert -> cert.reavaliarStatus(hoje));
        return lista;
    }

    @Override
    public List<Treinamento> listarTreinamentosDoColaborador(Integer colaboradorId) {
        if (colaboradorId == null) {
            return List.of();
        }
        return capacitacaoRepository.buscarTreinamentosPorColaborador(colaboradorId);
    }

    @Override
    public List<ExigenciaCapacitacao> listarExigenciasPorTarefa(Integer tarefaId) {
        if (tarefaId == null) {
            return List.of();
        }
        return capacitacaoRepository.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.TAREFA, tarefaId);
    }

    @Override
    public List<ExigenciaCapacitacao> listarExigenciasPorEpi(Integer epiId) {
        if (epiId == null) {
            return List.of();
        }
        return capacitacaoRepository.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.EPI, epiId);
    }

    @Override
    public PendenciasColaboradorDTO consultarPendenciasColaborador(Integer colaboradorId) {
        List<Certificacao> certificacoes = listarCertificacoesDoColaborador(colaboradorId);
        List<Treinamento> treinamentos = listarTreinamentosDoColaborador(colaboradorId);

        int totalCertificacoes = certificacoes.size();
        List<Certificacao> alertasVencimento = new ArrayList<>();
        List<Certificacao> certificacoesVencidasLista = new ArrayList<>();
        int emDia = 0;

        for (Certificacao cert : certificacoes) {
            StatusCertificacao status = cert.getStatus();
            if (status == StatusCertificacao.PROXIMA_DO_VENCIMENTO) {
                alertasVencimento.add(cert);
            } else if (status == StatusCertificacao.VENCIDA) {
                certificacoesVencidasLista.add(cert);
            } else if (status == StatusCertificacao.EM_DIA) {
                emDia++;
            }
        }

        boolean pendenteIntegracaoAdmissional = certificacoes.isEmpty() && treinamentos.isEmpty();

        return new PendenciasColaboradorDTO(
                colaboradorId,
                totalCertificacoes,
                emDia,
                alertasVencimento.size(),
                certificacoesVencidasLista.size(),
                treinamentos.size(),
                pendenteIntegracaoAdmissional,
                alertasVencimento,
                certificacoesVencidasLista,
                treinamentos
        );
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
        LocalDate hoje = LocalDate.now();

        for (ExigenciaCapacitacao exigencia : exigencias) {
            String nomeExigido = exigencia.getNomeCapacitacao();
            if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO) {
                boolean possuiValida = certificacoes.stream()
                        .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeExigido)
                                && (c.getDataVencimento().isAfter(hoje) || c.getDataVencimento().isEqual(hoje)));
                if (!possuiValida) {
                    impedimentos.add("certificação " + nomeExigido + " ausente ou vencida");
                }
            } else if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO) {
                boolean realizou = treinamentos.stream()
                        .anyMatch(t -> t.getNome().equalsIgnoreCase(nomeExigido)
                                && "REALIZADO".equals(t.verificarStatus()));
                if (!realizou) {
                    impedimentos.add("treinamento " + nomeExigido + " não registrado");
                }
            }
        }

        return impedimentos;
    }
}
