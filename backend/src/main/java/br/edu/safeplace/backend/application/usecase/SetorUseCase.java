package br.edu.safeplace.backend.application.usecase;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

@Service
public class SetorUseCase implements GerenciarSetorUseCase {

    private static final String ENTIDADE = "Setor";

    private final SetorRepositoryPort setorRepositoryPort;
    private final LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;

    public SetorUseCase(SetorRepositoryPort setorRepositoryPort, LogAuditoriaRepositoryPort logAuditoriaRepositoryPort) {
        this.setorRepositoryPort = setorRepositoryPort;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepositoryPort = Objects.requireNonNull(
                logAuditoriaRepositoryPort, "Porta de auditoria é obrigatória (RNF05).");
    }

    @Override
    @Transactional
    public SetorOutputDTO cadastrarSetor(CadastrarSetorInputDTO inputDTO, String operador) {
        Setor setor = Setor.novo(
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.localizacao(),
                inputDTO.status()
        );
        Setor salvo = setorRepositoryPort.salvar(setor);
        logAuditoriaRepositoryPort.salvar(LogAuditoria.registrarCadastro(
                operador != null ? operador : "sistema",
                ENTIDADE,
                salvo.getId(),
                "Cadastro do setor " + salvo.getNome()));
        return SetorOutputDTO.deDominio(salvo);
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
