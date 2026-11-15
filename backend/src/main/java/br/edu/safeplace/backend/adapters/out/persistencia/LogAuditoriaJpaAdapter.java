/*
 * Adaptador JPA da trilha de auditoria: grava o LogAuditoria, que é a própria entidade (#256), lista por entidade,
 * localiza ids registrados por um responsável, filtrando pelo tipo de entidade, e lista as ações
 * recentes de um responsável.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

@Component
public class LogAuditoriaJpaAdapter implements LogAuditoriaRepository {

    private final LogAuditoriaJpaRepository jpaRepository;

    public LogAuditoriaJpaAdapter(LogAuditoriaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LogAuditoria salvar(LogAuditoria log) {
        return jpaRepository.save(log);
    }

    @Override
    public List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId) {
        return jpaRepository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc(entidade, entidadeId);
    }

    @Override
    public List<Integer> buscarIdsPorResponsavel(
            String responsavel, String tipoOperacao, Collection<String> entidades) {
        if (responsavel == null || tipoOperacao == null || entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        return jpaRepository.buscarIdsPorResponsavelETipoOperacao(responsavel, tipoOperacao, entidades);
    }

    @Override
    public List<LogAuditoria> listarPorResponsavel(String responsavel, int limite) {
        if (responsavel == null || responsavel.isBlank() || limite < 1) {
            return List.of();
        }
        return jpaRepository.findByResponsavelOrderByDataHoraDescIdDesc(responsavel, PageRequest.of(0, limite));
    }
}
