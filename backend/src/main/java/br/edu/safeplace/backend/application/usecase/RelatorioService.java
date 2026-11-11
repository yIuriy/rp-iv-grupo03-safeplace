/*
 * Caso de uso dos relatórios estatísticos (RF18, issue #299): resolve o período, valida, escolhe o
 * relatório e o formato pelos mapas e registra a emissão do arquivo na trilha de auditoria (RNF05).
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.ArquivoRelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;
import br.edu.safeplace.backend.application.port.in.EmitirRelatorioUseCase;
import br.edu.safeplace.backend.application.port.out.FormatoRelatorio;
import br.edu.safeplace.backend.application.relatorio.Relatorio;
import br.edu.safeplace.backend.application.relatorio.TipoFormato;
import br.edu.safeplace.backend.application.relatorio.TipoRelatorio;
import br.edu.safeplace.backend.validacao.Validador;

@Service
public class RelatorioService implements EmitirRelatorioUseCase {

    static final String OPERACAO_AUDITORIA = "EMISSAO";
    static final String ENTIDADE_AUDITORIA = "Relatorio";

    private final Map<TipoRelatorio, Relatorio<?>> relatorios;
    private final Map<TipoFormato, FormatoRelatorio> formatos;
    private final Validador<PeriodoRelatorio> validador;
    private final RegistradorAuditoria auditoria;
    private final Clock clock;

    /**
     * Relatórios e formatos chegam como listas de beans e viram mapas por tipo, como as fábricas de
     * ocorrência (#239). Faltar um tipo é erro de configuração: a aplicação não sobe.
     */
    public RelatorioService(List<Relatorio<?>> relatorios, List<FormatoRelatorio> formatos,
            Validador<PeriodoRelatorio> validador, RegistradorAuditoria auditoria, Clock clock) {
        this.relatorios = porTipo(relatorios, Relatorio::tipo, TipoRelatorio.class);
        this.formatos = porTipo(formatos, FormatoRelatorio::tipo, TipoFormato.class);
        this.validador = validador;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public RelatorioOutputDTO gerar(String tipo, LocalDate inicio, LocalDate fim) {
        return relatorios.get(TipoRelatorio.de(tipo)).gerar(periodo(inicio, fim), LocalDateTime.now(clock));
    }

    @Override
    @Transactional
    public ArquivoRelatorioOutputDTO exportar(String tipo, LocalDate inicio, LocalDate fim, String formato) {
        TipoRelatorio tipoRelatorio = TipoRelatorio.de(tipo);
        TipoFormato tipoFormato = TipoFormato.de(formato);
        PeriodoRelatorio periodo = periodo(inicio, fim);

        byte[] conteudo = relatorios.get(tipoRelatorio)
                .exportar(periodo, LocalDateTime.now(clock), formatos.get(tipoFormato));

        auditoria.operacao(OPERACAO_AUDITORIA, ENTIDADE_AUDITORIA, null,
                tipoRelatorio.titulo() + " em " + tipoFormato.name() + ", período " + periodo.descrever() + ".");
        return new ArquivoRelatorioOutputDTO(nomeArquivo(tipoRelatorio, tipoFormato, periodo),
                tipoFormato.tipoConteudo(), conteudo);
    }

    /** Sem nenhuma data, o mês corrente; com as datas, o período informado, já validado. */
    private PeriodoRelatorio periodo(LocalDate inicio, LocalDate fim) {
        PeriodoRelatorio periodo = inicio == null && fim == null ? mesCorrente() : new PeriodoRelatorio(inicio, fim);
        validador.validar(periodo);
        return periodo;
    }

    private PeriodoRelatorio mesCorrente() {
        YearMonth mes = YearMonth.now(clock);
        return new PeriodoRelatorio(mes.atDay(1), mes.atEndOfMonth());
    }

    /** Ex.: relatorio-acidentes-2026-11-01-a-2026-11-30.pdf */
    static String nomeArquivo(TipoRelatorio tipo, TipoFormato formato, PeriodoRelatorio periodo) {
        return "relatorio-" + tipo.caminho() + "-" + periodo.inicio() + "-a-" + periodo.fim() + "." + formato.extensao();
    }

    private static <K extends Enum<K>, V> Map<K, V> porTipo(List<V> itens, Function<V, K> tipo, Class<K> classe) {
        Map<K, V> mapa = new EnumMap<>(classe);
        for (V item : itens) {
            if (mapa.put(tipo.apply(item), item) != null) {
                throw new IllegalStateException("Mais de uma implementação registrada para " + tipo.apply(item) + ".");
            }
        }
        for (K constante : classe.getEnumConstants()) {
            if (!mapa.containsKey(constante)) {
                throw new IllegalStateException("Nenhuma implementação registrada para " + constante + ".");
            }
        }
        return mapa;
    }
}
