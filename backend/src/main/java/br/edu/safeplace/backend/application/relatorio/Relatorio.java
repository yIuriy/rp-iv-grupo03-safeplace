/*
 * Base dos relatórios estatísticos (RF18, issue #299). Reúne dois padrões:
 *
 * - Template Method: gerar(periodo) é o algoritmo fixo (buscar os dados, montar o resumo, detalhar e
 *   embalar o documento); cada relatório concreto implementa só os passos buscar, resumir e detalhar.
 * - Bridge: esta hierarquia é a Abstraction. exportar escreve o documento pelas operações primitivas
 *   de um FormatoRelatorio (Implementor), sem saber se o arquivo é CSV ou PDF. Relatórios e formatos
 *   variam de forma independente: 3 relatórios e 2 formatos são 5 classes, não 6.
 */
package br.edu.safeplace.backend.application.relatorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Secao;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Tabela;
import br.edu.safeplace.backend.application.port.out.EscritaRelatorio;
import br.edu.safeplace.backend.application.port.out.FormatoRelatorio;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

/**
 * @param <D> dados que o relatório busca uma vez e reaproveita no resumo e no detalhamento
 */
public abstract class Relatorio<D> {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Tipo atendido; o RelatorioService monta o mapa de relatórios por este valor. */
    public abstract TipoRelatorio tipo();

    /**
     * Template Method: a ordem dos passos é a mesma para todo relatório e não pode ser alterada
     * pelas subclasses ({@code final}).
     */
    public final RelatorioOutputDTO gerar(PeriodoRelatorio periodo, LocalDateTime geradoEm) {
        D dados = buscar(periodo);
        List<Secao> secoes = new ArrayList<>();
        secoes.add(new Secao("Resumo", resumir(dados, periodo)));
        secoes.addAll(detalhar(dados, periodo));
        return new RelatorioOutputDTO(tipo().name(), tipo().titulo(), periodo.inicio(), periodo.fim(),
                geradoEm, semRegistros(dados), List.copyOf(secoes));
    }

    /** Aviso escrito no topo do arquivo quando o período não tem registros do assunto. */
    public static final String AVISO_SEM_REGISTROS =
            "Nenhum registro encontrado no período. Confira se as datas cadastradas estão dentro do período escolhido.";

    /**
     * Operação da Abstraction no Bridge: percorre o documento e o entrega às operações primitivas
     * do formato. Um formato novo (XLSX, por exemplo) não exige mudança em nenhum relatório.
     */
    public final byte[] exportar(PeriodoRelatorio periodo, LocalDateTime geradoEm, FormatoRelatorio formato) {
        RelatorioOutputDTO documento = gerar(periodo, geradoEm);
        EscritaRelatorio escrita = formato.iniciar(documento.titulo(),
                "Período: " + periodo.descrever() + " · Gerado em " + DATA_HORA.format(geradoEm));
        if (documento.semRegistros()) {
            escrita.aviso(AVISO_SEM_REGISTROS);
        }
        for (Secao secao : documento.secoes()) {
            escrita.secao(secao.titulo());
            secao.indicadores().forEach(indicador -> escrita.indicador(indicador.rotulo(), indicador.valor()));
            Tabela tabela = secao.tabela();
            if (tabela != null && tabela.vazia()) {
                escrita.aviso(tabela.semDados());
            } else if (tabela != null) {
                escrita.tabela(tabela.colunas(), tabela.linhas());
            }
        }
        return escrita.concluir();
    }

    /** Passo 1: busca os dados do período pelas portas de saída. */
    protected abstract D buscar(PeriodoRelatorio periodo);

    /** Passo 2: indicadores da seção "Resumo", que abre todo relatório. */
    protected abstract List<Indicador> resumir(D dados, PeriodoRelatorio periodo);

    /** Passo 3: seções de detalhe, normalmente uma tabela cada. */
    protected abstract List<Secao> detalhar(D dados, PeriodoRelatorio periodo);

    /**
     * Hook do Template Method: tem implementação padrão (nunca vazio) e cada relatório pode
     * redefini-lo dizendo o que significa "nenhum registro" para o seu assunto.
     */
    protected boolean semRegistros(D dados) {
        return false;
    }

    // Formatação comum, para que os três relatórios escrevam datas e níveis do mesmo jeito.

    protected static String data(LocalDate data) {
        return data == null ? "-" : DATA.format(data);
    }

    protected static String numero(long valor) {
        return Long.toString(valor);
    }

    protected static Indicador indicador(String rotulo, long valor) {
        return new Indicador(rotulo, numero(valor));
    }

    protected static String rotulo(NivelPerigo nivel) {
        if (nivel == null) {
            return "Não informado";
        }
        return switch (nivel) {
            case BAIXO -> "Baixo";
            case MEDIO -> "Médio";
            case ALTO -> "Alto";
            case CRITICO -> "Crítico";
        };
    }
}
