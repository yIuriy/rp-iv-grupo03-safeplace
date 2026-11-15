package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;

@Component
public class SetorJpaAdapter implements SetorRepository {

    private final SetorJpaRepository jpaRepository;

    public SetorJpaAdapter(SetorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Setor salvar(Setor setor) {
        return jpaRepository.save(setor);
    }

    @Override
    public Optional<Setor> buscarPorId(Integer id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Setor> listar() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Setor> buscarPorNome(String nome) {
        return jpaRepository.findByNomeIgnoreCase(nome);
    }
}
