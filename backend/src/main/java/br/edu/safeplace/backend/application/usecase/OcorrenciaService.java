/*
 * Caso de uso de ocorrências (UC05/UC09): registra acidentes e incidentes com auditoria e notificação,
 * consulta por critérios, id e protocolo CAT respeitando o perfil (issue #128) e conduz triagem,
 * arquivamento e consolidação da CAT. O relato é validado aqui, sem depender do frontend: área,
 * colaborador, gravidade e causa raiz obrigatórias, protocolo e relator definidos pelo servidor (#195).
 * A criação da família de objetos do relato utiliza o padrão Abstract Factory (#239).
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.assembler.OcorrenciaOutputAssembler;
import br.edu.safeplace.backend.application.dto.input.AtualizarOcorrenciaInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoIncidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ocorrencia.OcorrenciaValidador;
import br.edu.safeplace.backend.validacao.ocorrencia.ProtocoloCATValidador;

@Service
public class OcorrenciaService implements RegistrarOcorrenciaUseCase, GerenciarOcorrenciaUseCase {
    /** Nomes de entidade gravados na auditoria ao registrar um relato (ver registrarAcidente e registrarIncidente). */
    static final List<String> ENTIDADES_DE_RELATO = List.of("Acidente", "Incidente");

    private final OcorrenciaRepository repository;
    private final AreaRiscoRepository areaRiscoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final NotificadorOcorrencia notificadorOcorrencia;
    private final Validador<Ocorrencia> validador;
    private final Validador<Acidente> protocoloValidador;
    private final OperadorAtual operadorAtual;
    private final RegistradorAuditoria auditoria;
    private final Map<TipoOcorrencia, FabricaRelatoOcorrencia<?, ?>> fabricas;
    private final OcorrenciaOutputAssembler assembler = new OcorrenciaOutputAssembler();

    @org.springframework.beans.factory.annotation.Autowired
    public OcorrenciaService(
            OcorrenciaRepository repository,
            AreaRiscoRepository areaRiscoRepository,
            UsuarioRepository usuarioRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            NotificadorOcorrencia notificadorOcorrencia,
            @Qualifier("ocorrenciaValidador") Validador<Ocorrencia> validador,
            @Qualifier("protocoloCATValidador") Validador<Acidente> protocoloValidador,
            OperadorAtual operadorAtual,
            RegistradorAuditoria auditoria,
            FabricaRelatoAcidente fabricaRelatoAcidente,
            FabricaRelatoIncidente fabricaRelatoIncidente) {
        this.validador = java.util.Objects.requireNonNull(validador, "Validador da ocorrência é obrigatório.");
        this.protocoloValidador = java.util.Objects.requireNonNull(
                protocoloValidador, "Validador do protocolo CAT é obrigatório.");
        this.repository = repository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.usuarioRepository = usuarioRepository;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.logAuditoriaRepository = java.util.Objects.requireNonNull(
                logAuditoriaRepository, "Porta de auditoria é obrigatória (RNF05).");
        // Null Object (#227): a porta é obrigatória; sem notificação, injeta-se NotificadorOcorrenciaNulo.
        this.notificadorOcorrencia = java.util.Objects.requireNonNull(
                notificadorOcorrencia, "Notificador de ocorrência é obrigatório.");
        // Adapter (#236): relator, auditoria e visibilidade leem o operador pela porta.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
        // Facade (#223): a trilha é gravada numa chamada, sem montar o LogAuditoria aqui.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        // Abstract Factory (#239): família de criação parametrizada por tipo de relato.
        this.fabricas = Map.of(
                TipoOcorrencia.ACIDENTE, java.util.Objects.requireNonNull(fabricaRelatoAcidente, "Fábrica de relato de acidente é obrigatória."),
                TipoOcorrencia.INCIDENTE, java.util.Objects.requireNonNull(fabricaRelatoIncidente, "Fábrica de relato de incidente é obrigatória.")
        );
    }

    /** Construtor para testes sem injeção explícita das fábricas (#239). */
    public OcorrenciaService(
            OcorrenciaRepository repository,
            AreaRiscoRepository areaRiscoRepository,
            UsuarioRepository usuarioRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            NotificadorOcorrencia notificadorOcorrencia,
            Validador<Ocorrencia> validador,
            Validador<Acidente> protocoloValidador,
            OperadorAtual operadorAtual,
            RegistradorAuditoria auditoria) {
        this(repository, areaRiscoRepository, usuarioRepository, logAuditoriaRepository, notificadorOcorrencia,
                validador, protocoloValidador, operadorAtual, auditoria,
                new FabricaRelatoAcidente(), new FabricaRelatoIncidente());
    }

    /** Usa o validador e fábricas padrão; usado pelos testes sem contexto Spring. */
    public OcorrenciaService(
            OcorrenciaRepository repository,
            AreaRiscoRepository areaRiscoRepository,
            UsuarioRepository usuarioRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            NotificadorOcorrencia notificadorOcorrencia,
            OperadorAtual operadorAtual) {
        this(repository, areaRiscoRepository, usuarioRepository, logAuditoriaRepository, notificadorOcorrencia,
                new OcorrenciaValidador(), new ProtocoloCATValidador(), operadorAtual,
                new RegistradorAuditoria(logAuditoriaRepository, operadorAtual),
                new FabricaRelatoAcidente(), new FabricaRelatoIncidente());
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarAcidente(RegistrarAcidenteInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new DadosOcorrenciaInvalidosException("Dados do relato não podem ser nulos.");
        }
        AreaRisco area = resolverAreaObrigatoria(inputDTO.areaId());
        Colaborador colaborador = resolverColaboradorObrigatorio(inputDTO.colaboradorId());

        // US13: o protocolo do acidente é sempre gerado pelo servidor, nunca informado pelo cliente.
        LocalDateTime dataRef = inputDTO.dataOcorrencia() != null ? inputDTO.dataOcorrencia() : LocalDateTime.now();
        long sequencial = repository.proximoSequencialCAT(dataRef.getYear(), dataRef.getMonthValue());
        NumeroProtocoloCAT numeroProtocolo = NumeroProtocoloCAT.gerar(dataRef, sequencial);

        @SuppressWarnings("unchecked")
        FabricaRelatoOcorrencia<RegistrarAcidenteInputDTO, Acidente> fabrica =
                (FabricaRelatoOcorrencia<RegistrarAcidenteInputDTO, Acidente>) fabricas.get(TipoOcorrencia.ACIDENTE);

        Acidente acidente = fabrica.criarOcorrencia(inputDTO, area, colaborador, relatorAutenticado(), numeroProtocolo);
        // Chain of Responsibility (#266): valida antes do repository.
        validador.validar(acidente);
        Ocorrencia salvo = repository.salvar(acidente);

        auditoria.cadastro(fabrica.entidadeAuditoria(), salvo.getId(), fabrica.descricaoAuditoria());

        notificadorOcorrencia.notificar(salvo);

        return assembler.montar(salvo);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO registrarIncidente(RegistrarIncidenteInputDTO inputDTO) {
        if (inputDTO == null) {
            throw new DadosOcorrenciaInvalidosException("Dados do relato não podem ser nulos.");
        }
        AreaRisco area = resolverAreaObrigatoria(inputDTO.areaId());
        Colaborador colaborador = resolverColaboradorObrigatorio(inputDTO.colaboradorId());

        @SuppressWarnings("unchecked")
        FabricaRelatoOcorrencia<RegistrarIncidenteInputDTO, Incidente> fabrica =
                (FabricaRelatoOcorrencia<RegistrarIncidenteInputDTO, Incidente>) fabricas.get(TipoOcorrencia.INCIDENTE);

        Incidente incidente = fabrica.criarOcorrencia(inputDTO, area, colaborador, relatorAutenticado(), null);
        validador.validar(incidente);
        Ocorrencia salvo = repository.salvar(incidente);

        auditoria.cadastro(fabrica.entidadeAuditoria(), salvo.getId(), fabrica.descricaoAuditoria());

        notificadorOcorrencia.notificar(salvo);

        return assembler.montar(salvo);
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
        return operadorAtual.obterIdentificadorOperador()
                .flatMap(usuarioRepository::buscarPorEmail)
                .orElse(null);
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
                .map(assembler::montar)
                .toList();
    }

    /**
     * O Supervisor acompanha os relatos que registrou (UC09); o Gestor vê todos (UC05). A mesma regra
     * vale para a lista, o detalhe e a busca por protocolo, para que o detalhe não revele o que a lista
     * esconde. Sem sessão, a consulta não é restringida.
     */
    private Predicate<Ocorrencia> visibilidadeDoUsuarioAtual() {
        Optional<String> email = operadorAtual.obterIdentificadorOperador();
        if (!operadorAtual.possuiPapel("ROLE_SUPERVISOR") || email.isEmpty()) {
            return ocorrencia -> true;
        }
        List<Integer> idsRelatados = logAuditoriaRepository
                .buscarIdsPorResponsavel(email.get(), "CADASTRO", ENTIDADES_DE_RELATO);
        return ocorrencia -> idsRelatados.contains(ocorrencia.getIdOcorrencia());
    }

    @Override
    @Transactional(readOnly = true)
    public OcorrenciaOutputDTO buscarPorId(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .filter(visibilidadeDoUsuarioAtual())
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));
        return assembler.montar(ocorrencia);
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
        return assembler.montar(ocorrencia);
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

        Ocorrencia salva = switch (atual) {
            case Acidente acidente -> {
                NivelPerigo gravidade = inputDTO.gravidade() == null || inputDTO.gravidade().isBlank()
                        ? acidente.getGravidade()
                        : Acidente.lerGravidade(inputDTO.gravidade());
                CausaRaizAcidente causaRaiz = inputDTO.causaRaiz() == null || inputDTO.causaRaiz().isBlank()
                        ? acidente.getCausaRaiz()
                        : CausaRaizAcidente.de(inputDTO.causaRaiz());
                Acidente atualizado = Acidente.builder()
                        .idOcorrencia(acidente.getIdOcorrencia())
                        .descricao(inputDTO.descricao())
                        .dataOcorrencia(inputDTO.dataOcorrencia())
                        .dataRegistro(acidente.getDataRegistro())
                        .statusOcorrencia(acidente.getStatusOcorrencia())
                        .colaborador(colaborador)
                        .area(area)
                        .relator(acidente.getRelator())
                        .localInformado(acidente.getLocalInformado())
                        .planoDeAcao(acidente.getPlanoDeAcao())
                        .causaRaiz(causaRaiz)
                        .tipo(inputDTO.tipo())
                        .dano(inputDTO.dano())
                        .numeroProtocoloCAT(acidente.getProtocoloCAT())
                        .destino(inputDTO.destino())
                        .gravidade(gravidade)
                        .build();
                validador.validar(atualizado);
                yield repository.salvar(atualizado);
            }
            case Incidente incidente -> {
                Incidente atualizado = Incidente.builder()
                        .idOcorrencia(incidente.getIdOcorrencia())
                        .descricao(inputDTO.descricao())
                        .dataOcorrencia(inputDTO.dataOcorrencia())
                        .dataRegistro(incidente.getDataRegistro())
                        .statusOcorrencia(incidente.getStatusOcorrencia())
                        .colaborador(colaborador)
                        .area(area)
                        .relator(incidente.getRelator())
                        .localInformado(incidente.getLocalInformado())
                        .planoDeAcao(incidente.getPlanoDeAcao())
                        .situacaoRisco(inputDTO.situacaoRisco())
                        .potencialDano(inputDTO.potencialDano())
                        .build();
                validador.validar(atualizado);
                yield repository.salvar(atualizado);
            }
            // Sem sealed (commit 01a0d18, proxies do Hibernate), o switch não é exaustivo para o compilador.
            // Nenhuma associação aponta para Ocorrencia, então não chega proxy da classe base aqui.
            default -> throw new IllegalArgumentException("Tipo de ocorrência não suportado: " + atual.getClass().getName());
        };

        auditoria.alteracao(entidadeAuditada(salva), salva.getId(),
                "Atualização de ocorrência. Campos alterados: " + descreverAlteracoes(atual, salva));

        return assembler.montar(salva);
    }

    private static String descreverAlteracoes(Ocorrencia antes, Ocorrencia depois) {
        List<String> campos = new ArrayList<>();
        if (!Objects.equals(antes.getDataOcorrencia(), depois.getDataOcorrencia())) campos.add("dataOcorrencia");
        if (!Objects.equals(antes.getDescricao(), depois.getDescricao())) campos.add("descricao");
        if (!Objects.equals(idDaArea(antes), idDaArea(depois))) campos.add("area");
        if (!Objects.equals(idDoColaborador(antes), idDoColaborador(depois))) campos.add("colaborador");
        if (antes.getTipoOcorrencia() == TipoOcorrencia.ACIDENTE && depois.getTipoOcorrencia() == TipoOcorrencia.ACIDENTE) {
            Acidente a = (Acidente) antes;
            Acidente d = (Acidente) depois;
            if (a.getCausaRaiz() != d.getCausaRaiz()) campos.add("causaRaiz");
            if (a.getGravidade() != d.getGravidade()) campos.add("gravidade");
            if (!Objects.equals(a.getTipo(), d.getTipo())) campos.add("tipo");
            if (!Objects.equals(a.getDano(), d.getDano())) campos.add("dano");
            if (!Objects.equals(a.getDestino(), d.getDestino())) campos.add("destino");
        }
        if (antes.getTipoOcorrencia() == TipoOcorrencia.INCIDENTE && depois.getTipoOcorrencia() == TipoOcorrencia.INCIDENTE) {
            Incidente a = (Incidente) antes;
            Incidente d = (Incidente) depois;
            if (!Objects.equals(a.getSituacaoRisco(), d.getSituacaoRisco())) campos.add("situacaoRisco");
            if (!Objects.equals(a.getPotencialDano(), d.getPotencialDano())) campos.add("potencialDano");
        }
        return campos.isEmpty() ? "nenhum" : String.join(", ", campos);
    }

    private String entidadeAuditada(Ocorrencia ocorrencia) {
        FabricaRelatoOcorrencia<?, ?> fabrica = fabricas.get(ocorrencia.getTipoOcorrencia());
        return fabrica != null ? fabrica.entidadeAuditoria() : ocorrencia.getTipoOcorrencia().name();
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

        Ocorrencia salva = repository.salvar(ocorrencia);

        auditoria.alteracao(entidadeAuditada(salva), salva.getId(), "Envio de ocorrência para triagem");

        return assembler.montar(salva);
    }

    @Override
    @Transactional
    public OcorrenciaOutputDTO arquivar(Integer idOcorrencia) {
        Ocorrencia ocorrencia = repository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));

        ocorrencia.arquivar();

        Ocorrencia salva = repository.salvar(ocorrencia);

        auditoria.alteracao(entidadeAuditada(salva), salva.getId(), "Arquivamento de ocorrência");

        return assembler.montar(salva);
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
        // Só o protocolo muda: valida o formato (elo de #266) antes do repository, sem reaplicar as
        // regras do relato a acidentes antigos (issue #230).
        protocoloValidador.validar(consolidado);
        Acidente salvo = (Acidente) repository.salvar(consolidado);

        auditoria.alteracao("Acidente", salvo.getId(),
                "Consolidação de CAT com protocolo " + salvo.getNumeroProtocolo());

        return assembler.montar(salvo);
    }
}
