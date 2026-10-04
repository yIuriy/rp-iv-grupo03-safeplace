/*
 * Caso de uso da classificação de periculosidade das tarefas (RF06 / UC11): cadastro, classificação
 * com responsável técnico e histórico em LogAuditoria (#112) e leitura desse histórico (#114).
 */
package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.TarefaOutputDTO;
import br.edu.safeplace.backend.application.port.in.ClassificarTarefaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiTarefaUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.tarefa.RegistroClassificacaoTarefa;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
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
public class TarefaUseCase implements ClassificarTarefaUseCase, GerenciarEpiTarefaUseCase {

    private static final String ENTIDADE = "Tarefa";

    private final TarefaRepositoryPort repositoryPort;
    private final LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;
    private final UsuarioRepositorioPorta usuarioRepositorio;
    private final AreaRiscoRepositoryPort areaRiscoRepositoryPort;
    private final SetorRepositoryPort setorRepositoryPort;
    private final EpiRepositoryPort epiRepositoryPort;

    @Autowired
    public TarefaUseCase(TarefaRepositoryPort repositoryPort, LogAuditoriaRepositoryPort logAuditoriaRepositoryPort,
            UsuarioRepositorioPorta usuarioRepositorio, AreaRiscoRepositoryPort areaRiscoRepositoryPort,
            SetorRepositoryPort setorRepositoryPort, EpiRepositoryPort epiRepositoryPort) {
        this.repositoryPort = repositoryPort;
        this.logAuditoriaRepositoryPort = logAuditoriaRepositoryPort;
        this.usuarioRepositorio = usuarioRepositorio;
        this.areaRiscoRepositoryPort = areaRiscoRepositoryPort;
        this.setorRepositoryPort = setorRepositoryPort;
        this.epiRepositoryPort = epiRepositoryPort;
    }

    public TarefaUseCase(TarefaRepositoryPort repositoryPort, LogAuditoriaRepositoryPort logAuditoriaRepositoryPort,
            UsuarioRepositorioPorta usuarioRepositorio, AreaRiscoRepositoryPort areaRiscoRepositoryPort,
            SetorRepositoryPort setorRepositoryPort) {
        this(repositoryPort, logAuditoriaRepositoryPort, usuarioRepositorio, areaRiscoRepositoryPort, setorRepositoryPort, null);
    }

    public TarefaUseCase(TarefaRepositoryPort repositoryPort, LogAuditoriaRepositoryPort logAuditoriaRepositoryPort,
            UsuarioRepositorioPorta usuarioRepositorio) {
        this(repositoryPort, logAuditoriaRepositoryPort, usuarioRepositorio, null, null, null);
    }

    @Override
    @Transactional
    public TarefaOutputDTO cadastrarTarefa(CadastrarTarefaInputDTO inputDTO) {
        validarRelacoes(inputDTO.areaRiscoId(), inputDTO.setoresIds());
        Tarefa novaTarefa = Tarefa.cadastrarComRelacoes(
                inputDTO.descricao(), inputDTO.areaRiscoId(), inputDTO.setoresIds());
        GestorDeSeguranca responsavel = null;
        if (inputDTO.nivelPerigo() != null) {
            responsavel = resolverResponsavelTecnico();
            novaTarefa.classificar(inputDTO.nivelPerigo(), responsavel);
        }
        Tarefa salva = repositoryPort.salvar(novaTarefa);

        registrarLog(LogAuditoria.registrarCadastro(
                resolverOperador(), ENTIDADE, salva.getId(), "Cadastro de Tarefa: " + salva.getDescricao()));
        if (salva.estaClassificada()) {
            registrarClassificacao(new RegistroClassificacaoTarefa(
                    salva.getId(), null, salva.getNivelPerigo(), responsavel, salva.getDataClassificacao()));
        }

        return TarefaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaOutputDTO> listar() {
        return repositoryPort.listarTodas().stream()
                .map(TarefaOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarefaOutputDTO> listarPorNivelPerigo(NivelPerigo nivelPerigo) {
        if (nivelPerigo == null) {
            return listar();
        }
        return repositoryPort.listarPorNivelPerigo(nivelPerigo).stream()
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
        RegistroClassificacaoTarefa registro = tarefa.classificar(nivelPerigo, resolverResponsavelTecnico());
        Tarefa salva = repositoryPort.salvar(tarefa);

        registrarClassificacao(registro);

        return TarefaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroHistoricoOutputDTO> listarHistorico(Integer id) {
        buscarTarefa(id);
        if (logAuditoriaRepositoryPort == null) {
            return List.of();
        }
        return logAuditoriaRepositoryPort.listarPorEntidade(ENTIDADE, id).stream()
                .map(RegistroHistoricoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional
    public TarefaOutputDTO associarEpis(Integer tarefaId, List<Integer> epiIds) {
        Tarefa tarefa = buscarTarefa(tarefaId);
        List<Epi> epis = resolverEpis(epiIds);
        tarefa.associarEpis(epis);
        Tarefa salva = repositoryPort.salvar(tarefa);
        return TarefaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpiOutputDTO> listarEpisObrigatorios(Integer tarefaId) {
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
        if (epiRepositoryPort == null) {
            return List.of();
        }
        return epiIds.stream()
                .distinct()
                .map(epiId -> epiRepositoryPort.buscarPorId(epiId)
                        .orElseThrow(() -> new EpiNaoEncontradoException(epiId)))
                .toList();
    }

    private void registrarClassificacao(RegistroClassificacaoTarefa registro) {
        String anterior = registro.nivelAnterior() != null ? registro.nivelAnterior().name() : "sem classificação";
        registrarLog(new LogAuditoria(
                null,
                "ALTERACAO",
                registro.dataHora(),
                registro.responsavel().getEmail(),
                String.format("Classificação de periculosidade da tarefa: de %s para %s; responsável técnico: %s (id %d)",
                        anterior, registro.nivelNovo().name(), registro.responsavel().getNome(),
                        registro.responsavel().getId()),
                ENTIDADE,
                registro.idTarefa()));
    }

    private void registrarLog(LogAuditoria log) {
        if (logAuditoriaRepositoryPort != null) {
            logAuditoriaRepositoryPort.salvar(log);
        }
    }

    /**
     * O responsável técnico é o Gestor de Segurança autenticado (UC11, ator principal). A regra de
     * acesso já restringe as alterações ao Gestor; sem um Gestor identificável, a classificação é
     * recusada em vez de ser gravada sem autoria.
     */
    private GestorDeSeguranca resolverResponsavelTecnico() {
        String email = emailAutenticado();
        if (email != null && usuarioRepositorio != null) {
            GestorDeSeguranca gestor = usuarioRepositorio.buscarPorEmail(email)
                    .filter(GestorDeSeguranca.class::isInstance)
                    .map(GestorDeSeguranca.class::cast)
                    .orElse(null);
            if (gestor != null) {
                return gestor;
            }
        }
        throw new AccessDeniedException(
                "A classificação de periculosidade exige um Gestor de Segurança autenticado como responsável técnico.");
    }

    private String resolverOperador() {
        String email = emailAutenticado();
        return email != null ? email : "SISTEMA";
    }

    private String emailAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
            String email = auth.getName().trim();
            return "anonymousUser".equals(email) ? null : email;
        }
        return null;
    }

    private Tarefa buscarTarefa(Integer id) {
        return repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    private void validarRelacoes(Integer areaRiscoId, List<Integer> setoresIds) {
        if (areaRiscoId != null && areaRiscoRepositoryPort != null) {
            areaRiscoRepositoryPort.buscarPorId(areaRiscoId)
                    .orElseThrow(() -> new IllegalArgumentException("Área de risco não encontrada com id: " + areaRiscoId));
        }
        if (setoresIds == null || setoresIds.isEmpty()) {
            return;
        }
        if (setoresIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Identificador de setor não pode ser nulo.");
        }
        if (setorRepositoryPort == null) {
            return;
        }
        setoresIds.stream().distinct().forEach(setorId ->
                setorRepositoryPort.buscarPorId(setorId)
                        .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + setorId)));
    }
}
