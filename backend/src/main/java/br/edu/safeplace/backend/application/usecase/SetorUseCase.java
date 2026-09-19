package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.Setor;

@Service
public class SetorUseCase implements GerenciarSetorUseCase {

    private final SetorRepositoryPort setorRepositoryPort;

    public SetorUseCase(SetorRepositoryPort setorRepositoryPort) {
        this.setorRepositoryPort = setorRepositoryPort;
    }

    @Override
    @Transactional
    public SetorOutputDTO cadastrarSetor(CadastrarSetorInputDTO inputDTO) {
        Setor setor = Setor.novo(
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.localizacao(),
                inputDTO.status()
        );
        return SetorOutputDTO.deDominio(setorRepositoryPort.salvar(setor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SetorOutputDTO> listar() {
        return setorRepositoryPort.listarTodos().stream()
                .map(SetorOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SetorOutputDTO buscarPorId(Integer id) {
        return setorRepositoryPort.buscarPorId(id)
                .map(SetorOutputDTO::deDominio)
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + id));
    }
}
