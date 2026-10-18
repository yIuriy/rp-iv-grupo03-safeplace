package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;

/**
 * Porta de saída para persistência e consulta de planos de ação (ações),
 * responsáveis e histórico de prorrogações (RF07, RNF05, UC07, US07, Issue #98).
 */
public interface PlanoDeAcaoRepositoryPort {

    /**
     * Salva uma ação do plano, registrando auditoria correspondente.
     */
    PlanoDeAcao salvar(PlanoDeAcao plano);

    /**
     * Busca uma ação pelo seu identificador (idAcao / idPlano).
     */
    Optional<PlanoDeAcao> buscarPorId(Integer id);

    /**
     * Lista todas as ações vinculadas a uma ocorrência específica.
     */
    List<PlanoDeAcao> listarPorOcorrencia(Integer idOcorrencia);

    /**
     * Salva o registro estruturado de uma prorrogação de prazo aceita com justificativa,
     * incluindo registro no log de auditoria.
     */
    ProrrogacaoPrazo salvarProrrogacao(ProrrogacaoPrazo prorrogacao);

    /**
     * Lista o histórico de prorrogações de prazo de uma ação.
     */
    List<ProrrogacaoPrazo> listarProrrogacoes(Integer idAcao);
}
