package br.edu.safeplace.backend.application.port.out;

import java.util.List;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;

public interface LogAuditoriaRepositoryPort {
    LogAuditoria salvar(LogAuditoria log);
    List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId);

    default List<Integer> buscarIdsPorResponsavelETipoOperacao(String responsavel, String tipoOperacao) {
        return List.of();
    }
}
