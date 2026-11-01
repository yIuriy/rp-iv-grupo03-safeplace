package br.edu.safeplace.backend.suporte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

/**
 * Dublê da porta de auditoria para testes isolados. RNF05 (issue #193) torna a porta obrigatória
 * nos casos de uso; este dublê guarda os registros para que o teste confira o que foi auditado.
 * Não comprova imutabilidade, coberta por LogAuditoriaImutabilidadeIntegracaoTest.
 */
public class TrilhaAuditoriaEmMemoria implements LogAuditoriaRepository {

    private final List<LogAuditoria> registros = new ArrayList<>();

    @Override
    public LogAuditoria salvar(LogAuditoria log) {
        LogAuditoria salvo = new LogAuditoria(registros.size() + 1, log.getTipoOperacao(), log.getDataHora(),
                log.getResponsavel(), log.getJustificativa(), log.getEntidade(), log.getEntidadeId());
        registros.add(salvo);
        return salvo;
    }

    @Override
    public List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId) {
        return registros.stream()
                .filter(log -> Objects.equals(log.getEntidade(), entidade)
                        && Objects.equals(log.getEntidadeId(), entidadeId))
                .sorted(Comparator.comparing(LogAuditoria::getDataHora).reversed())
                .toList();
    }

    public List<LogAuditoria> registros() {
        return Collections.unmodifiableList(registros);
    }
}
