package br.edu.safeplace.backend.validacao.aptidao;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Tudo o que a validação de aptidão precisa para decidir se o colaborador pode ser alocado na
 * tarefa (RF06, RF10, UC02, UC11). Substitui os 6 parâmetros soltos do antigo
 * {@code ValidadorAptidaoColaboradorService}.
 *
 * @param dataReferencia data das verificações de vencimento; nula vale como hoje
 */
public record AptidaoParaTarefa(Colaborador colaborador, Tarefa tarefa,
                                List<Certificacao> certificacoes,
                                List<Treinamento> treinamentos,
                                List<ExigenciaCapacitacao> exigencias,
                                LocalDate dataReferencia) {

    public AptidaoParaTarefa {
        certificacoes = certificacoes != null ? List.copyOf(certificacoes) : List.of();
        treinamentos = treinamentos != null ? List.copyOf(treinamentos) : List.of();
        exigencias = exigencias != null ? List.copyOf(exigencias) : List.of();
        dataReferencia = dataReferencia != null ? dataReferencia : LocalDate.now();
    }
}
