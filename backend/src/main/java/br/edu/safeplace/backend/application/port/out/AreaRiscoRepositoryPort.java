package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public interface AreaRiscoRepositoryPort {

    AreaRisco salvar(AreaRisco areaRisco);

    Optional<AreaRisco> buscarPorId(Integer id);

    Optional<AreaRisco> buscarPorCodigo(String codigo);

    List<AreaRisco> listarTodas();

    List<AreaRisco> listarPorNivelPerigo(NivelPerigo nivelPerigo);
}
