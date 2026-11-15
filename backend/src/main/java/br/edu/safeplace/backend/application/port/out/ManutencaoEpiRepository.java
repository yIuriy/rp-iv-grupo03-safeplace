package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;

import java.util.List;
import java.util.Optional;

public interface ManutencaoEpiRepository {
    ManutencaoEpi salvar(ManutencaoEpi manutencao);
    Optional<ManutencaoEpi> buscarPorId(Integer id);
    List<ManutencaoEpi> listarPorEpi(Integer epiId);
}
