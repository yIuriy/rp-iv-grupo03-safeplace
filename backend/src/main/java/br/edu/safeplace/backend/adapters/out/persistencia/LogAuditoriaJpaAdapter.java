package br.edu.safeplace.backend.adapters.out.persistencia;

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
    public List<Integer> buscarIdsPorResponsavelETipoOperacao(String responsavel, String tipoOperacao) {
        if (responsavel == null || tipoOperacao == null) {
            return List.of();
        }
        return jpaRepository.buscarIdsPorResponsavelETipoOperacao(responsavel, tipoOperacao);
    }
}
