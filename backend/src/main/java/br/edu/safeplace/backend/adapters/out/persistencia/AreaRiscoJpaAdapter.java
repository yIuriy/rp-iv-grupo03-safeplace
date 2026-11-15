package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/** Delega ao repositório JPA da AreaRisco; os EPIs obrigatórios são associação da própria entidade (#258). */
@Repository
public class AreaRiscoJpaAdapter implements AreaRiscoRepository {

    private final AreaRiscoJpaRepository areaRiscoRepository;

    public AreaRiscoJpaAdapter(AreaRiscoJpaRepository areaRiscoRepository) {
        this.areaRiscoRepository = areaRiscoRepository;
    }

    @Override
    public AreaRisco salvar(AreaRisco areaRisco) {
        return areaRiscoRepository.save(areaRisco);
    }

    @Override
    public AreaRisco atualizar(AreaRisco areaRisco) {
        return salvar(areaRisco);
    }

    @Override
    public Optional<AreaRisco> buscarPorId(Integer id) {
        return areaRiscoRepository.findById(id);
    }

    @Override
    public Optional<AreaRisco> buscarPorCodigo(String codigo) {
        return areaRiscoRepository.findByCodigo(codigo);
    }

    @Override
    public List<AreaRisco> listar() {
        return areaRiscoRepository.findAll();
    }

    @Override
    public List<AreaRisco> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        return areaRiscoRepository.findByNivelPerigo(nivelPerigo);
    }
}
