/*
 * Acompanhamento somente leitura das capacitações (RF10, UC02, US10, issue #119). Reaproveita as
 * consultas de ConsultarCapacitacaoUseCase, que reavaliam a situação das certificações na data da
 * consulta, e a regra de pendências de PendenciasColaboradorDTO; não grava nada.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.capacitacao.AlertaCertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.AlertaTreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.AlertasCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CapacitacoesColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ColaboradorCapacitacaoResumoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.PendenciaColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.AcompanharCapacitacoesUseCase;
import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.PrazoVencimento;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusTreinamento;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class AcompanharCapacitacoesService implements AcompanharCapacitacoesUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ConsultarCapacitacaoUseCase consultarCapacitacao;
    private final Clock clock;

    public AcompanharCapacitacoesService(UsuarioRepository usuarioRepository,
                                         ConsultarCapacitacaoUseCase consultarCapacitacao) {
        this(usuarioRepository, consultarCapacitacao, Clock.systemDefaultZone());
    }

    @Autowired
    public AcompanharCapacitacoesService(UsuarioRepository usuarioRepository,
                                         ConsultarCapacitacaoUseCase consultarCapacitacao,
                                         Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.consultarCapacitacao = consultarCapacitacao;
        this.clock = java.util.Objects.requireNonNull(clock, "Clock é obrigatório.");
    }

    @Override
    public List<ColaboradorCapacitacaoResumoOutputDTO> listarColaboradores(StatusCertificacao situacao) {
        Map<Integer, List<Certificacao>> certificacoesPorColaborador = consultarCapacitacao.listarCertificacoes()
                .stream()
                .collect(Collectors.groupingBy(Certificacao::getColaboradorId));
        Map<Integer, List<Treinamento>> treinamentosPorColaborador = agruparTreinamentosPorColaborador();

        return usuarioRepository.listar().stream()
                .sorted(Comparator.comparing(Colaborador::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(colaborador -> resumir(colaborador,
                        certificacoesPorColaborador.getOrDefault(colaborador.getId(), List.of()),
                        treinamentosPorColaborador.getOrDefault(colaborador.getId(), List.of())))
                .filter(resumo -> situacao == null || situacao == resumo.situacaoCertificacoes())
                .toList();
    }

    @Override
    public CapacitacoesColaboradorOutputDTO consultarColaborador(Integer colaboradorId) {
        Colaborador colaborador = usuarioRepository.buscarPorId(colaboradorId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Colaborador não encontrado: " + colaboradorId));

        List<Certificacao> certificacoes = consultarCapacitacao.listarCertificacoes(colaborador.getId());
        List<Treinamento> treinamentos = consultarCapacitacao.listarTreinamentos(colaborador.getId());
        PendenciasColaboradorDTO pendencias = PendenciasColaboradorDTO.avaliar(colaborador.getId(), certificacoes, treinamentos);

        return new CapacitacoesColaboradorOutputDTO(
                colaborador.getId(),
                colaborador.getNome(),
                situacaoConsolidada(certificacoes),
                certificacoes.stream().map(CertificacaoOutputDTO::deDominio).toList(),
                treinamentos.stream().map(TreinamentoOutputDTO::deDominio).toList(),
                pendencias.certificacoesVencidasLista().stream().map(CertificacaoOutputDTO::deDominio).toList(),
                pendencias.alertasVencimento().stream().map(CertificacaoOutputDTO::deDominio).toList(),
                pendencias.pendenteIntegracaoAdmissional(),
                pendencias.obterMensagensAvisos());
    }

    @Override
    public AlertasCapacitacaoOutputDTO consultarAlertas() {
        LocalDate hoje = LocalDate.now(clock);
        List<Colaborador> colaboradores = usuarioRepository.listar();
        Map<Integer, String> nomes = new HashMap<>();
        colaboradores.forEach(c -> nomes.put(c.getId(), c.getNome()));

        List<Certificacao> certificacoes = consultarCapacitacao.listarCertificacoes();
        List<AlertaCertificacaoOutputDTO> proximas = new ArrayList<>();
        List<AlertaCertificacaoOutputDTO> vencidas = new ArrayList<>();
        for (Certificacao certificacao : certificacoes) {
            String nome = nomes.get(certificacao.getColaboradorId());
            if (certificacao.getStatus() == StatusCertificacao.PROXIMA_DO_VENCIMENTO) {
                proximas.add(AlertaCertificacaoOutputDTO.deDominio(certificacao, nome));
            } else if (certificacao.getStatus() == StatusCertificacao.VENCIDA) {
                vencidas.add(AlertaCertificacaoOutputDTO.deDominio(certificacao, nome));
            }
        }

        Map<Integer, List<Certificacao>> certificacoesPorColaborador = certificacoes.stream()
                .collect(Collectors.groupingBy(Certificacao::getColaboradorId));
        Map<Integer, List<Treinamento>> treinamentosPorColaborador = agruparTreinamentosPorColaborador();
        List<PendenciaColaboradorOutputDTO> pendencias = colaboradores.stream()
                .sorted(Comparator.comparing(Colaborador::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(colaborador -> PendenciasColaboradorDTO.avaliar(colaborador.getId(),
                        certificacoesPorColaborador.getOrDefault(colaborador.getId(), List.of()),
                        treinamentosPorColaborador.getOrDefault(colaborador.getId(), List.of())))
                .filter(PendenciasColaboradorDTO::pendenteIntegracaoAdmissional)
                .map(p -> new PendenciaColaboradorOutputDTO(p.colaboradorId(), nomes.get(p.colaboradorId()),
                        p.obterMensagensAvisos()))
                .toList();

        Map<StatusTreinamento, List<AlertaTreinamentoOutputDTO>> alertasTreinamento = alertarTreinamentos(hoje, nomes);

        return new AlertasCapacitacaoOutputDTO(hoje, PrazoVencimento.DIAS_ANTECEDENCIA_ALERTA,
                proximas, vencidas, pendencias,
                alertasTreinamento.getOrDefault(StatusTreinamento.PROXIMO_DO_VENCIMENTO, List.of()),
                alertasTreinamento.getOrDefault(StatusTreinamento.VENCIDO, List.of()));
    }

    private Map<StatusTreinamento, List<AlertaTreinamentoOutputDTO>> alertarTreinamentos(LocalDate hoje, Map<Integer, String> nomes) {
        return consultarCapacitacao.listarTreinamentos().stream()
                .flatMap(treinamento -> {
                    StatusTreinamento status = treinamento.calcularStatus(hoje);
                    if (status != StatusTreinamento.VENCIDO && status != StatusTreinamento.PROXIMO_DO_VENCIMENTO) {
                        return Stream.empty();
                    }
                    return treinamento.getColaboradoresIds().stream()
                            .map(colaboradorId -> AlertaTreinamentoOutputDTO.deDominio(
                                    treinamento, status, colaboradorId, nomes.get(colaboradorId)));
                })
                .sorted(Comparator.comparing(AlertaTreinamentoOutputDTO::dataVencimento)
                        .thenComparing(a -> a.colaboradorNome() == null ? "" : a.colaboradorNome(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(AlertaTreinamentoOutputDTO::status));
    }

    private ColaboradorCapacitacaoResumoOutputDTO resumir(Colaborador colaborador, List<Certificacao> certificacoes,
                                                          List<Treinamento> treinamentos) {
        PendenciasColaboradorDTO pendencias = PendenciasColaboradorDTO.avaliar(colaborador.getId(), certificacoes, treinamentos);
        return new ColaboradorCapacitacaoResumoOutputDTO(
                colaborador.getId(),
                colaborador.getNome(),
                situacaoConsolidada(certificacoes),
                certificacoes.isEmpty(),
                certificacoes.size(),
                treinamentos.size(),
                pendencias.pendenteIntegracaoAdmissional());
    }

    private Map<Integer, List<Treinamento>> agruparTreinamentosPorColaborador() {
        Map<Integer, List<Treinamento>> porColaborador = new HashMap<>();
        for (Treinamento treinamento : consultarCapacitacao.listarTreinamentos()) {
            for (Integer colaboradorId : treinamento.getColaboradoresIds()) {
                porColaborador.computeIfAbsent(colaboradorId, id -> new ArrayList<>()).add(treinamento);
            }
        }
        return porColaborador;
    }

    private static StatusCertificacao situacaoConsolidada(List<Certificacao> certificacoes) {
        return StatusCertificacao.maisCritica(certificacoes.stream().map(Certificacao::getStatus).toList());
    }
}
