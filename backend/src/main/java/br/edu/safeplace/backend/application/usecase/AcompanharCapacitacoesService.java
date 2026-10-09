/*
 * Acompanhamento somente leitura das capacitações (RF10, UC02, US10, issue #119). Reaproveita as
 * consultas de ConsultarCapacitacaoUseCase, que reavaliam a situação das certificações na data da
 * consulta, e a regra de pendências de PendenciasColaboradorDTO; não grava nada.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.capacitacao.AlertaCertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.AlertasCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CapacitacoesColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ColaboradorCapacitacaoResumoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.PendenciaColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.AcompanharCapacitacoesUseCase;
import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.PrazoVencimento;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class AcompanharCapacitacoesService implements AcompanharCapacitacoesUseCase {

    private final UsuarioRepositorioPorta usuarioRepository;
    private final ConsultarCapacitacaoUseCase consultarCapacitacao;

    public AcompanharCapacitacoesService(UsuarioRepositorioPorta usuarioRepository,
                                         ConsultarCapacitacaoUseCase consultarCapacitacao) {
        this.usuarioRepository = usuarioRepository;
        this.consultarCapacitacao = consultarCapacitacao;
    }

    @Override
    public List<ColaboradorCapacitacaoResumoOutputDTO> listarColaboradores(StatusCertificacao situacao) {
        Map<Integer, List<Certificacao>> certificacoesPorColaborador = consultarCapacitacao.listarTodasCertificacoes()
                .stream()
                .collect(Collectors.groupingBy(Certificacao::getColaboradorId));
        Map<Integer, List<Treinamento>> treinamentosPorColaborador = agruparTreinamentosPorColaborador();

        return usuarioRepository.listarTodos().stream()
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

        List<Certificacao> certificacoes = consultarCapacitacao.listarCertificacoesDoColaborador(colaborador.getId());
        List<Treinamento> treinamentos = consultarCapacitacao.listarTreinamentosDoColaborador(colaborador.getId());
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
        List<Colaborador> colaboradores = usuarioRepository.listarTodos();
        Map<Integer, String> nomes = new HashMap<>();
        colaboradores.forEach(c -> nomes.put(c.getId(), c.getNome()));

        List<Certificacao> certificacoes = consultarCapacitacao.listarTodasCertificacoes();
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

        return new AlertasCapacitacaoOutputDTO(LocalDate.now(), PrazoVencimento.DIAS_ANTECEDENCIA_ALERTA,
                proximas, vencidas, pendencias);
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
        for (Treinamento treinamento : consultarCapacitacao.listarTodosTreinamentos()) {
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
