package br.edu.safeplace.backend.application.port.out;

import java.util.List;
import java.util.Optional;

import br.edu.safeplace.backend.domain.area_risco.Setor;

public interface SetorRepository {
    Setor salvar(Setor setor);
    Optional<Setor> buscarPorId(Integer id);
    List<Setor> listar();
    Optional<Setor> buscarPorNome(String nome);
}
