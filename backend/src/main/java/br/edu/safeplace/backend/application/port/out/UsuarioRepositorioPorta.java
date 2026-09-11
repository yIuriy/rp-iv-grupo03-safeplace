package br.edu.safeplace.backend.application.port.out;

import br.edu.safeplace.backend.domain.usuario.Colaborador;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorioPorta {
    Colaborador salvar(Colaborador colaborador);
    Optional<Colaborador> buscarPorId(Integer id);
    Optional<Colaborador> buscarPorCpf(String cpf);
    Optional<Colaborador> buscarPorEmail(String email);
    List<Colaborador> listarTodos();
}
