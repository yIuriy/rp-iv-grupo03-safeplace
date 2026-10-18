/*
 * Caso de uso de ocorrências (UC05/UC09): registra acidentes e incidentes com auditoria e notificação,
 * consulta por critérios, id e protocolo CAT respeitando o perfil (issue #128) e conduz triagem,
 * arquivamento e consolidação da CAT. O relato é validado aqui, sem depender do frontend: área,
 * colaborador, gravidade e causa raiz obrigatórias, protocolo e relator definidos pelo servidor (#195).
 */
package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarOcorrenciaInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.NotificacaoOcorrenciaPort;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class OcorrenciaUseCase implements RegistrarOcorrenciaUseCase, GerenciarOcorrenciaUseCase {
    /** Nomes de entidade gravados na auditoria ao registrar um relato (ver registrarAcidente e registrarIncidente). */
    static final List<String> ENTIDADES_DE_RELATO = List.of("Acidente", "Incidente");

    private final OcorrenciaRepositoryPort repository;
    private final AreaRiscoRepositoryPort areaRiscoRepository;
    private final UsuarioRepositorioPorta usuarioRepository;
    private final LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;
    private final NotificacaoOcorrenciaPort notificacaoOcorrenciaPort;

    @org.springframework.beans.factory.annotation.Autowired
    public OcorrenciaUseCase(
            OcorrenciaRepositoryPort repository,
            AreaRiscoRepositoryPort areaRiscoRepository,
            UsuarioRepositorioPorta usuarioRepository,
            LogAuditoriaRepositoryPort logAuditoriaRepositoryPort,
            @org.springframework.beans.factory.annotation.Autowired(required = false) NotificacaoOcorrenciaPort notificacaoOcorrenciaPort) {
        this.repository = repository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.usuarioRepository = usuarioRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepositoryPort = java.util.Objects.requireNonNull(
                logAuditoriaRepositoryPort, "Porta de auditoria é obrigatória (RNF05).");
        this.notificacaoOcorrenciaPort = notificacaoOcorrenciaPort;
    }

    public OcorrenciaUseCase(
            OcorrenciaRepositoryPort repository,
            AreaRiscoRepositoryPort areaRiscoRepository,
            UsuarioRepositorioPorta usuarioRepository,
            LogAuditoriaRepositoryPort logAuditoriaRepositoryPort) {
        this(repository, areaRiscoRepository, usuarioRepository, logAuditoriaRepositoryPort, null);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new DadosOcorrenciaInvalidosException("Dados do relato não podem ser nulos.");
        }
        AreaRisco area = resolverAreaObrigatoria(inputDTO.areaId());
        Colaborador colaborador = resolverColaboradorObrigatorio(inputDTO.colaboradorId());
        CausaRaizAcidente causaRaiz = CausaRaizAcidente.de(inputDTO.causaRaiz());
        NivelPerigo gravidade = Acidente.lerGravidade(inputDTO.gravidade());

        // US13: o protocolo do acidente é sempre gerado pelo servidor, nunca informado pelo cliente.
        LocalDateTime dataRef = inputDTO.dataOcorrencia() != null ? inputDTO.dataOcorrencia() : LocalDateTime.now();
        long sequencial = repository.proximoSequencialCAT(dataRef.getYear(), dataRef.getMonthValue());
        String numeroProtocolo = NumeroProtocoloCAT.gerar(dataRef, sequencial).getValor();

        Acidente acidente = new Acidente(
                null,
                inputDTO.descricao(),
                inputDTO.dataOcorrencia(),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                relatorAutenticado(),
                null,
                null,
                causaRaiz,
                inputDTO.tipo(),
                inputDTO.dano(),
                numeroProtocolo,
                inputDTO.destino(),
                gravidade
        );
        Acidente salvo = repository.salvarAcidente(acidente);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                resolverOperador(),
                "Acidente",
                salvo.getId(),
                "Registro de Acidente de Trabalho"
        );
        logAuditoriaRepositoryPort.salvar(log);

        if (notificacaoOcorrenciaPort != null) {
            notificacaoOcorrenciaPort.notificarGestor(salvo);
        }

        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new DadosOcorrenciaInvalidosException("Dados do relato não podem ser nulos.");
        }
        AreaRisco area = resolverAreaObrigatoria(inputDTO.areaId());
        Colaborador colaborador = resolverColaboradorObrigatorio(inputDTO.colaboradorId());

        Incidente incidente = new Incidente(
                null,
                inputDTO.descricao(),
                inputDTO.dataOcorrencia(),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                relatorAutenticado(),
                null,
                null,
                inputDTO.situacaoRisco(),
                inputDTO.potencialDano()
        );
        Incidente salvo = repository.salvarIncidente(incidente);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                resolverOperador(),
                "Incidente",
                salvo.getId(),
                "Registro de Incidente"
        );
        logAuditoriaRepositoryPort.salvar(log);

        if (notificacaoOcorrenciaPort != null) {
            notificacaoOcorrenciaPort.notificarGestor(salvo);
        }

        return OcorrenciaOutputDTO.deDominio(salvo);
    }

    /**
     * Resolve a área de risco do fato, obrigatória no relato e na edição (issue #195). A localização
     * estrutural vem apenas desta associação (DS09, seção 2.3).
     */
    private AreaRisco resolverAreaObrigatoria(Integer areaId) {
        if (areaId == null) {
            throw new DadosOcorrenciaInvalidosException("Área de risco é obrigatória.");
        }
        return areaRiscoRepository.buscarPorId(areaId)
                .orElseThrow(() -> new DadosOcorrenciaInvalidosException("Área de risco não encontrada para o ID: " + areaId));
    }

    /** Resolve o colaborador envolvido, obrigatório no relato e na edição (DS09, seção 2.2; issue #195). */
    private Colaborador resolverColaboradorObrigatorio(Integer colaboradorId) {
        if (colaboradorId == null) {
            throw new DadosOcorrenciaInvalidosException("Colaborador envolvido é obrigatório.");
        }
        return usuarioRepository.buscarPorId(colaboradorId)
                .orElseThrow(() -> new DadosOcorrenciaInvalidosException("Colaborador não encontrado para o ID: " + colaboradorId));
    }

    /**
     * Relator do relato: o usuário autenticado, Gestor de Segurança ou Supervisor (RF13, UC09). Só o
     * servidor o define (issue #195); sem sessão, o relato fica sem relator e a auditoria registra
     * {@code SISTEMA}.
     */
    private Colaborador relatorAutenticado() {
        String email = emailAutenticado();
        if (email == null) {
            return null;
        }
        return usuarioRepository.buscarPorEmail(email).orElse(null);
    }

    /**
     * Sobrescreve o método default da porta para que a consulta sem filtro também abra a transação:
     * o default chamaria listar(filtro) dentro do próprio objeto, sem passar pelo proxy do Spring, e
     * a área de risco carregada sob demanda falharia sem sessão.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OcorrenciaOutputDTO> listar() {
        return listar(FiltroOcorrenciasDTO.SEM_FILTRO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcorrenciaOutputDTO> listar(FiltroOcorrenciasDTO filtro) {
        Predicate<Ocorrencia> visivel = visibilidadeDoUsuarioAtual();
        return repository.listar(filtro != null ? filtro : FiltroOcorrenciasDTO.SEM_FILTRO).stream()
                .filter(visivel)
                .map(OcorrenciaOutputDTO::deDominio)
                .toList();
    }

    /**
     * O Supervisor acompanha os relatos que registrou (UC09); o Gestor vê todos (UC05). A mesma regra
     * vale para a lista, o detalhe e a busca por protocolo, para que o detalhe não revele o que a lista
     * esconde. Sem sessão, a consulta não é restringida.
     */
    private Predicate<Ocorrencia> visibilidadeDoUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSupervisor = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERVISOR".equals(a.getAuthority()));
        String email = emailAutenticado();
        if (!isSupervisor || email == null) {
            return ocorrencia -> true;
        }
        List<Integer> idsRelatados = logAuditoriaRepositoryPort
                .buscarIdsPorResponsavelETipoOperacao(email, "CADASTRO", ENTIDADES_DE_RELATO);
        return ocorrencia -> idsRelatados.contains(ocorrencia.getIdOcorrencia());
    }

    @Override
    @Transactional(readOnly = true)
    public OcorrenciaOutputDTO buscarPorId(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .filter(visibilidadeDoUsuarioAtual())
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));
        return OcorrenciaOutputDTO.deDominio(ocorrencia);
    }

    @Override
    @Transactional(readOnly = true)
    public OcorrenciaOutputDTO buscarPorProtocolo(String protocolo) {
        if (protocolo == null || protocolo.isBlank()) {
            throw new IllegalArgumentException("Protocolo não pode ser vazio.");
        }
        Ocorrencia ocorrencia = repository.buscarPorProtocolo(protocolo.trim())
                .filter(visibilidadeDoUsuarioAtual())
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException("Ocorrência não encontrada com o protocolo " + protocolo.trim() + "."));
        return OcorrenciaOutputDTO.deDominio(ocorrencia);
    }

    /**
     * Edita os dados do relato (UC05, cenário alternativo IV). Preserva tipo, data do registro,
     * situação, relator, protocolo CAT e plano embutido; registra na auditoria quais campos mudaram.
     * Ocorrência arquivada encerrou o acompanhamento e não é editada.
     */
    @Override
    @Transactional
    public OcorrenciaOutputDTO atualizar(Integer idOcorrencia, AtualizarOcorrenciaInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new IllegalArgumentException("Dados da ocorrência não podem ser nulos.");
        }
        Ocorrencia atual = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));
        if (atual.getStatusOcorrencia() == StatusOcorrencia.ARQUIVADA) {
            throw new IllegalStateException("Ocorrência arquivada não pode ser editada.");
        }

        AreaRisco area = resolverAreaObrigatoria(inputDTO.areaId());
        Colaborador colaborador = resolverColaboradorObrigatorio(inputDTO.colaboradorId());

        Ocorrencia salva;
        if (atual instanceof Acidente acidente) {
            // Campo em branco mantém o valor atual; valor desconhecido é recusado (issue #195).
            NivelPerigo gravidade = inputDTO.gravidade() == null || inputDTO.gravidade().isBlank()
                    ? acidente.getGravidade()
                    : Acidente.lerGravidade(inputDTO.gravidade());
            CausaRaizAcidente causaRaiz = inputDTO.causaRaiz() == null || inputDTO.causaRaiz().isBlank()
                    ? acidente.getCausaRaiz()
                    : CausaRaizAcidente.de(inputDTO.causaRaiz());
            salva = repository.salvarAcidente(new Acidente(
                    acidente.getIdOcorrencia(),
                    inputDTO.descricao(),
                    inputDTO.dataOcorrencia(),
                    acidente.getDataRegistro(),
                    acidente.getStatusOcorrencia(),
                    colaborador,
                    area,
                    acidente.getRelator(),
                    acidente.getLocalInformado(),
                    acidente.getPlanoDeAcao(),
                    causaRaiz,
                    inputDTO.tipo(),
                    inputDTO.dano(),
                    acidente.getNumeroProtocoloCAT(),
                    inputDTO.destino(),
                    gravidade));
        } else if (atual instanceof Incidente incidente) {
            salva = repository.salvarIncidente(new Incidente(
                    incidente.getIdOcorrencia(),
                    inputDTO.descricao(),
                    inputDTO.dataOcorrencia(),
                    incidente.getDataRegistro(),
                    incidente.getStatusOcorrencia(),
                    colaborador,
                    area,
                    incidente.getRelator(),
                    incidente.getLocalInformado(),
                    incidente.getPlanoDeAcao(),
                    inputDTO.situacaoRisco(),
                    inputDTO.potencialDano()));
        } else {
            throw new IllegalStateException("Tipo de ocorrência não suportado.");
        }

        logAuditoriaRepositoryPort.salvar(LogAuditoria.registrarAlteracao(
                resolverOperador(),
                salva instanceof Acidente ? "Acidente" : "Incidente",
                salva.getId(),
                "Atualização de ocorrência. Campos alterados: " + descreverAlteracoes(atual, salva)));

        return OcorrenciaOutputDTO.deDominio(salva);
    }

    private static String descreverAlteracoes(Ocorrencia antes, Ocorrencia depois) {
        List<String> campos = new ArrayList<>();
        if (!Objects.equals(antes.getDataOcorrencia(), depois.getDataOcorrencia())) campos.add("dataOcorrencia");
        if (!Objects.equals(antes.getDescricao(), depois.getDescricao())) campos.add("descricao");
        if (!Objects.equals(idDaArea(antes), idDaArea(depois))) campos.add("area");
        if (!Objects.equals(idDoColaborador(antes), idDoColaborador(depois))) campos.add("colaborador");
        if (antes instanceof Acidente a && depois instanceof Acidente d) {
            if (a.getCausaRaiz() != d.getCausaRaiz()) campos.add("causaRaiz");
            if (a.getGravidade() != d.getGravidade()) campos.add("gravidade");
            if (!Objects.equals(a.getTipo(), d.getTipo())) campos.add("tipo");
            if (!Objects.equals(a.getDano(), d.getDano())) campos.add("dano");
            if (!Objects.equals(a.getDestino(), d.getDestino())) campos.add("destino");
        }
        if (antes instanceof Incidente a && depois instanceof Incidente d) {
            if (!Objects.equals(a.getSituacaoRisco(), d.getSituacaoRisco())) campos.add("situacaoRisco");
            if (!Objects.equals(a.getPotencialDano(), d.getPotencialDano())) campos.add("potencialDano");
        }
        return campos.isEmpty() ? "nenhum" : String.join(", ", campos);
    }

    private static Integer idDaArea(Ocorrencia ocorrencia) {
        return ocorrencia.getArea() != null ? ocorrencia.getArea().getId() : null;
    }

    private static Integer idDoColaborador(Ocorrencia ocorrencia) {
        return ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getId() : null;
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO enviarParaTriagem(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));

        ocorrencia.enviarParaTriagem();

        Ocorrencia salva = salvarOcorrencia(ocorrencia);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                resolverOperador(),
                salva instanceof Acidente ? "Acidente" : "Incidente",
                salva.getId(),
                "Envio de ocorrência para triagem"
        );
        logAuditoriaRepositoryPort.salvar(log);

        return OcorrenciaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO arquivar(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));

        ocorrencia.arquivar();

        Ocorrencia salva = salvarOcorrencia(ocorrencia);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                resolverOperador(),
                salva instanceof Acidente ? "Acidente" : "Incidente",
                salva.getId(),
                "Arquivamento de ocorrência"
        );
        logAuditoriaRepositoryPort.salvar(log);

        return OcorrenciaOutputDTO.deDominio(salva);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO consolidarCAT(Integer idAcidente) {
        Acidente acidente = repository.buscarAcidentePorId(idAcidente)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idAcidente));

        LocalDateTime dataRef = acidente.getDataOcorrencia() != null
                ? acidente.getDataOcorrencia()
                : LocalDateTime.now();

        long sequencial = repository.proximoSequencialCAT(dataRef.getYear(), dataRef.getMonthValue());
        Acidente consolidado = acidente.consolidarCAT(sequencial);
        Acidente salvo = repository.salvarAcidente(consolidado);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                resolverOperador(),
                "Acidente",
                salvo.getId(),
                "Consolidação de CAT com protocolo " + salvo.getNumeroProtocolo()
        );
        logAuditoriaRepositoryPort.salvar(log);

        return OcorrenciaOutputDTO.deDominio(salvo);
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

    private Ocorrencia salvarOcorrencia(Ocorrencia ocorrencia) {
        if (ocorrencia instanceof Acidente acidente) {
            return repository.salvarAcidente(acidente);
        } else if (ocorrencia instanceof Incidente incidente) {
            return repository.salvarIncidente(incidente);
        }
        throw new IllegalStateException("Tipo de ocorrência não suportado para persistência.");
    }
}
