/*
 * Adaptador JPA da trilha de auditoria: converte LogAuditoria em entidade, grava, lista por entidade e
 * localiza ids registrados por um responsável, filtrando pelo tipo de entidade.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

@Component
public class LogAuditoriaJpaAdapter implements LogAuditoriaRepositoryPort {

    private final LogAuditoriaJpaRepository jpaRepository;

    public LogAuditoriaJpaAdapter(LogAuditoriaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LogAuditoria salvar(LogAuditoria log) {
        LogAuditoriaEntity salvo = jpaRepository.save(LogAuditoriaEntity.deDominio(log));
        return salvo.paraDominio();
    }

    @Override
    public List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId) {
        return jpaRepository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc(entidade, entidadeId).stream()
                .map(LogAuditoriaEntity::paraDominio)
                .toList();
    }

    @Override
    public List<Integer> buscarIdsPorResponsavelETipoOperacao(
            String responsavel, String tipoOperacao, Collection<String> entidades) {
        if (responsavel == null || tipoOperacao == null || entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        return jpaRepository.buscarIdsPorResponsavelETipoOperacao(responsavel, tipoOperacao, entidades);
    }
}
