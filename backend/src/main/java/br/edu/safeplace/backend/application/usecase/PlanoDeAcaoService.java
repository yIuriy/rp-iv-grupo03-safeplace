package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarResponsavelInputDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarStatusPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ConfigurarAlertaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.application.dto.input.ProrrogarPrazoInputDTO;
import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProrrogacaoPrazoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.GerenciarPlanoDeAcaoUseCase;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusPlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPlanoNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ocorrencia.AcaoDoPlano;
import br.edu.safeplace.backend.validacao.ocorrencia.AcaoPlanoValidador;
import br.edu.safeplace.backend.validacao.ocorrencia.PlanoDaOcorrencia;
import br.edu.safeplace.backend.validacao.ocorrencia.PlanoValidador;

@Service
public class PlanoDeAcaoService implements GerenciarPlanoDeAcaoUseCase {

    private final PlanoDeAcaoRepository planoDeAcaoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SetorRepository setorRepository;
    private final NotificadorAcao notificadorAcao;
    private final GerenciarOcorrenciaUseCase consultaOcorrencia;
    private final Validador<AcaoDoPlano> acaoValidador;
    private final Validador<PlanoDaOcorrencia> planoValidador;

    @Autowired
    public PlanoDeAcaoService(
            PlanoDeAcaoRepository planoDeAcaoRepository,
            OcorrenciaRepository ocorrenciaRepository,
            UsuarioRepository usuarioRepository,
            SetorRepository setorRepository,
            NotificadorAcao notificadorAcao,
            GerenciarOcorrenciaUseCase consultaOcorrencia,
            Validador<AcaoDoPlano> acaoValidador,
            Validador<PlanoDaOcorrencia> planoValidador) {
        this.acaoValidador = acaoValidador;
        this.planoValidador = planoValidador;
        this.planoDeAcaoRepository = planoDeAcaoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        // #241: portas obrigatórias, sem construtores que repassam null.
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "Repositório de usuários é obrigatório.");
        this.setorRepository = Objects.requireNonNull(setorRepository, "Repositório de setores é obrigatório.");
        // UC07, alternativo II: a reatribuição sempre avisa o novo encarregado (issue #196).
        this.notificadorAcao = Objects.requireNonNull(
                notificadorAcao, "Porta de notificação das ações é obrigatória (UC07).");
        this.consultaOcorrencia = Objects.requireNonNull(
                consultaOcorrencia, "Consulta de ocorrências é obrigatória (visibilidade do plano, issue #100).");
    }

    /** Usa os validadores padrão; usado pelos testes sem contexto Spring. */
    public PlanoDeAcaoService(
            PlanoDeAcaoRepository planoDeAcaoRepository,
            OcorrenciaRepository ocorrenciaRepository,
            UsuarioRepository usuarioRepository,
            SetorRepository setorRepository,
            NotificadorAcao notificadorAcao,
            GerenciarOcorrenciaUseCase consultaOcorrencia) {
        this(planoDeAcaoRepository, ocorrenciaRepository, usuarioRepository, setorRepository, notificadorAcao,
                consultaOcorrencia, new AcaoPlanoValidador(), new PlanoValidador());
    }

    /** Ação inexistente responde como não encontrada (404), não como dado inválido (issue #196). */
    private PlanoDeAcao buscarAcao(Integer idAcao) {
        return planoDeAcaoRepository.buscarPorId(idAcao)
                .orElseThrow(() -> new AcaoPlanoNaoEncontradaException(idAcao));
    }

    /**
     * As consultas do plano seguem a visibilidade do detalhe da ocorrência (UC09, issue #100): o
     * Supervisor só acompanha o plano dos relatos que registrou; um relato alheio responde como
     * inexistente, do mesmo modo que o detalhe.
     */
    private void garantirOcorrenciaVisivel(Integer idOcorrencia) {
        consultaOcorrencia.buscarPorId(idOcorrencia);
    }

    @Override
    @Transactional
    public PlanoDeAcaoOutputDTO cadastrarAcao(Integer idOcorrencia, CriarAcaoPlanoInputDTO inputDTO) {
        Ocorrencia ocorrencia = ocorrenciaRepository.buscarPorId(idOcorrencia)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(idOcorrencia));

        Colaborador colaborador = null;
        if (inputDTO.responsavelColaboradorId() != null) {
            colaborador = usuarioRepository.buscarPorId(inputDTO.responsavelColaboradorId())
                    .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado para o ID: " + inputDTO.responsavelColaboradorId()));
        }

        Setor setor = null;
        if (inputDTO.responsavelSetorId() != null) {
            setor = setorRepository.buscarPorId(inputDTO.responsavelSetorId())
                    .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado para o ID: " + inputDTO.responsavelSetorId()));
        }

        GestorDeSeguranca gestor = gestorAutenticado();
        LocalDate hoje = LocalDate.now();

        PlanoDeAcao acao = PlanoDeAcao.definir(
                ocorrencia,
                inputDTO.medidasCorretivas(),
                inputDTO.medidasPreventivas(),
                inputDTO.prazo(),
                colaborador,
                setor,
                inputDTO.alertaAtivo(),
                inputDTO.antecedenciaAlertaDias(),
                gestor,
                hoje
        );
        // Chain of Responsibility (#266): a ação e, depois, o plano inteiro, antes do repository.
        acaoValidador.validar(new AcaoDoPlano(acao, ocorrencia, hoje));

        List<PlanoDeAcao> acoesExistentes = planoDeAcaoRepository.listarPorOcorrencia(idOcorrencia);
        List<PlanoDeAcao> todasAcoes = new ArrayList<>(acoesExistentes);
        todasAcoes.add(acao);
        planoValidador.validar(new PlanoDaOcorrencia(ocorrencia, todasAcoes));

        PlanoDeAcao salvo = planoDeAcaoRepository.salvar(acao);
        return PlanoDeAcaoOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanoDeAcaoOutputDTO> listarPorOcorrencia(Integer idOcorrencia) {
        garantirOcorrenciaVisivel(idOcorrencia);

        return planoDeAcaoRepository.listarPorOcorrencia(idOcorrencia).stream()
                .map(PlanoDeAcaoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanoDeAcaoOutputDTO buscarPorId(Integer idAcao) {
        PlanoDeAcao acao = buscarAcao(idAcao);
        if (acao.getIdOcorrencia() != null) {
            garantirOcorrenciaVisivel(acao.getIdOcorrencia());
        }
        return PlanoDeAcaoOutputDTO.deDominio(acao);
    }

    @Override
    @Transactional
    public PlanoDeAcaoOutputDTO atualizarStatus(Integer idAcao, AtualizarStatusPlanoInputDTO inputDTO) {
        PlanoDeAcao acao = buscarAcao(idAcao);

        if (inputDTO == null || inputDTO.status() == null || inputDTO.status().isBlank()) {
            throw new IllegalArgumentException("Status não pode ser nulo ou vazio.");
        }

        StatusPlanoDeAcao novoStatus;
        try {
            novoStatus = StatusPlanoDeAcao.valueOf(inputDTO.status().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido para o plano de ação: " + inputDTO.status());
        }

        acao.atualizarStatus(novoStatus);
        PlanoDeAcao salvo = planoDeAcaoRepository.salvar(acao);
        return PlanoDeAcaoOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public ProrrogacaoPrazoOutputDTO prorrogarPrazo(Integer idAcao, ProrrogarPrazoInputDTO inputDTO) {
        PlanoDeAcao acao = buscarAcao(idAcao);

        if (inputDTO == null) {
            throw new IllegalArgumentException("Dados de prorrogação não podem ser nulos.");
        }

        ProrrogacaoPrazo prorrogacao = acao.prorrogarPrazo(inputDTO.novoPrazo(), inputDTO.justificativa(), LocalDate.now());
        planoDeAcaoRepository.salvarProrrogacao(prorrogacao);
        planoDeAcaoRepository.salvar(acao);

        return ProrrogacaoPrazoOutputDTO.deDominio(prorrogacao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProrrogacaoPrazoOutputDTO> listarProrrogacoes(Integer idAcao) {
        PlanoDeAcao acao = buscarAcao(idAcao);
        if (acao.getIdOcorrencia() != null) {
            garantirOcorrenciaVisivel(acao.getIdOcorrencia());
        }

        return planoDeAcaoRepository.listarProrrogacoes(idAcao).stream()
                .map(ProrrogacaoPrazoOutputDTO::deDominio)
                .toList();
    }

    @Override
    @Transactional
    public PlanoDeAcaoOutputDTO atualizarResponsavel(Integer idAcao, AtualizarResponsavelInputDTO inputDTO) {
        PlanoDeAcao acao = buscarAcao(idAcao);

        if (inputDTO == null) {
            throw new IllegalArgumentException("Dados do novo responsável não podem ser nulos.");
        }

        AvisoAcao aviso;
        if (inputDTO.colaboradorId() != null) {
            Colaborador colaborador = usuarioRepository.buscarPorId(inputDTO.colaboradorId())
                    .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado para o ID: " + inputDTO.colaboradorId()));
            aviso = acao.atualizarResponsavel(colaborador, LocalDate.now());
        } else if (inputDTO.setorId() != null) {
            Setor setor = setorRepository.buscarPorId(inputDTO.setorId())
                    .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado para o ID: " + inputDTO.setorId()));
            aviso = acao.atualizarResponsavel(setor, LocalDate.now());
        } else {
            throw new IllegalArgumentException("Informe o novo responsável: colaboradorId ou setorId.");
        }

        PlanoDeAcao salvo = planoDeAcaoRepository.salvar(acao);
        // UC07, alternativo II: o aviso ao novo encarregado era descartado; agora é entregue (issue #196).
        notificadorAcao.notificar(aviso);
        return PlanoDeAcaoOutputDTO.deDominio(salvo);
    }

    @Override
    @Transactional
    public PlanoDeAcaoOutputDTO configurarAlerta(Integer idAcao, ConfigurarAlertaInputDTO inputDTO) {
        PlanoDeAcao acao = buscarAcao(idAcao);

        if (inputDTO == null) {
            throw new IllegalArgumentException("Configuração de alerta não pode ser nula.");
        }

        acao.configurarAlerta(inputDTO.ativo(), inputDTO.antecedenciaDias());
        PlanoDeAcao salvo = planoDeAcaoRepository.salvar(acao);
        return PlanoDeAcaoOutputDTO.deDominio(salvo);
    }

    private GestorDeSeguranca gestorAutenticado() {
        if (usuarioRepository == null) {
            return null;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        String email = auth.getName().trim();
        return usuarioRepository.buscarPorEmail(email)
                .filter(GestorDeSeguranca.class::isInstance)
                .map(GestorDeSeguranca.class::cast)
                .orElse(null);
    }
}
