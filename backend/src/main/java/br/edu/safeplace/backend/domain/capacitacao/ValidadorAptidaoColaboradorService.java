package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Serviço de domínio puro responsável por aplicar as regras normativas de segurança e
 * validar a aptidão de um colaborador para alocação em tarefas de risco (RF06, RF10, UC02, UC11, US06, US10).
 */
public class ValidadorAptidaoColaboradorService {

    /**
     * Valida de forma impeditiva se o colaborador pode ser alocado na tarefa.
     *
     * @param colaborador Colaborador a ser alocado.
     * @param tarefa Tarefa alvo da alocação.
     * @param certificacoes Certificações do colaborador.
     * @param treinamentos Treinamentos realizados/vinculados ao colaborador.
     * @param exigencias Exigências de capacitação cadastradas para a tarefa.
     * @param dataReferencia Data base para verificação cronológica (hoje).
     * @throws br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException se a tarefa não for classificada.
     * @throws AlocacaoBloqueadaException se houver certificações vencidas ou treinamentos pendentes.
     */
    public void validarAptidaoParaTarefa(Colaborador colaborador, Tarefa tarefa,
                                         List<Certificacao> certificacoes,
                                         List<Treinamento> treinamentos,
                                         List<ExigenciaCapacitacao> exigencias,
                                         LocalDate dataReferencia) {
        if (colaborador == null) {
            throw new IllegalArgumentException("Colaborador é obrigatório para validação de aptidão.");
        }
        if (tarefa == null) {
            throw new IllegalArgumentException("Tarefa é obrigatória para validação de aptidão.");
        }

        // 1. UC11 Exceção I / US06 Regra 3: Tarefa sem classificação não aceita alocação
        tarefa.validarClassificacaoParaAlocacao();

        LocalDate dataBase = dataReferencia != null ? dataReferencia : LocalDate.now();
        List<String> impedimentos = verificarPendenciasNormativas(tarefa.getId(), certificacoes, treinamentos, exigencias, dataBase);

        if (!impedimentos.isEmpty()) {
            throw new AlocacaoBloqueadaException(impedimentos);
        }
    }

    /**
     * Consulta as pendências normativas para fins informativos (ex.: consulta prévia de aptidão).
     * Retorna a lista descritiva dos impedimentos sem lançar exceção.
     */
    public List<String> obterImpedimentos(Colaborador colaborador, Tarefa tarefa,
                                          List<Certificacao> certificacoes,
                                          List<Treinamento> treinamentos,
                                          List<ExigenciaCapacitacao> exigencias,
                                          LocalDate dataReferencia) {
        List<String> impedimentos = new ArrayList<>();
        if (tarefa == null) {
            impedimentos.add("tarefa não informada");
            return impedimentos;
        }
        if (colaborador == null) {
            impedimentos.add("colaborador não informado");
            return impedimentos;
        }

        if (!tarefa.estaClassificada()) {
            impedimentos.add("tarefa '" + tarefa.getDescricao() + "' sem grau de periculosidade classificado");
        }

        LocalDate dataBase = dataReferencia != null ? dataReferencia : LocalDate.now();
        impedimentos.addAll(verificarPendenciasNormativas(tarefa.getId(), certificacoes, treinamentos, exigencias, dataBase));
        return Collections.unmodifiableList(impedimentos);
    }

    private List<String> verificarPendenciasNormativas(Integer tarefaId,
                                                      List<Certificacao> certificacoes,
                                                      List<Treinamento> treinamentos,
                                                      List<ExigenciaCapacitacao> exigencias,
                                                      LocalDate dataBase) {
        List<String> impedimentos = new ArrayList<>();
        if (exigencias == null || exigencias.isEmpty()) {
            return impedimentos;
        }

        List<Certificacao> certs = certificacoes != null ? certificacoes : Collections.emptyList();
        List<Treinamento> treinos = treinamentos != null ? treinamentos : Collections.emptyList();

        for (ExigenciaCapacitacao exigencia : exigencias) {
            if (exigencia.getAlvo() != ExigenciaCapacitacao.TipoAlvo.TAREFA
                    || !tarefaId.equals(exigencia.getAlvoId())) {
                continue;
            }

            String nomeExigido = exigencia.getNomeCapacitacao();

            if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO) {
                // UC02 Exceção I: certificação obrigatória vencida ou não cadastrada impede alocação
                boolean possuiValida = certs.stream()
                        .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeExigido)
                                && (c.getDataVencimento().isAfter(dataBase) || c.getDataVencimento().isEqual(dataBase)));
                if (!possuiValida) {
                    impedimentos.add("certificação obrigatória '" + nomeExigido + "' ausente ou vencida");
                }
            } else if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO) {
                // UC02 Cenário Alternativo II: pendência de treinamento não realizado impede alocação
                boolean realizou = treinos.stream()
                        .anyMatch(t -> t.getNome().equalsIgnoreCase(nomeExigido)
                                && "REALIZADO".equals(t.verificarStatus())
                                && !t.getDataRealizacao().isAfter(dataBase));
                if (!realizou) {
                    impedimentos.add("treinamento obrigatório '" + nomeExigido + "' pendente ou não realizado");
                }
            }
        }

        return impedimentos;
    }
}
