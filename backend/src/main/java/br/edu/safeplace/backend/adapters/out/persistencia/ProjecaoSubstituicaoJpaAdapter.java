package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepositoryPort;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;

@Repository
public class ProjecaoSubstituicaoJpaAdapter implements ProjecaoSubstituicaoRepositoryPort {

    private final ProjecaoSubstituicaoJpaRepository repositorio;

    public ProjecaoSubstituicaoJpaAdapter(ProjecaoSubstituicaoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ProjecaoSubstituicao salvar(ProjecaoSubstituicao projecao) {
        ProjecaoSubstituicaoEntity entity = paraEntidade(projecao);
        return paraDominio(repositorio.save(entity));
    }

    @Override
    public List<ProjecaoSubstituicao> salvarTodas(List<ProjecaoSubstituicao> projecoes) {
        List<ProjecaoSubstituicaoEntity> entidades = projecoes.stream().map(this::paraEntidade).toList();
        return repositorio.saveAll(entidades).stream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<ProjecaoSubstituicao> buscarUltimaPorEpi(Integer epiId) {
        return repositorio.findFirstByEpiIdOrderByDataProjecaoDescIdDesc(epiId).map(this::paraDominio);
    }

    @Override
    public List<ProjecaoSubstituicao> listarPorEpi(Integer epiId) {
        return repositorio.findByEpiIdOrderByDataProjecaoDescIdDesc(epiId).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public List<ProjecaoSubstituicao> listarTodas() {
        return repositorio.findAllByOrderByDataProjecaoDescIdDesc().stream()
                .map(this::paraDominio)
                .toList();
    }

    private ProjecaoSubstituicaoEntity paraEntidade(ProjecaoSubstituicao dominio) {
        return new ProjecaoSubstituicaoEntity(
                dominio.getId(),
                dominio.getEpiId(),
                dominio.getDataProjecao(),
                dominio.getDataEstimadaDescarte(),
                dominio.getJustificativa()
        );
    }

    private ProjecaoSubstituicao paraDominio(ProjecaoSubstituicaoEntity entity) {
        return ProjecaoSubstituicao.reconstituir(
                entity.getId(),
                entity.getEpiId(),
                entity.getDataProjecao(),
                entity.getDataEstimadaDescarte(),
                entity.getJustificativa()
        );
    }
}
