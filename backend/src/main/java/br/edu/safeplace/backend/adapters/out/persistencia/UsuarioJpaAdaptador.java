package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioJpaAdaptador implements UsuarioRepositorioPorta {
    private final UsuarioJpaRepositorio jpaRepositorio;

    public UsuarioJpaAdaptador(UsuarioJpaRepositorio jpaRepositorio) {
        this.jpaRepositorio = jpaRepositorio;
    }

    @Override
    public Colaborador salvar(Colaborador colaborador) {
        ColaboradorEntidade entidade = ColaboradorEntidade.deDominio(colaborador);
        ColaboradorEntidade salvo = jpaRepositorio.save(entidade);
        return salvo.paraDominio();
    }

    @Override
    public Optional<Colaborador> buscarPorId(Integer id) {
        return jpaRepositorio.findById(id).map(ColaboradorEntidade::paraDominio);
    }

    @Override
    public Optional<Colaborador> buscarPorCpf(String cpf) {
        return jpaRepositorio.findByCpf(cpf).map(ColaboradorEntidade::paraDominio);
    }

    @Override
    public Optional<Colaborador> buscarPorEmail(String email) {
        return jpaRepositorio.findByEmail(email).map(ColaboradorEntidade::paraDominio);
    }

    @Override
    public List<Colaborador> listarTodos() {
        return jpaRepositorio.findAll().stream().map(ColaboradorEntidade::paraDominio).toList();
    }
}
