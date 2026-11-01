/*
 * Caso de uso das áreas de risco (RF05 / UC03 / US05): cadastro, atualização com histórico de valor
 * anterior e novo em LogAuditoria, consultas e leitura desse histórico pelo Gestor (issue #114).
 */
package br.edu.safeplace.backend.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AreaRiscoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarAreaRiscoUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.AlteracaoAreaRisco;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.arearisco.AreaRiscoValidador;

/**
 * Mapeamento das áreas de risco do ambiente de trabalho (RF05 / UC03 / US05).
 */
@Service
public class AreaRiscoService implements GerenciarAreaRiscoUseCase {

    private static final String ENTIDADE = "AreaRisco";

    private final AreaRiscoRepository areaRiscoRepository;
    private final EpiRepository epiRepository;
    private final SetorRepository setorRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final Validador<AreaRisco> validador;

    @Autowired
    public AreaRiscoService(
            AreaRiscoRepository areaRiscoRepository,
            EpiRepository epiRepository,
            SetorRepository setorRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            Validador<AreaRisco> validador) {
        this.areaRiscoRepository = areaRiscoRepository;
        this.epiRepository = epiRepository;
        this.setorRepository = setorRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        this.validador = Objects.requireNonNull(validador, "Validador da área de risco é obrigatório.");
    }

    /** Monta o validador com as portas informadas; usado pelos testes sem contexto Spring. */
    public AreaRiscoService(
            AreaRiscoRepository areaRiscoRepository,
            EpiRepository epiRepository,
            SetorRepository setorRepository,
            LogAuditoriaRepository logAuditoriaRepository) {
        this(areaRiscoRepository, epiRepository, setorRepository, logAuditoriaRepository,
                new AreaRiscoValidador(areaRiscoRepository, setorRepository));
    }

    @Override
    @Transactional
    public AreaRiscoOutputDTO cadastrarAreaRisco(CadastrarAreaRiscoInputDTO inputDTO, String operador) {
        List<Epi> episObrigatorios = resolverEpis(inputDTO.episObrigatoriosIds());

        AreaRisco novaArea = AreaRisco.cadastrar(
                inputDTO.codigo(),
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.nivelPerigo(),
                inputDTO.setorId(),
                inputDTO.agentesRisco(),
                inputDTO.limites(),
                episObrigatorios);
        // Chain of Responsibility (#263): código único, setor existente e campos, antes do repository.
        validador.validar(novaArea);

        AreaRisco salva = areaRiscoRepository.salvar(novaArea);
        logAuditoriaRepository.salvar(LogAuditoria.registrarCadastro(
                operador != null ? operador : "sistema",
                ENTIDADE,
                salva.getId(),
                "Cadastro da área de risco " + salva.getCodigo() + " - " + salva.getNome()));

        return AreaRiscoOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional
    public AreaRiscoOutputDTO atualizarAreaRisco(Integer id, AtualizarAreaRiscoInputDTO inputDTO, String operador) {
        AreaRisco existente = areaRiscoRepository.buscarPorId(id)
                .orElseThrow(() -> new AreaRiscoNaoEncontradaException(id));

        List<Epi> episObrigatorios = resolverEpis(inputDTO.episObrigatoriosIds());

        AreaRisco atualizada = existente.atualizar(
                inputDTO.nome(),
                inputDTO.descricao(),
                inputDTO.nivelPerigo(),
                inputDTO.setorId(),
                inputDTO.agentesRisco(),
                inputDTO.limites(),
                episObrigatorios);
        validador.validar(atualizada);

        List<AlteracaoAreaRisco> alteracoes = existente.compararCom(atualizada, this::descreverSetor);
        AreaRisco salva = areaRiscoRepository.atualizar(atualizada);

        logAuditoriaRepository.salvar(LogAuditoria.registrarAlteracao(
                operador != null ? operador : "sistema",
                ENTIDADE,
                id,
                descreverHistorico(alteracoes, inputDTO.justificativa())
        ));

        return AreaRiscoOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaRiscoOutputDTO> listar() {
        return areaRiscoRepository.listarTodas().stream()
                .map(AreaRiscoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaRiscoOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        if (nivelPerigo == null) {
            return listar();
        }
        return areaRiscoRepository.listarPorNivelPerigo(nivelPerigo).stream()
                .map(AreaRiscoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AreaRiscoOutputDTO buscarPorId(Integer id) {
        AreaRisco areaRisco = areaRiscoRepository.buscarPorId(id)
                .orElseThrow(() -> new AreaRiscoNaoEncontradaException(id));
        return AreaRiscoOutputDTO.deDominio(areaRisco);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroHistoricoOutputDTO> listarHistorico(Integer id) {
        areaRiscoRepository.buscarPorId(id).orElseThrow(() -> new AreaRiscoNaoEncontradaException(id));
        return logAuditoriaRepository.listarPorEntidade(ENTIDADE, id).stream()
                .map(RegistroHistoricoOutputDTO::deDominio)
                .toList();
    }

    /**
     * Texto gravado em {@code LogAuditoria.justificativa}: cada campo alterado com valor anterior e
     * novo, no mesmo formato "de ... para ..." do histórico de tarefas (#112), seguido do motivo
     * informado pelo Gestor.
     */
    static String descreverHistorico(List<AlteracaoAreaRisco> alteracoes, String justificativa) {
        String mudancas = alteracoes.isEmpty()
                ? "Atualização da área de risco sem mudança de dados."
                : alteracoes.stream()
                        .map(alteracao -> alteracao.campo() + ": de " + valorNoHistorico(alteracao.anterior())
                                + " para " + valorNoHistorico(alteracao.novo()))
                        .collect(Collectors.joining("; ", "Alteração da área de risco: ", "."));
        if (justificativa == null || justificativa.isBlank()) {
            return mudancas;
        }
        return mudancas + " Justificativa: " + justificativa.trim();
    }

    private static String valorNoHistorico(String valor) {
        return valor != null ? "\"" + valor + "\"" : "não informado";
    }

    private String descreverSetor(Integer setorId) {
        if (setorRepository == null) {
            return "setor " + setorId;
        }
        return setorRepository.buscarPorId(setorId)
                .map(Setor::getNome)
                .orElse("setor " + setorId);
    }

    private List<Epi> resolverEpis(List<Integer> episObrigatoriosIds) {
        if (episObrigatoriosIds == null || episObrigatoriosIds.isEmpty()) {
            return List.of();
        }
        return episObrigatoriosIds.stream()
                .distinct()
                .map(epiId -> epiRepository.buscarPorId(epiId)
                        .orElseThrow(() -> new EpiNaoEncontradoException(epiId)))
                .toList();
    }
}
