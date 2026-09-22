/*
 * Adaptador somente leitura da porta ImpedimentoCapacitacaoPorta sobre as tabelas do módulo de
 * capacitações (migração V15, issue #116): exigencias_capacitacao, certificacoes, treinamentos e
 * treinamentos_colaboradores. Não altera essas tabelas nem o domínio de capacitações.
 *
 * Regra aplicada (UC12, exceção I): para cada exigência vinculada ao EPI (tipo_alvo = 'EPI'),
 * uma CERTIFICACAO precisa existir para o colaborador com o mesmo nome e data de vencimento não
 * ultrapassada; um TREINAMENTO precisa constar para o colaborador com o mesmo nome. A tabela de
 * treinamentos não possui data de vencimento, então um treinamento só é impedimento quando falta;
 * a revalidação periódica depende de decisão registrada nas issues #116/#117.
 */
package br.edu.safeplace.backend.adapters.out.capacitacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.ImpedimentoCapacitacaoPorta;

@Component
public class ImpedimentoCapacitacaoJdbcAdapter implements ImpedimentoCapacitacaoPorta {
    private static final String EXIGENCIAS_DO_EPI = """
            SELECT nome_capacitacao, tipo_capacitacao
              FROM exigencias_capacitacao
             WHERE tipo_alvo = 'EPI' AND alvo_id = ?
            """;
    private static final String CERTIFICACOES_VALIDAS = """
            SELECT COUNT(*) FROM certificacoes
             WHERE colaborador_id = ? AND LOWER(nome) = LOWER(?) AND data_vencimento >= ?
            """;
    private static final String TREINAMENTOS_REALIZADOS = """
            SELECT COUNT(*) FROM treinamentos t
              JOIN treinamentos_colaboradores tc ON tc.treinamento_id = t.id
             WHERE tc.colaborador_id = ? AND LOWER(t.nome) = LOWER(?)
            """;

    private final JdbcTemplate jdbc;

    public ImpedimentoCapacitacaoJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<String> impedimentosParaEntrega(Integer colaboradorId, Integer epiId) {
        LocalDate hoje = LocalDate.now();
        List<String> impedimentos = new ArrayList<>();
        jdbc.query(EXIGENCIAS_DO_EPI, rs -> {
            String nome = rs.getString("nome_capacitacao");
            String tipo = rs.getString("tipo_capacitacao");
            if ("CERTIFICACAO".equals(tipo) && !possuiCertificacaoValida(colaboradorId, nome, hoje)) {
                impedimentos.add("certificação " + nome + " ausente ou vencida");
            } else if ("TREINAMENTO".equals(tipo) && !realizouTreinamento(colaboradorId, nome)) {
                impedimentos.add("treinamento " + nome + " não registrado");
            }
        }, epiId);
        return impedimentos;
    }

    private boolean possuiCertificacaoValida(Integer colaboradorId, String nome, LocalDate hoje) {
        Integer total = jdbc.queryForObject(CERTIFICACOES_VALIDAS, Integer.class, colaboradorId, nome, hoje);
        return Objects.requireNonNullElse(total, 0) > 0;
    }

    private boolean realizouTreinamento(Integer colaboradorId, String nome) {
        Integer total = jdbc.queryForObject(TREINAMENTOS_REALIZADOS, Integer.class, colaboradorId, nome);
        return Objects.requireNonNullElse(total, 0) > 0;
    }
}
