/*
 * Entidade de domínio Emprestimo (RF11, UC12, US11, issue #106). Segue o diagrama de classes:
 * dataRetirada, dataDevolucaoPrevista, dataDevolucaoReal e status: ClassificacaoEmprestimo, com
 * as associações Colaborador (beneficiário) e EPI. UC12 exige ainda data/hora e o responsável pela
 * entrega, o estado na devolução e o termo de cautela; esses dados ficam aqui como campos de apoio.
 *
 * Decisões registradas na issue #106: cada empréstimo retém uma unidade do registro de EPI (a
 * decisão #124 fixa EPI como saldo de um lote/modelo, sem ItemEPI); a data prevista é informada
 * pelo operador; o atraso é calculado na consulta, não gravado.
 */
package br.edu.safeplace.backend.domain.emprestimo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;

public class Emprestimo {
    /** Unidades retidas por empréstimo: uma, conforme a decisão registrada na issue #106. */
    public static final int QUANTIDADE_POR_EMPRESTIMO = 1;

    private final Integer id;
    private final Integer epiId;
    private final Integer colaboradorId;
    private final Integer responsavelEntregaId;
    private final LocalDateTime dataRetirada;
    private final LocalDate dataDevolucaoPrevista;
    private final String termoCautela;
    private final String justificativaSubstituicao;
    private LocalDateTime dataDevolucaoReal;
    private Integer responsavelDevolucaoId;
    private EstadoDevolucao estadoDevolucao;
    private ClassificacaoEmprestimo status;

    public Emprestimo(Integer id, Integer epiId, Integer colaboradorId, Integer responsavelEntregaId,
                      LocalDateTime dataRetirada, LocalDate dataDevolucaoPrevista, String termoCautela,
                      String justificativaSubstituicao, LocalDateTime dataDevolucaoReal,
                      Integer responsavelDevolucaoId, EstadoDevolucao estadoDevolucao,
                      ClassificacaoEmprestimo status) {
        // Os dados da entrega e a coerência com a devolução são validados por EmprestimoValidador
        // antes do repository (issue #265).

        this.id = id;
        this.epiId = epiId;
        this.colaboradorId = colaboradorId;
        this.responsavelEntregaId = responsavelEntregaId;
        this.dataRetirada = dataRetirada;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.termoCautela = termoCautela != null ? termoCautela.trim() : null;
        this.justificativaSubstituicao = justificativaSubstituicao;
        this.dataDevolucaoReal = dataDevolucaoReal;
        this.responsavelDevolucaoId = responsavelDevolucaoId;
        this.estadoDevolucao = estadoDevolucao;
        this.status = status;
    }

    /** Passo 6 do UC12: registra a entrega com data, hora, responsável e termo de cautela. */
    public static Emprestimo registrarEntrega(Integer epiId, Integer colaboradorId, Integer responsavelEntregaId,
                                              LocalDateTime dataRetirada, LocalDate dataDevolucaoPrevista,
                                              String termoCautela, String justificativaSubstituicao) {
        return new Emprestimo(null, epiId, colaboradorId, responsavelEntregaId, dataRetirada,
                dataDevolucaoPrevista, termoCautela, justificativaSubstituicao,
                null, null, null, ClassificacaoEmprestimo.INDISPONIVEL);
    }

    /** Cenário alternativo I do UC12: encerra o empréstimo e guarda o estado do item devolvido. */
    public void devolver(EstadoDevolucao estado, Integer responsavelDevolucaoId, LocalDateTime dataHora) {
        if (!estaAtivo()) {
            throw new EmprestimoJaEncerradoException(id);
        }
        exigir(estado != null, "Estado na devolução é obrigatório.");
        exigir(responsavelDevolucaoId != null, "Responsável pela devolução é obrigatório.");
        exigir(dataHora != null, "Data e hora da devolução são obrigatórias.");
        exigir(!dataHora.isBefore(dataRetirada), "Devolução não pode ser anterior à retirada.");
        this.estadoDevolucao = estado;
        this.responsavelDevolucaoId = responsavelDevolucaoId;
        this.dataDevolucaoReal = dataHora;
        this.status = ClassificacaoEmprestimo.DISPONIVEL;
    }

    public boolean estaAtivo() {
        return status.ativo();
    }

    /** US11, regra 6: sem devolução após a data prevista, o empréstimo está atrasado. */
    public boolean estaAtrasadoEm(LocalDate hoje) {
        return estaAtivo() && hoje.isAfter(dataDevolucaoPrevista);
    }

    /** Classificação vigente na data informada; o atraso não é gravado, é calculado na consulta. */
    public ClassificacaoEmprestimo classificarEm(LocalDate hoje) {
        if (estaAtrasadoEm(hoje)) {
            return ClassificacaoEmprestimo.DEVOLUCAO_ATRASADA;
        }
        return status;
    }

    public Integer getId() {
        return id;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public Integer getColaboradorId() {
        return colaboradorId;
    }

    public Integer getResponsavelEntregaId() {
        return responsavelEntregaId;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public LocalDateTime getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }

    public Integer getResponsavelDevolucaoId() {
        return responsavelDevolucaoId;
    }

    public EstadoDevolucao getEstadoDevolucao() {
        return estadoDevolucao;
    }

    public ClassificacaoEmprestimo getStatus() {
        return status;
    }

    public String getTermoCautela() {
        return termoCautela;
    }

    public String getJustificativaSubstituicao() {
        return justificativaSubstituicao;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
