package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.Setor;

@Component
public class SetorJpaAdapter implements SetorRepositoryPort {

    private final SetorJpaRepository jpaRepository;

    public SetorJpaAdapter(SetorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Setor salvar(Setor setor) {
        SetorEntity salvo = jpaRepository.save(SetorEntity.deDominio(setor));
        return salvo.paraDominio();
    }

    @Override
    public Optional<Setor> buscarPorId(Integer id) {
        return jpaRepository.findById(id).map(SetorEntity::paraDominio);
    }

    @Override
    public List<Setor> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(SetorEntity::paraDominio)
                .toList();
    }

    @Override
    public Optional<Setor> buscarPorNome(String nome) {
        return jpaRepository.findByNomeIgnoreCase(nome).map(SetorEntity::paraDominio);
    }
}
