package br.edu.safeplace.backend.application.usecase;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AtualizarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

@Service
public class SetorService implements GerenciarSetorUseCase {

    private static final String ENTIDADE = "Setor";

    private final SetorRepository setorRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    public SetorService(SetorRepository setorRepository, LogAuditoriaRepository logAuditoriaRepository) {
        this.setorRepository = setorRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
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
        Setor salvo = setorRepository.salvar(setor);
        logAuditoriaRepository.salvar(LogAuditoria.registrarCadastro(
                operador != null ? operador : "sistema",
                ENTIDADE,
                salvo.getId(),
                "Cadastro do setor " + salvo.getNome()));
        return SetorOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public SetorOutputDTO atualizarSetor(Integer id, AtualizarSetorInputDTO inputDTO, String operador) {
        Setor existente = setorRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + id));

        Setor atualizado = existente.atualizar(
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.localizacao(),
                inputDTO.status()
        );

        Setor salvo = setorRepository.salvar(atualizado);
        logAuditoriaRepository.salvar(LogAuditoria.registrarAlteracao(
                operador != null ? operador : "sistema",
                ENTIDADE,
                salvo.getId(),
                "Atualização do setor " + salvo.getNome() + " (status: " + salvo.getStatus() + ")"));
        return SetorOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SetorOutputDTO> listar() {
        return setorRepository.listarTodos().stream()
                .map(SetorOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SetorOutputDTO buscarPorId(Integer id) {
        return setorRepository.buscarPorId(id)
                .map(SetorOutputDTO::deDominio)
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + id));
    }
}
