/*
 * Caso de uso da classificação de periculosidade das tarefas (RF06 / UC11): cadastro, classificação
 * com responsável técnico e histórico em LogAuditoria (#112) e leitura desse histórico (#114).
 */
package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.AtualizarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.tarefa.TarefaValidador;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.tarefa.RegistroClassificacaoTarefa;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.TarefaBuilder;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Classificação do nível de periculosidade das tarefas (RF06 / UC11)
 * e vinculação de EPIs obrigatórios (RF12 / UC08).
 *
 * <p>Toda classificação, no cadastro ou em reavaliação, identifica o Gestor de Segurança
 * autenticado como responsável técnico e grava em {@code LogAuditoria} o nível anterior e o novo
 * (UC11, regra 1; US06, critério 2).</p>
 */
@Service
public class TarefaService implements ClassificarTarefaUseCase, GerenciarEpiTarefaUseCase {

    private static final String ENTIDADE = "Tarefa";
    /**
     * UC11: o responsável técnico é o Gestor de Segurança autenticado. A regra de acesso já restringe
     * as alterações ao Gestor; sem um Gestor identificável, a classificação é recusada em vez de ser
     * gravada sem autoria.
     */
    private static final String SEM_RESPONSAVEL_TECNICO =
            "A classificação de periculosidade exige um Gestor de Segurança autenticado como responsável técnico.";

    private final TarefaRepository repository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final OperadorAtual operadorAtual;
    private final RegistradorAuditoria auditoria;
    private final Validador<Tarefa> validador;
    private final EpiRepository epiRepository;
    private final AreaRiscoRepository areaRiscoRepository;
    private final SetorRepository setorRepository;

    @Autowired
    public TarefaService(TarefaRepository repository, RegistradorAuditoria auditoria,
            LogAuditoriaRepository logAuditoriaRepository, OperadorAtual operadorAtual, Validador<Tarefa> validador,
            EpiRepository epiRepository, AreaRiscoRepository areaRiscoRepository, SetorRepository setorRepository) {
        this.repository = repository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        // Adapter (#236): operador e Gestor responsável vêm da porta OperadorAutenticado.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
        // Facade (#223): a trilha é gravada numa chamada, sem montar o LogAuditoria aqui.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        // #241: portas obrigatórias, sem construtores que repassam null.
        this.validador = java.util.Objects.requireNonNull(validador, "Validador da tarefa é obrigatório.");
        this.epiRepository = java.util.Objects.requireNonNull(epiRepository, "Repositório de EPIs é obrigatório.");
        // #260: área e setores são associações por objeto, resolvidas por estas portas.
        this.areaRiscoRepository = java.util.Objects.requireNonNull(
                areaRiscoRepository, "Repositório de áreas de risco é obrigatório.");
        this.setorRepository = java.util.Objects.requireNonNull(setorRepository, "Repositório de setores é obrigatório.");
    }

    /** Monta o validador padrão; usado pelos testes sem contexto Spring. */
    public TarefaService(TarefaRepository repository, LogAuditoriaRepository logAuditoriaRepository,
            OperadorAtual operadorAtual, AreaRiscoRepository areaRiscoRepository,
            SetorRepository setorRepository, EpiRepository epiRepository) {
        this(repository, new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                logAuditoriaRepository, operadorAtual,
                new TarefaValidador(), epiRepository, areaRiscoRepository, setorRepository);
    }

    @Override
    @Transactional
    public TarefaOutputDTO cadastrar(CadastrarTarefaInputDTO inputDTO) {
        // Builder (#240): constrói a tarefa nomeando os dados da criação
        Tarefa novaTarefa = new TarefaBuilder()
                .descricao(inputDTO.descricao())
                .areaRisco(resolverArea(inputDTO.areaRiscoId()))
                .setores(resolverSetores(inputDTO.setoresIds()))
                .build();
        // Chain of Responsibility (#228): valida antes de classificar e de chamar o repository.
        validador.validar(novaTarefa);
        GestorDeSeguranca responsavel = null;
        if (inputDTO.nivelPerigo() != null) {
            responsavel = operadorAtual.gestorObrigatorio(SEM_RESPONSAVEL_TECNICO);
            novaTarefa.classificar(inputDTO.nivelPerigo(), responsavel);
        }
        Tarefa salva = repository.salvar(novaTarefa);

        auditoria.cadastro(ENTIDADE, salva.getId(), "Cadastro de Tarefa: " + salva.getDescricao());
        if (salva.estaClassificada()) {
            registrarClassificacao(new RegistroClassificacaoTarefa(
                    salva.getId(), null, salva.getNivelPerigo(), responsavel, salva.getDataClassificacao()));
        }

        return TarefaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional
    public TarefaOutputDTO atualizar(Integer id, AtualizarTarefaInputDTO inputDTO, String operador) {
        Tarefa existente = buscarTarefa(id);

        Tarefa atualizada = existente.atualizar(
                inputDTO.descricao(),
                resolverArea(inputDTO.areaRiscoId()),
                resolverSetores(inputDTO.setoresIds())
        );
        validador.validar(atualizada);

        Tarefa salva = repository.salvar(atualizada);
        auditoria.por(operador).alteracao(ENTIDADE, salva.getId(), "Atualização de Tarefa: " + salva.getDescricao());

        return TarefaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaOutputDTO> listar() {
        return repository.listar().stream()
                .map(TarefaOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        if (nivelPerigo == null) {
            return listar();
        }
        return repository.listarPorNivelPerigo(nivelPerigo).stream()
                .map(TarefaOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TarefaOutputDTO buscarPorId(Integer id) {
        return TarefaOutputDTO.deDominio(buscarTarefa(id));
    }

    @Override
    @Transactional
    public TarefaOutputDTO classificar(Integer id, NivelPerigo nivelPerigo) {
        Tarefa tarefa = buscarTarefa(id);
        RegistroClassificacaoTarefa registro = tarefa.classificar(
                nivelPerigo, operadorAtual.gestorObrigatorio(SEM_RESPONSAVEL_TECNICO));
        Tarefa salva = repository.salvar(tarefa);

        registrarClassificacao(registro);

        return TarefaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroHistoricoOutputDTO> listarHistorico(Integer id) {
        buscarTarefa(id);
        return logAuditoriaRepository.listarPorEntidade(ENTIDADE, id).stream()
                .map(RegistroHistoricoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional
    public TarefaOutputDTO associar(Integer tarefaId, List<Integer> epiIds) {
        Tarefa tarefa = buscarTarefa(tarefaId);
        List<Epi> epis = resolverEpis(epiIds);
        String anteriores = descreverEpis(tarefa.verificarEpisObrigatoriosTarefa());
        tarefa.associarEpis(epis);
        Tarefa salva = repository.salvar(tarefa);

        // RNF05: o vínculo registra operador e o dado alterado (EPIs anteriores e novos).
        auditoria.alteracao(ENTIDADE, salva.getId(),
                "EPIs obrigatórios da tarefa: de " + anteriores + " para "
                        + descreverEpis(salva.verificarEpisObrigatoriosTarefa()));
        return TarefaOutputDTO.deDominio(salva);
    }

    private static String descreverEpis(List<Epi> epis) {
        if (epis == null || epis.isEmpty()) {
            return "nenhum";
        }
        return epis.stream()
                .map(epi -> epi.getNome() + " (id " + epi.getId() + ")")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpiOutputDTO> listarObrigatorios(Integer tarefaId) {
        Tarefa tarefa = buscarTarefa(tarefaId);
        return tarefa.verificarEpisObrigatoriosTarefa().stream()
                .map(EpiOutputDTO::deDominio)
                .toList();
    }

    private List<Epi> resolverEpis(List<Integer> epiIds) {
        if (epiIds == null || epiIds.isEmpty()) {
            return List.of();
        }
        if (epiIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Identificador de EPI não pode ser nulo.");
        }
        return epiIds.stream()
                .distinct()
                .map(epiId -> epiRepository.buscarPorId(epiId)
                        .orElseThrow(() -> new EpiNaoEncontradoException(epiId)))
                .toList();
    }

    private void registrarClassificacao(RegistroClassificacaoTarefa registro) {
        String anterior = registro.nivelAnterior() != null ? registro.nivelAnterior().name() : "sem classificação";
        auditoria.por(registro.responsavel().getEmail()).em(registro.dataHora()).alteracao(
                ENTIDADE,
                registro.idTarefa(),
                String.format("Classificação de periculosidade da tarefa: de %s para %s; responsável técnico: %s (id %d)",
                        anterior, registro.nivelNovo().name(), registro.responsavel().getNome(),
                        registro.responsavel().getId()));
    }

    /** A área, quando informada, precisa existir (V26); sem ela, a tarefa fica sem área. */
    private AreaRisco resolverArea(Integer areaRiscoId) {
        if (areaRiscoId == null) {
            return null;
        }
        return areaRiscoRepository.buscarPorId(areaRiscoId)
                .orElseThrow(() -> new IllegalArgumentException("Área de risco não encontrada com id: " + areaRiscoId));
    }

    /** A lista de setores pode ser vazia, mas cada id informado precisa existir (relação N:N da V26). */
    private List<Setor> resolverSetores(List<Integer> setoresIds) {
        if (setoresIds == null || setoresIds.isEmpty()) {
            return List.of();
        }
        if (setoresIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Identificador de setor não pode ser nulo.");
        }
        return setoresIds.stream()
                .distinct()
                .map(setorId -> setorRepository.buscarPorId(setorId)
                        .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + setorId)))
                .toList();
    }

    private Tarefa buscarTarefa(Integer id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }
}
