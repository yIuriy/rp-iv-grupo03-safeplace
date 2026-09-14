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

    /**
     * Lista somente as pessoas com perfil Colaborador (excluindo Supervisor e Gestor de
     * Segurança), aplicando os filtros informados. Os parâmetros nulos são ignorados; o nome é
     * comparado por conteúdo, sem diferenciar maiúsculas, e o CPF por igualdade.
     */
    List<Colaborador> listarColaboradores(String nome, String cpf);
}
