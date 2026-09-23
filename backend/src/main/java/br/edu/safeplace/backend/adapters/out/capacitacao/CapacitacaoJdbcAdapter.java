package br.edu.safeplace.backend.adapters.out.capacitacao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

@Repository
public class CapacitacaoJdbcAdapter implements CapacitacaoRepositoryPort {

    private final JdbcTemplate jdbc;

    public CapacitacaoJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Certificacao> certificacaoMapper = (rs, rowNum) -> {
        Integer id = rs.getInt("id");
        String nome = rs.getString("nome");
        LocalDate dataVencimento = rs.getDate("data_vencimento").toLocalDate();
        LocalDate dataEmissao = rs.getDate("data_emissao").toLocalDate();
        Integer colaboradorId = rs.getInt("colaborador_id");
        return new Certificacao(id, nome, dataVencimento, dataEmissao, colaboradorId);
    };

    private final RowMapper<ExigenciaCapacitacao> exigenciaMapper = (rs, rowNum) -> {
        Integer id = rs.getInt("id");
        String nomeCapacitacao = rs.getString("nome_capacitacao");
        ExigenciaCapacitacao.TipoCapacitacao tipoCapacitacao = ExigenciaCapacitacao.TipoCapacitacao.valueOf(rs.getString("tipo_capacitacao"));
        Integer alvoId = rs.getInt("alvo_id");
        ExigenciaCapacitacao.TipoAlvo alvo = ExigenciaCapacitacao.TipoAlvo.valueOf(rs.getString("tipo_alvo"));
        return new ExigenciaCapacitacao(id, nomeCapacitacao, tipoCapacitacao, alvoId, alvo);
    };

    @Override
    public Certificacao salvarCertificacao(Certificacao certificacao) {
        if (certificacao.getId() == null) {
            String sql = "INSERT INTO certificacoes (nome, data_vencimento, data_emissao, colaborador_id) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, certificacao.getNome());
                ps.setDate(2, Date.valueOf(certificacao.getDataVencimento()));
                ps.setDate(3, Date.valueOf(certificacao.getDataEmissao()));
                ps.setInt(4, certificacao.getColaboradorId());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            Integer id = (key != null) ? key.intValue() : null;
            return new Certificacao(id, certificacao.getNome(), certificacao.getDataVencimento(), certificacao.getDataEmissao(), certificacao.getColaboradorId());
        } else {
            String sql = "UPDATE certificacoes SET nome = ?, data_vencimento = ?, data_emissao = ?, colaborador_id = ? WHERE id = ?";
            jdbc.update(sql, certificacao.getNome(), Date.valueOf(certificacao.getDataVencimento()), Date.valueOf(certificacao.getDataEmissao()), certificacao.getColaboradorId(), certificacao.getId());
            return certificacao;
        }
    }

    @Override
    public Treinamento salvarTreinamento(Treinamento treinamento) {
        Integer treinamentoId = treinamento.getId();
        if (treinamentoId == null) {
            String sql = "INSERT INTO treinamentos (nome, conteudo, instrutor, data_realizacao, carga_horaria) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, treinamento.getNome());
                ps.setString(2, treinamento.getConteudo());
                ps.setString(3, treinamento.getInstrutor());
                ps.setDate(4, Date.valueOf(treinamento.getDataRealizacao()));
                if (treinamento.getCargaHoraria() != null) {
                    ps.setInt(5, treinamento.getCargaHoraria());
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            treinamentoId = (key != null) ? key.intValue() : null;
        } else {
            String sql = "UPDATE treinamentos SET nome = ?, conteudo = ?, instrutor = ?, data_realizacao = ?, carga_horaria = ? WHERE id = ?";
            jdbc.update(sql, treinamento.getNome(), treinamento.getConteudo(), treinamento.getInstrutor(), Date.valueOf(treinamento.getDataRealizacao()), treinamento.getCargaHoraria(), treinamentoId);
            jdbc.update("DELETE FROM treinamentos_colaboradores WHERE treinamento_id = ?", treinamentoId);
        }

        if (treinamento.getColaboradoresIds() != null && !treinamento.getColaboradoresIds().isEmpty()) {
            String insertColabSql = "INSERT INTO treinamentos_colaboradores (treinamento_id, colaborador_id) VALUES (?, ?)";
            final Integer finalId = treinamentoId;
            List<Object[]> batchArgs = treinamento.getColaboradoresIds().stream()
                    .map(colabId -> new Object[]{finalId, colabId})
                    .toList();
            jdbc.batchUpdate(insertColabSql, batchArgs);
        }

        return new Treinamento(treinamentoId, treinamento.getNome(), treinamento.getConteudo(), treinamento.getInstrutor(),
                treinamento.getDataRealizacao(), treinamento.getCargaHoraria(), treinamento.getColaboradoresIds());
    }

    @Override
    public ExigenciaCapacitacao salvarExigencia(ExigenciaCapacitacao exigencia) {
        if (exigencia.getId() == null) {
            String sql = "INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, exigencia.getNomeCapacitacao());
                ps.setString(2, exigencia.getTipoCapacitacao().name());
                ps.setInt(3, exigencia.getAlvoId());
                ps.setString(4, exigencia.getAlvo().name());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            Integer id = (key != null) ? key.intValue() : null;
            return new ExigenciaCapacitacao(id, exigencia.getNomeCapacitacao(), exigencia.getTipoCapacitacao(), exigencia.getAlvoId(), exigencia.getAlvo());
        } else {
            String sql = "UPDATE exigencias_capacitacao SET nome_capacitacao = ?, tipo_capacitacao = ?, alvo_id = ?, tipo_alvo = ? WHERE id = ?";
            jdbc.update(sql, exigencia.getNomeCapacitacao(), exigencia.getTipoCapacitacao().name(), exigencia.getAlvoId(), exigencia.getAlvo().name(), exigencia.getId());
            return exigencia;
        }
    }

    @Override
    public List<Certificacao> buscarCertificacoesPorColaborador(Integer colaboradorId) {
        String sql = "SELECT id, nome, data_vencimento, data_emissao, colaborador_id FROM certificacoes WHERE colaborador_id = ? ORDER BY data_vencimento ASC";
        return jdbc.query(sql, certificacaoMapper, colaboradorId);
    }

    @Override
    public List<Treinamento> buscarTreinamentosPorColaborador(Integer colaboradorId) {
        String sql = """
                SELECT t.id, t.nome, t.conteudo, t.instrutor, t.data_realizacao, t.carga_horaria
                  FROM treinamentos t
                  JOIN treinamentos_colaboradores tc ON tc.treinamento_id = t.id
                 WHERE tc.colaborador_id = ?
                 ORDER BY t.data_realizacao DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Integer id = rs.getInt("id");
            String nome = rs.getString("nome");
            String conteudo = rs.getString("conteudo");
            String instrutor = rs.getString("instrutor");
            LocalDate dataRealizacao = rs.getDate("data_realizacao").toLocalDate();
            Integer cargaHoraria = (Integer) rs.getObject("carga_horaria");

            List<Integer> colabIds = buscarColaboradoresDoTreinamento(id);
            if (colabIds.isEmpty()) {
                colabIds = List.of(colaboradorId);
            }
            return new Treinamento(id, nome, conteudo, instrutor, dataRealizacao, cargaHoraria, colabIds);
        }, colaboradorId);
    }

    private List<Integer> buscarColaboradoresDoTreinamento(Integer treinamentoId) {
        String sql = "SELECT colaborador_id FROM treinamentos_colaboradores WHERE treinamento_id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getInt("colaborador_id"), treinamentoId);
    }

    @Override
    public List<ExigenciaCapacitacao> buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId) {
        String sql = "SELECT id, nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo FROM exigencias_capacitacao WHERE tipo_alvo = ? AND alvo_id = ?";
        return jdbc.query(sql, exigenciaMapper, tipoAlvo.name(), alvoId);
    }

    @Override
    public Optional<Certificacao> buscarCertificacaoPorId(Integer id) {
        String sql = "SELECT id, nome, data_vencimento, data_emissao, colaborador_id FROM certificacoes WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, certificacaoMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Treinamento> buscarTreinamentoPorId(Integer id) {
        String sql = "SELECT id, nome, conteudo, instrutor, data_realizacao, carga_horaria FROM treinamentos WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, (rs, rowNum) -> {
                String nome = rs.getString("nome");
                String conteudo = rs.getString("conteudo");
                String instrutor = rs.getString("instrutor");
                LocalDate dataRealizacao = rs.getDate("data_realizacao").toLocalDate();
                Integer cargaHoraria = (Integer) rs.getObject("carga_horaria");
                List<Integer> colabIds = buscarColaboradoresDoTreinamento(id);
                return new Treinamento(id, nome, conteudo, instrutor, dataRealizacao, cargaHoraria, colabIds.isEmpty() ? List.of(0) : colabIds);
            }, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
