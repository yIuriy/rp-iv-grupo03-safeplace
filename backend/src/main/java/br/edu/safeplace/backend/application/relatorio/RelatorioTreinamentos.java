/*
 * Relatório de treinamentos e certificações (RF18 e RF10, issue #299): treinamentos realizados no
 * período e certificações vencidas ou próximas do vencimento no último dia do período. A situação da
 * certificação usa a mesma regra de vencimento do UC02 (Certificacao.calcularStatus).
 */
package br.edu.safeplace.backend.application.relatorio;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Secao;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Tabela;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@Component
public class RelatorioTreinamentos extends Relatorio<RelatorioTreinamentos.Dados> {

    /**
     * Dados buscados uma vez: treinamentos do período, certificações, nomes dos colaboradores e a data
     * de referência das certificações (último dia do período).
     */
    public record Dados(List<Treinamento> realizados, List<Certificacao> certificacoes, Map<Integer, String> nomes,
            LocalDate referencia) {
    }

    private final CapacitacaoRepository capacitacoes;
    private final UsuarioRepository usuarios;

    public RelatorioTreinamentos(CapacitacaoRepository capacitacoes, UsuarioRepository usuarios) {
        this.capacitacoes = capacitacoes;
        this.usuarios = usuarios;
    }

    @Override
    public TipoRelatorio tipo() {
        return TipoRelatorio.TREINAMENTOS;
    }

    @Override
    protected Dados buscar(PeriodoRelatorio periodo) {
        List<Treinamento> realizados = capacitacoes.listarTreinamentos().stream()
                .filter(t -> periodo.contem(t.getDataRealizacao()))
                .sorted(Comparator.comparing(Treinamento::getDataRealizacao).thenComparing(Treinamento::getNome))
                .toList();
        Map<Integer, String> nomes = usuarios.listar().stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Colaborador::getId, Colaborador::getNome, (a, b) -> a));
        return new Dados(realizados, capacitacoes.listarCertificacoes(), nomes, periodo.fim());
    }

    @Override
    protected List<Indicador> resumir(Dados dados, PeriodoRelatorio periodo) {
        LocalDate referencia = periodo.fim();
        return List.of(
                indicador("Treinamentos realizados", dados.realizados().size()),
                indicador("Participações de colaboradores", dados.realizados().stream()
                        .mapToLong(t -> t.getColaboradoresIds() == null ? 0 : t.getColaboradoresIds().size()).sum()),
                indicador("Carga horária total (h)", dados.realizados().stream()
                        .mapToLong(t -> t.getCargaHoraria() == null ? 0 : t.getCargaHoraria()).sum()),
                indicador("Certificações vencidas em " + data(referencia),
                        contar(dados.certificacoes(), referencia, StatusCertificacao.VENCIDA)),
                indicador("Certificações próximas do vencimento em " + data(referencia),
                        contar(dados.certificacoes(), referencia, StatusCertificacao.PROXIMA_DO_VENCIMENTO)));
    }

    @Override
    protected List<Secao> detalhar(Dados dados, PeriodoRelatorio periodo) {
        return List.of(treinamentos(dados), certificacoesPendentes(dados, periodo.fim()));
    }

    /** Sem treinamento no período e sem certificação pendente no último dia. */
    @Override
    protected boolean semRegistros(Dados dados) {
        return dados.realizados().isEmpty() && dados.certificacoes().stream()
                .allMatch(c -> situacao(c, dados.referencia()) == StatusCertificacao.EM_DIA);
    }

    private Secao treinamentos(Dados dados) {
        List<List<String>> linhas = dados.realizados().stream()
                .map(t -> List.of(data(t.getDataRealizacao()), t.getNome(), textoOuTraco(t.getInstrutor()),
                        t.getCargaHoraria() == null ? "-" : numero(t.getCargaHoraria()),
                        numero(t.getColaboradoresIds() == null ? 0 : t.getColaboradoresIds().size())))
                .toList();
        return new Secao("Treinamentos realizados no período", new Tabela(
                List.of("Data", "Treinamento", "Instrutor", "Carga horária (h)", "Participantes"),
                linhas, "Nenhum treinamento realizado no período."));
    }

    /** Vencidas primeiro (são as que já impedem alocação), depois pela data de vencimento. */
    private Secao certificacoesPendentes(Dados dados, LocalDate referencia) {
        Function<Certificacao, StatusCertificacao> situacao = c -> situacao(c, referencia);
        List<List<String>> linhas = dados.certificacoes().stream()
                .filter(c -> situacao.apply(c) != StatusCertificacao.EM_DIA)
                .sorted(Comparator.comparing(situacao).reversed()
                        .thenComparing(Certificacao::getDataVencimento, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(c -> List.of(dados.nomes().getOrDefault(c.getColaboradorId(), "Colaborador " + c.getColaboradorId()),
                        c.getNome(), data(c.getDataVencimento()), rotulo(situacao.apply(c))))
                .toList();
        return new Secao("Certificações vencidas ou a vencer em " + data(referencia), new Tabela(
                List.of("Colaborador", "Certificação", "Vencimento", "Situação"),
                linhas, "Nenhuma certificação vencida ou próxima do vencimento."));
    }

    private static long contar(List<Certificacao> certificacoes, LocalDate referencia, StatusCertificacao status) {
        return certificacoes.stream().filter(c -> situacao(c, referencia) == status).count();
    }

    /** Certificação sem vencimento cadastrado não vence; não entra como pendência. */
    private static StatusCertificacao situacao(Certificacao certificacao, LocalDate referencia) {
        return certificacao.getDataVencimento() == null ? StatusCertificacao.EM_DIA : certificacao.calcularStatus(referencia);
    }

    private static String rotulo(StatusCertificacao status) {
        return switch (status) {
            case EM_DIA -> "Em dia";
            case PROXIMA_DO_VENCIMENTO -> "Próxima do vencimento";
            case VENCIDA -> "Vencida";
        };
    }

    private static String textoOuTraco(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }
}
