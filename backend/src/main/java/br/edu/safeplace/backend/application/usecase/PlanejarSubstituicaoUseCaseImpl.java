package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.GerarAnaliseProjecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlertaSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.FalhaProjecaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ProjecaoSubstituicaoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ResultadoAnaliseSubstituicaoDTO;
import br.edu.safeplace.backend.application.port.in.PlanejarSubstituicaoUseCase;
import br.edu.safeplace.backend.application.port.out.EmprestimoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.epi.AlertaSubstituicaoEpi;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import br.edu.safeplace.backend.domain.epi.SubstituicaoInteligenteService;
import br.edu.safeplace.backend.domain.epi.exception.DadosInsuficientesParaProjecaoException;

@Service
public class PlanejarSubstituicaoUseCaseImpl implements PlanejarSubstituicaoUseCase {

    private final EpiRepositoryPort epiRepository;
    private final EmprestimoRepositoryPort emprestimoRepository;
    private final SetorRepositoryPort setorRepository;
    private final ProjecaoSubstituicaoRepositoryPort projecaoRepository;
    private final SubstituicaoInteligenteService calculoService;

    public PlanejarSubstituicaoUseCaseImpl(
            EpiRepositoryPort epiRepository,
            EmprestimoRepositoryPort emprestimoRepository,
            SetorRepositoryPort setorRepository,
            ProjecaoSubstituicaoRepositoryPort projecaoRepository) {
        this.epiRepository = epiRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.setorRepository = setorRepository;
        this.projecaoRepository = projecaoRepository;
        this.calculoService = new SubstituicaoInteligenteService();
    }

    /** Construtor com serviço injetável para testes unitários */
    public PlanejarSubstituicaoUseCaseImpl(
            EpiRepositoryPort epiRepository,
            EmprestimoRepositoryPort emprestimoRepository,
            SetorRepositoryPort setorRepository,
            ProjecaoSubstituicaoRepositoryPort projecaoRepository,
            SubstituicaoInteligenteService calculoService) {
        this.epiRepository = epiRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.setorRepository = setorRepository;
        this.projecaoRepository = projecaoRepository;
        this.calculoService = calculoService;
    }

    @Override
    @Transactional
    public ResultadoAnaliseSubstituicaoDTO gerarAnalise(GerarAnaliseProjecaoInputDTO input) {
        if (input == null) {
            input = new GerarAnaliseProjecaoInputDTO(LocalDate.now(), null, 90);
        }

        LocalDate dataReferencia = input.obterDataReferencia();
        int diasAmostragem = input.obterDiasAmostragem();

        // 1. Resolução do setor global caso informado nos parâmetros
        Optional<Setor> setorParametro = Optional.empty();
        if (input.setorId() != null) {
            setorParametro = setorRepository.buscarPorId(input.setorId());
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
                continue;
            }

            NivelPerigo perigo = setorResolvido.get().getStatus();
            List<Emprestimo> emprestimos = emprestimoRepository.listarPorEpi(epiId);

            try {
                ProjecaoSubstituicao projecao = calculoService.calcularProjecao(
                        epi, perigo, emprestimos, dataReferencia, diasAmostragem);

                projecoesParaSalvar.add(projecao);

                projecoesDTO.add(ProjecaoSubstituicaoOutputDTO.aPartirDe(
                        projecao, nomeEpi, epi.getDataValidadeCa(), epi.getQuantidade(), epi.getEstoqueMinimo()));

                // Emite alertas preventivos para a projeção bem-sucedida
                List<AlertaSubstituicaoEpi> alertasEpi = calculoService.emitirAlertas(
                        epi, projecao, dataReferencia, SubstituicaoInteligenteService.DIAS_ANTECEDENCIA_PADRAO);
                for (AlertaSubstituicaoEpi al : alertasEpi) {
                    alertasDTO.add(AlertaSubstituicaoOutputDTO.aPartirDe(al));
                }

            } catch (DadosInsuficientesParaProjecaoException ex) {
                // Falha detectada conforme UC10 Exceção I: registra pendência para permitir nova tentativa
                falhasDTO.add(new FalhaProjecaoOutputDTO(epiId, nomeEpi, ex.getMessage()));
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
    public List<AlertaSubstituicaoOutputDTO> emitirAlertasPreventivos(LocalDate dataReferencia, Long diasAntecedencia) {
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        long antecedencia = (diasAntecedencia != null && diasAntecedencia > 0) ? diasAntecedencia : SubstituicaoInteligenteService.DIAS_ANTECEDENCIA_PADRAO;

        List<Epi> epis = epiRepository.listarTodos();
        List<AlertaSubstituicaoOutputDTO> resultado = new ArrayList<>();

        for (Epi epi : epis) {
            Integer epiId = epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
            Optional<ProjecaoSubstituicao> ultimaProjecao = projecaoRepository.buscarUltimaPorEpi(epiId);

            List<AlertaSubstituicaoEpi> alertas = calculoService.emitirAlertas(
                    epi, ultimaProjecao.orElse(null), dataReferencia, antecedencia);

            for (AlertaSubstituicaoEpi a : alertas) {
                resultado.add(AlertaSubstituicaoOutputDTO.aPartirDe(a));
            }
        }

        return resultado;
    }

    private Optional<Setor> resolverSetorEpi(Epi epi) {
        if (epi.getLocalizacao() == null || epi.getLocalizacao().isBlank()) {
            return Optional.empty();
        }
        String localizacao = epi.getLocalizacao().trim();
        // Tenta buscar por nome do setor
        Optional<Setor> porNome = setorRepository.buscarPorNome(localizacao);
        if (porNome.isPresent()) {
            return porNome;
        }
        // Tenta buscar por id numérico caso a localização seja um id
        try {
            int id = Integer.parseInt(localizacao);
            return setorRepository.buscarPorId(id);
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }
}
