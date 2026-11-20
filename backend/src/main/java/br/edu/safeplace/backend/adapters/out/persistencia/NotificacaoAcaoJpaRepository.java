package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NotificacaoAcao;

/**
 * Repositório Spring Data da tabela notificacoes_acao, sobre a própria entidade de domínio (#262).
 * Sem SQL nativo: a unicidade do alerta de prazo é verificada por {@link #existsByAcaoIdAndPrazoAndMotivo}
 * e reforçada pelo índice único parcial {@code ux_notificacoes_acao_prazo}.
 */
public interface NotificacaoAcaoJpaRepository extends JpaRepository<NotificacaoAcao, Integer> {

    boolean existsByAcaoIdAndPrazoAndMotivo(Integer acaoId, LocalDate prazo, MotivoAvisoAcao motivo);

    @EntityGraph(attributePaths = {"acao", "ocorrencia"})
    List<NotificacaoAcao> findAllByOrderByCriadaEmDescIdDesc();
}
