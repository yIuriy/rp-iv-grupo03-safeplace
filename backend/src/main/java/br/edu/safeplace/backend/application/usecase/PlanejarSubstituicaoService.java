package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.GerarAnaliseProjecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlertaSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.FalhaProjecaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProjecaoSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ResultadoAnaliseSubstituicaoDTO;
import br.edu.safeplace.backend.application.port.in.PlanejarSubstituicaoUseCase;
import br.edu.safeplace.backend.application.port.out.EmprestimoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.epi.AlertaSubstituicaoEpi;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import br.edu.safeplace.backend.domain.epi.CalculoProjecaoSubstituicao;
import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.domain.epi.SubstituicaoInteligenteService;
import br.edu.safeplace.backend.domain.epi.exception.DadosInsuficientesParaProjecaoException;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.projecao.ProjecaoValidador;

@Service
public class PlanejarSubstituicaoService implements PlanejarSubstituicaoUseCase {

    private final EpiRepository epiRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final SetorRepository setorRepository;
    private final ProjecaoSubstituicaoRepository projecaoRepository;
    private final CalculoProjecaoSubstituicao calculoService;
    private final Validador<ContextoProjecao> validadorProjecao;

    @Autowired
    public PlanejarSubstituicaoService(
            EpiRepository epiRepository,
            EmprestimoRepository emprestimoRepository,
            SetorRepository setorRepository,
            ProjecaoSubstituicaoRepository projecaoRepository,
            CalculoProjecaoSubstituicao calculoService,
            Validador<ContextoProjecao> validadorProjecao) {
        this.epiRepository = epiRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.setorRepository = setorRepository;
        this.projecaoRepository = projecaoRepository;
        this.calculoService = calculoService;
        this.validadorProjecao = validadorProjecao;
    }

    /** Monta o cálculo e o validador padrão; usado pelos testes sem contexto Spring. */
    public PlanejarSubstituicaoService(
            EpiRepository epiRepository,
            EmprestimoRepository emprestimoRepository,
            SetorRepository setorRepository,
            ProjecaoSubstituicaoRepository projecaoRepository) {
        this(epiRepository, emprestimoRepository, setorRepository, projecaoRepository,
                new SubstituicaoInteligenteService(), new ProjecaoValidador());
    }

    @Override
    @Transactional
    public ResultadoAnaliseSubstituicaoDTO gerarAnalise(GerarAnaliseProjecaoInputDTO input) {
        if (input == null) {
            input = new GerarAnaliseProjecaoInputDTO(LocalDate.now(), null, null);
        }

        LocalDate dataReferencia = input.obterDataReferencia();
        Integer diasAmostragem = input.obterDiasAmostragem();

        // 1. Setor informado na requisição: inexistente é erro, não é ignorado
        Optional<Setor> setorParametro = Optional.empty();
        if (input.setorId() != null) {
            Integer setorId = input.setorId();
            setorParametro = Optional.of(setorRepository.buscarPorId(setorId)
                    .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com id: " + setorId)));
        }

        List<Epi> epis = epiRepository.listarTodos();
        List<ProjecaoSubstituicao> projecoesParaSalvar = new ArrayList<>();
        List<ProjecaoSubstituicaoOutputDTO> projecoesDTO = new ArrayList<>();
        List<FalhaProjecaoOutputDTO> falhasDTO = new ArrayList<>();
        List<AlertaSubstituicaoOutputDTO> alertasDTO = new ArrayList<>();

        for (Epi epi : epis) {
            Integer epiId = epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
            String nomeEpi = epi.getNome();

            // Determina setor do EPI (parâmetro da requisição ou localização do EPI)
            Optional<Setor> setorResolvido = setorParametro.isPresent() ? setorParametro : resolverSetorEpi(epi);

            if (setorResolvido.isEmpty()) {
                falhasDTO.add(new FalhaProjecaoOutputDTO(epiId, nomeEpi,
                        "EPI não possui setor com periculosidade associado nem foi informado setor na requisição."));
                adicionarAlertas(alertasDTO, epi, null, dataReferencia);
                continue;
            }

            NivelPerigo perigo = setorResolvido.get().getStatus();
            List<Emprestimo> emprestimos = emprestimoRepository.listarPorEpi(epiId);

            try {
                ContextoProjecao contexto = new ContextoProjecao(
                        epi, perigo, emprestimos, dataReferencia, diasAmostragem);
                // Chain of Responsibility (#267): pré-condições antes do cálculo e do repository.
                validadorProjecao.validar(contexto);
                ProjecaoSubstituicao projecao = calculoService.calcularProjecao(contexto);

                projecoesParaSalvar.add(projecao);

                projecoesDTO.add(ProjecaoSubstituicaoOutputDTO.aPartirDe(
                        projecao, nomeEpi, epi.getDataValidadeCa(), epi.getQuantidade(), epi.getEstoqueMinimo()));

                // Emite alertas preventivos para a projeção bem-sucedida
                adicionarAlertas(alertasDTO, epi, projecao, dataReferencia);

            } catch (DadosInsuficientesParaProjecaoException ex) {
                // Falha detectada conforme UC10 Exceção I: registra pendência para permitir nova tentativa.
                // Estoque crítico e CA (DS10, 2.4) independem da projeção e continuam sinalizados.
                falhasDTO.add(new FalhaProjecaoOutputDTO(epiId, nomeEpi, ex.getMessage()));
                adicionarAlertas(alertasDTO, epi, null, dataReferencia);
            } catch (Exception ex) {
                falhasDTO.add(new FalhaProjecaoOutputDTO(epiId, nomeEpi,
                        "Erro inesperado no cálculo: " + ex.getMessage()));
            }
        }

        // 2. Persistência em lote das projeções geradas com sucesso
        if (!projecoesParaSalvar.isEmpty()) {
            projecaoRepository.salvarTodas(projecoesParaSalvar);
        }

        return ResultadoAnaliseSubstituicaoDTO.criar(projecoesDTO, falhasDTO, alertasDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjecaoSubstituicaoOutputDTO> listarProjecoes() {
        Map<Integer, Epi> mapaEpis = epiRepository.listarTodos().stream()
                .filter(e -> e.getId() != null)
                .collect(Collectors.toMap(Epi::getId, e -> e, (e1, e2) -> e1));

        return projecaoRepository.listarTodas().stream()
                .map(proj -> {
                    Epi epi = mapaEpis.get(proj.getEpiId());
                    String nome = epi != null ? epi.getNome() : "EPI-" + proj.getEpiId();
                    LocalDate ca = epi != null ? epi.getDataValidadeCa() : null;
                    int qtd = epi != null ? epi.getQuantidade() : 0;
                    int min = epi != null ? epi.getEstoqueMinimo() : 0;
                    return ProjecaoSubstituicaoOutputDTO.aPartirDe(proj, nome, ca, qtd, min);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjecaoSubstituicaoOutputDTO> listarProjecoesPorEpi(Integer epiId) {
        if (epiId == null) {
            return List.of();
        }
        Optional<Epi> epiOpt = epiRepository.buscarPorId(epiId);
        String nome = epiOpt.map(Epi::getNome).orElse("EPI-" + epiId);
        LocalDate ca = epiOpt.map(Epi::getDataValidadeCa).orElse(null);
        int qtd = epiOpt.map(Epi::getQuantidade).orElse(0);
        int min = epiOpt.map(Epi::getEstoqueMinimo).orElse(0);

        return projecaoRepository.listarPorEpi(epiId).stream()
                .map(proj -> ProjecaoSubstituicaoOutputDTO.aPartirDe(proj, nome, ca, qtd, min))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaSubstituicaoOutputDTO> emitirAlertasPreventivos(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }

        List<Epi> epis = epiRepository.listarTodos();
        List<AlertaSubstituicaoOutputDTO> resultado = new ArrayList<>();

        for (Epi epi : epis) {
            Integer epiId = epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
            Optional<ProjecaoSubstituicao> ultimaProjecao = projecaoRepository.buscarUltimaPorEpi(epiId);

            adicionarAlertas(resultado, epi, ultimaProjecao.orElse(null), dataReferencia);
        }

        return resultado;
    }

    private void adicionarAlertas(List<AlertaSubstituicaoOutputDTO> destino, Epi epi,
                                  ProjecaoSubstituicao projecao, LocalDate dataReferencia) {
        for (AlertaSubstituicaoEpi alerta : calculoService.emitirAlertas(epi, projecao, dataReferencia)) {
            destino.add(AlertaSubstituicaoOutputDTO.aPartirDe(alerta));
        }
    }

    /**
     * O modelo de classes não associa EPI a Setor; a ligação usa EPI.localizacao igual ao nome de um
     * setor cadastrado. Localização que não é nome de setor gera pendência, sem outra inferência.
     */
    private Optional<Setor> resolverSetorEpi(Epi epi) {
        if (epi.getLocalizacao() == null || epi.getLocalizacao().isBlank()) {
            return Optional.empty();
        }
        return setorRepository.buscarPorNome(epi.getLocalizacao().trim());
    }
}
