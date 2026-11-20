package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepository;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;

/**
 * Adaptador JPA da porta ProjecaoSubstituicaoRepository: delega ao repositório JPA da própria entidade
 * de domínio ProjecaoSubstituicao (#259), sem classe espelhada.
 */
@Repository
public class ProjecaoSubstituicaoJpaAdapter implements ProjecaoSubstituicaoRepository {

    private final ProjecaoSubstituicaoJpaRepository repositorio;

    public ProjecaoSubstituicaoJpaAdapter(ProjecaoSubstituicaoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ProjecaoSubstituicao salvar(ProjecaoSubstituicao projecao) {
        return repositorio.save(projecao);
    }

    @Override
    public List<ProjecaoSubstituicao> salvar(List<ProjecaoSubstituicao> projecoes) {
        return repositorio.saveAll(projecoes);
    }

    @Override
    public Optional<ProjecaoSubstituicao> buscarUltimaPorEpi(Integer epiId) {
        return repositorio.findFirstByEpiCodigoEPIOrderByDataProjecaoDescIdDesc(epiId);
    }

    @Override
    public List<ProjecaoSubstituicao> listarPorEpi(Integer epiId) {
        return repositorio.findByEpiCodigoEPIOrderByDataProjecaoDescIdDesc(epiId);
    }

    @Override
    public List<ProjecaoSubstituicao> listar() {
        return repositorio.findAllByOrderByDataProjecaoDescIdDesc();
    }
}
