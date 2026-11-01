package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;

/**
 * Porta de saída para persistência e consulta das projeções de substituição de EPIs (RF21, UC10).
 */
public interface ProjecaoSubstituicaoRepository {

    ProjecaoSubstituicao salvar(ProjecaoSubstituicao projecao);

    List<ProjecaoSubstituicao> salvarTodas(List<ProjecaoSubstituicao> projecoes);

    Optional<ProjecaoSubstituicao> buscarUltimaPorEpi(Integer epiId);

    List<ProjecaoSubstituicao> listarPorEpi(Integer epiId);

    List<ProjecaoSubstituicao> listarTodas();
}
