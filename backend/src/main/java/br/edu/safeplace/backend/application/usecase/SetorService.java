package br.edu.safeplace.backend.application.usecase;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AtualizarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarSetorUseCase;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.setor.SetorValidador;

@Service
public class SetorService implements GerenciarSetorUseCase {

    private static final String ENTIDADE = "Setor";

    private final SetorRepository setorRepository;
    private final RegistradorAuditoria auditoria;
    private final Validador<Setor> validador;

    @Autowired
    public SetorService(SetorRepository setorRepository, RegistradorAuditoria auditoria,
            Validador<Setor> validador) {
        this.setorRepository = setorRepository;
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        this.validador = Objects.requireNonNull(validador, "Validador do setor é obrigatório.");
    }

    /** Usa o validador padrão; usado pelos testes sem contexto Spring. */
    public SetorService(SetorRepository setorRepository, LogAuditoriaRepository logAuditoriaRepository) {
        this(setorRepository, new RegistradorAuditoria(logAuditoriaRepository), new SetorValidador());
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
        // Chain of Responsibility (#263): valida antes do repository.
        validador.validar(setor);
        Setor salvo = setorRepository.salvar(setor);
        auditoria.por(operador).cadastro(
                ENTIDADE,
                salvo.getId(),
                "Cadastro do setor " + salvo.getNome());
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

        validador.validar(atualizado);
        Setor salvo = setorRepository.salvar(atualizado);
        auditoria.por(operador).alteracao(
                ENTIDADE,
                salvo.getId(),
                "Atualização do setor " + salvo.getNome() + " (status: " + salvo.getStatus() + ")");
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
