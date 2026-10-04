package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Certificacao {
    /**
     * Antecedência do alerta de vencimento (UC02, regra 1; US10, regra 2).
     */
    public static final int DIAS_ANTECEDENCIA_ALERTA = 30;

    private final Integer id;
    private final String nome;
    private StatusCertificacao status;
    private final LocalDate dataVencimento;
    private final LocalDate dataEmissao;
    private final Integer colaboradorId;

    public Certificacao(Integer id, String nome, LocalDate dataVencimento, LocalDate dataEmissao, Integer colaboradorId) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da certificação é obrigatório.");
        }
        if (dataEmissao == null) {
            throw new IllegalArgumentException("Data de emissão é obrigatória.");
        }
        if (dataVencimento == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória.");
        }
        if (dataEmissao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de emissão não pode ser no futuro.");
        }
        if (validarInconsistenciaCronologica(dataEmissao, dataVencimento)) {
            throw new IllegalArgumentException("Data de emissão não pode ser posterior à data de vencimento.");
        }
        if (colaboradorId == null) {
            throw new IllegalArgumentException("Colaborador é obrigatório.");
        }

        this.id = id;
        this.nome = nome.trim();
        this.dataVencimento = dataVencimento;
        this.dataEmissao = dataEmissao;
        this.colaboradorId = colaboradorId;
        this.status = calcularStatus(LocalDate.now());
    }

    public static boolean validarInconsistenciaCronologica(LocalDate dataEmissao, LocalDate dataValidade) {
        if (dataEmissao == null || dataValidade == null) {
            return false;
        }
        return dataEmissao.isAfter(dataValidade);
    }

    public static Certificacao cadastrar(String nome, LocalDate dataVencimento, LocalDate dataEmissao, Integer colaboradorId) {
        if (dataVencimento.isBefore(LocalDate.now()) || dataVencimento.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("Data de vencimento não pode ser no passado ou presente no momento do cadastro.");
        }
        return new Certificacao(null, nome, dataVencimento, dataEmissao, colaboradorId);
    }

    public StatusCertificacao calcularStatus(LocalDate dataReferencia) {
        if (dataReferencia.isAfter(dataVencimento) || dataReferencia.isEqual(dataVencimento)) {
            return StatusCertificacao.VENCIDA;
        }
        long diasParaVencer = ChronoUnit.DAYS.between(dataReferencia, dataVencimento);
        if (diasParaVencer <= DIAS_ANTECEDENCIA_ALERTA) {
            return StatusCertificacao.PROXIMA_DO_VENCIMENTO;
        }
        return StatusCertificacao.EM_DIA;
    }

    /**
     * Indica se a certificação habilita o colaborador na data de referência. Segue a regra do
     * status: na própria data de vencimento ela já está vencida (UC02, cenário alternativo I), e a
     * alocação ou entrega que a exige é impedida (UC02, exceção I; UC12, exceção I).
     */
    public boolean estaValidaEm(LocalDate dataReferencia) {
        return calcularStatus(dataReferencia) != StatusCertificacao.VENCIDA;
    }

    public void reavaliarStatus(LocalDate dataReferencia) {
        this.status = calcularStatus(dataReferencia);
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusCertificacao getStatus() {
        return status;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public Integer getColaboradorId() {
        return colaboradorId;
    }
}
