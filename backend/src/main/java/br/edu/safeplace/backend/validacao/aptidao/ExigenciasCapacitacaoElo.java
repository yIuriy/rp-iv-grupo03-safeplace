package br.edu.safeplace.backend.validacao.aptidao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusTreinamento;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/**
 * UC02: cada certificação ou treinamento exigido pela tarefa precisa estar válido na data de
 * referência. As pendências saem na ordem em que as exigências foram cadastradas, por isso
 * certificações e treinamentos ficam no mesmo elo.
 */
class ExigenciasCapacitacaoElo extends EloValidacao<AptidaoParaTarefa> {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected void verificar(AptidaoParaTarefa aptidao, ResultadoValidacao resultado) {
        Integer tarefaId = aptidao.tarefa().getId();
        LocalDate data = aptidao.dataReferencia();
        for (ExigenciaCapacitacao exigencia : aptidao.exigencias()) {
            if (exigencia.getAlvo() != ExigenciaCapacitacao.TipoAlvo.TAREFA
                    || !tarefaId.equals(exigencia.getAlvoId())) {
                continue;
            }
            String nome = exigencia.getNomeCapacitacao();
            if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO) {
                // UC02, exceção I: certificação obrigatória vencida ou não cadastrada impede alocação
                boolean possuiValida = aptidao.certificacoes().stream()
                        .anyMatch(c -> c.getNome().equalsIgnoreCase(nome) && c.estaValidaEm(data));
                if (!possuiValida) {
                    resultado.adicionar(Problema.de("certificação obrigatória '" + nome + "' ausente ou vencida"));
                }
            } else if (exigencia.getTipoCapacitacao() == ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO) {
                // UC02, cenário alternativo II: treinamento não realizado ou vencido impede alocação
                List<Treinamento> mesmoNome = aptidao.treinamentos().stream()
                        .filter(t -> t.getNome().equalsIgnoreCase(nome))
                        .toList();
                if (mesmoNome.stream().noneMatch(t -> t.calcularStatus(data).atendeExigencia())) {
                    resultado.adicionar(Problema.de(descreverPendenciaTreinamento(nome, mesmoNome, data)));
                }
            }
        }
    }

    private String descreverPendenciaTreinamento(String nome, List<Treinamento> mesmoNome, LocalDate data) {
        return mesmoNome.stream()
                .filter(t -> t.calcularStatus(data) == StatusTreinamento.VENCIDO)
                .flatMap(t -> t.getDataVencimento().stream())
                .max(Comparator.naturalOrder())
                .map(vencimento -> "treinamento obrigatório '" + nome + "' vencido em " + vencimento.format(DATA))
                .orElse("treinamento obrigatório '" + nome + "' pendente ou não realizado");
    }
}
