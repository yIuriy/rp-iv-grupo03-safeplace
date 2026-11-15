package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Implementa a porta de usuários delegando ao repositório JPA da própria hierarquia de domínio (#256). */
@Repository
public class UsuarioJpaAdapter implements UsuarioRepository {
    private final UsuarioJpaRepository jpaRepositorio;

    public UsuarioJpaAdapter(UsuarioJpaRepository jpaRepositorio) {
        this.jpaRepositorio = jpaRepositorio;
    }

    @Override
    public Colaborador salvar(Colaborador colaborador) {
        return jpaRepositorio.save(colaborador);
    }

    @Override
    public Optional<Colaborador> buscarPorId(Integer id) {
        return jpaRepositorio.findById(id);
    }

    @Override
    public Optional<Colaborador> buscarPorCpf(String cpf) {
        return jpaRepositorio.findByCpf(cpf);
    }

    @Override
    public Optional<Colaborador> buscarPorEmail(String email) {
        return jpaRepositorio.findByEmail(email);
    }

    @Override
    public List<Colaborador> listar() {
        return jpaRepositorio.findAll();
    }

    @Override
    public List<Colaborador> listarColaboradores(String nome, String cpf) {
        return jpaRepositorio.buscarColaboradores(nome, cpf);
    }
}
