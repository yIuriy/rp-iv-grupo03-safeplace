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
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @org.springframework.beans.factory.annotation.Autowired
    public OcorrenciaService(
            OcorrenciaRepository repository,
            AreaRiscoRepository areaRiscoRepository,
            UsuarioRepository usuarioRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            NotificadorOcorrencia notificadorOcorrencia,
            @Qualifier("ocorrenciaValidador") Validador<Ocorrencia> validador,
            @Qualifier("protocoloCATValidador") Validador<Acidente> protocoloValidador,
            OperadorAtual operadorAtual) {
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
    }

    /** Usa o validador padrão; usado pelos testes sem contexto Spring. */
    public OcorrenciaService(
            OcorrenciaRepository repository,
            AreaRiscoRepository areaRiscoRepository,
            UsuarioRepository usuarioRepository,
            LogAuditoriaRepository logAuditoriaRepository,
            NotificadorOcorrencia notificadorOcorrencia,
            OperadorAtual operadorAtual) {
        this(repository, areaRiscoRepository, usuarioRepository, logAuditoriaRepository, notificadorOcorrencia,
                new OcorrenciaValidador(), new ProtocoloCATValidador(), operadorAtual);
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
        NumeroProtocoloCAT numeroProtocolo = NumeroProtocoloCAT.gerar(dataRef, sequencial);

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
        // Chain of Responsibility (#266): valida antes do repository.
        validador.validar(acidente);
        Acidente salvo = repository.salvarAcidente(acidente);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                operadorAtual.emailOuSistema(),
                "Acidente",
                salvo.getId(),
                "Registro de Acidente de Trabalho"
        );
        logAuditoriaRepository.salvar(log);

        notificadorOcorrencia.notificarGestor(salvo);

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
        validador.validar(incidente);
        Incidente salvo = repository.salvarIncidente(incidente);

        LogAuditoria log = LogAuditoria.registrarCadastro(
                operadorAtual.emailOuSistema(),
                "Incidente",
                salvo.getId(),
                "Registro de Incidente"
        );
        logAuditoriaRepository.salvar(log);

        notificadorOcorrencia.notificarGestor(salvo);

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
                .map(OcorrenciaOutputDTO::deDominio)
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
                .buscarIdsPorResponsavelETipoOperacao(email.get(), "CADASTRO", ENTIDADES_DE_RELATO);
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
            Acidente atualizado = new Acidente(
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
                    acidente.getProtocoloCAT(),
                    inputDTO.destino(),
                    gravidade);
            validador.validar(atualizado);
            salva = repository.salvarAcidente(atualizado);
        } else if (atual instanceof Incidente incidente) {
            Incidente atualizado = new Incidente(
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
                    inputDTO.potencialDano());
            validador.validar(atualizado);
            salva = repository.salvarIncidente(atualizado);
        } else {
            throw new IllegalStateException("Tipo de ocorrência não suportado.");
        }

        logAuditoriaRepository.salvar(LogAuditoria.registrarAlteracao(
                operadorAtual.emailOuSistema(),
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
                operadorAtual.emailOuSistema(),
                salva instanceof Acidente ? "Acidente" : "Incidente",
                salva.getId(),
                "Envio de ocorrência para triagem"
        );
        logAuditoriaRepository.salvar(log);

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
                operadorAtual.emailOuSistema(),
                salva instanceof Acidente ? "Acidente" : "Incidente",
                salva.getId(),
                "Arquivamento de ocorrência"
        );
        logAuditoriaRepository.salvar(log);

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
        // Só o protocolo muda: valida o formato (elo de #266) antes do repository, sem reaplicar as
        // regras do relato a acidentes antigos (issue #230).
        protocoloValidador.validar(consolidado);
        Acidente salvo = repository.salvarAcidente(consolidado);

        LogAuditoria log = LogAuditoria.registrarAlteracao(
                operadorAtual.emailOuSistema(),
                "Acidente",
                salvo.getId(),
                "Consolidação de CAT com protocolo " + salvo.getNumeroProtocolo()
        );
        logAuditoriaRepository.salvar(log);

        return OcorrenciaOutputDTO.deDominio(salvo);
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
