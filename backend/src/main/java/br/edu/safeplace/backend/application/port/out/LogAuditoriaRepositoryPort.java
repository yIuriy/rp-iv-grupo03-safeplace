/*
 * Porta de saída da trilha de auditoria (RF22/RNF05): grava registros, lista por entidade e localiza
 * as entidades registradas por um responsável.
 */
package br.edu.safeplace.backend.application.port.out;

import java.util.Collection;
import java.util.List;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;

public interface LogAuditoriaRepositoryPort {
    LogAuditoria salvar(LogAuditoria log);
    List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId);

    /**
     * Ids das entidades dos tipos informados (por exemplo, Acidente e Incidente) em que o responsável
     * aparece com a operação dada. Filtrar pelo tipo evita confundir o id de uma ocorrência com o de
     * um colaborador ou empréstimo cadastrado pela mesma pessoa (issue #128).
     */
    default List<Integer> buscarIdsPorResponsavelETipoOperacao(
            String responsavel, String tipoOperacao, Collection<String> entidades) {
        return List.of();
    }
}
