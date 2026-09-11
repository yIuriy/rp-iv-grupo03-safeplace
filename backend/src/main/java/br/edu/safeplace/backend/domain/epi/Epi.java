package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;

public class Epi {
    private final Integer id;
    private final String nome;
    private int quantidade;
    private final EspecificacaoEPI especificacao;
    private StatusEpi status;
    private final Integer vidaUtilDias;
    private final CertificadoAprovacao certificadoAprovacao;

    /**
     * Mantém compatibilidade com o adaptador de persistência existente.
     * Sem ID, representa criação e exige CA válido hoje.
     * Com ID, reconstrói um registro existente, inclusive com CA vencido.
     */
    public Epi(
            Integer id,
            String nome,
            String numeroCa,
            int quantidade,
            int estoqueMinimo,
            StatusEpi status,
            LocalDate dataValidadeCa,
            Integer vidaUtilDias) {
        this(
                id,
                nome,
                new CertificadoAprovacao(numeroCa, dataValidadeCa),
                quantidade,
                new EspecificacaoEPI(null, estoqueMinimo, null),
                status,
                vidaUtilDias);

        if (id == null) {
            this.certificadoAprovacao.validarParaCadastroEm(LocalDate.now());
        }
    }

    private Epi(
            Integer id,
            String nome,
            CertificadoAprovacao certificadoAprovacao,
            int quantidade,
            EspecificacaoEPI especificacao,
            StatusEpi status,
            Integer vidaUtilDias) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do EPI é obrigatório.");
        }

        if (quantidade < 0) {
            throw new IllegalArgumentException(
                    "Quantidade em estoque não pode ser negativa.");
        }

        if (especificacao == null) {
            throw new IllegalArgumentException(
                    "Especificação do EPI é obrigatória.");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status do EPI é obrigatório.");
        }

        this.id = id;
        this.nome = nome;
        this.certificadoAprovacao = certificadoAprovacao;
        this.quantidade = quantidade;
        this.especificacao = especificacao;
        this.status = status;
        this.vidaUtilDias = vidaUtilDias;
    }

    public static Epi novo(
            String nome,
            String numeroCa,
            int quantidade,
            int estoqueMinimo,
            LocalDate dataValidadeCa,
            Integer vidaUtilDias) {
        return novo(
                nome,
                numeroCa,
                quantidade,
                estoqueMinimo,
                dataValidadeCa,
                vidaUtilDias,
                LocalDate.now());
    }

    public static Epi novo(
            String nome,
            String numeroCa,
            int quantidade,
            int estoqueMinimo,
            LocalDate dataValidadeCa,
            Integer vidaUtilDias,
            LocalDate dataCadastro) {
        CertificadoAprovacao certificado = new CertificadoAprovacao(numeroCa, dataValidadeCa);

        certificado.validarParaCadastroEm(dataCadastro);

        StatusEpi statusInicial = quantidade > 0
                ? StatusEpi.DISPONIVEL
                : StatusEpi.ESGOTADO;

        return new Epi(
                null,
                nome,
                certificado,
                quantidade,
                new EspecificacaoEPI(null, estoqueMinimo, null),
                statusInicial,
                vidaUtilDias);
    }

    public MovimentacaoEstoque adicionarEstoque(int qtd, String motivo) {
        if (qtd <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade a adicionar deve ser maior que zero.");
        }

        if (qtd > Integer.MAX_VALUE - this.quantidade) {
            throw new IllegalArgumentException(
                    "Entrada excede o limite de quantidade do estoque.");
        }

        int novoSaldo = this.quantidade + qtd;

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                this.id,
                TipoMovimentacao.ENTRADA,
                qtd,
                LocalDateTime.now(),
                motivo);

        this.quantidade = novoSaldo;

        if (this.status == StatusEpi.ESGOTADO) {
            this.status = StatusEpi.DISPONIVEL;
        }

        return movimentacao;
    }

    public MovimentacaoEstoque removerEstoque(int qtd, String motivo) {
        if (qtd <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade a retirar deve ser maior que zero.");
        }

        if (qtd > this.quantidade) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente em estoque. Saldo atual: "
                            + this.quantidade
                            + ", quantidade solicitada: "
                            + qtd);
        }

        int novoSaldo = this.quantidade - qtd;

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                this.id,
                TipoMovimentacao.SAIDA,
                qtd,
                LocalDateTime.now(),
                motivo);

        this.quantidade = novoSaldo;

        if (novoSaldo == 0 && this.status == StatusEpi.DISPONIVEL) {
            this.status = StatusEpi.ESGOTADO;
        }

        return movimentacao;
    }

    public boolean isEstoqueCritico() {
        return this.quantidade <= this.especificacao.getQuantidadeMinima();
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroCa() {
        return certificadoAprovacao.numero();
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMinimo() {
        return especificacao.getQuantidadeMinima();
    }

    public StatusEpi getStatus() {
        return status;
    }

    public LocalDate getDataValidadeCa() {
        return certificadoAprovacao.dataValidade();
    }

    public CertificadoAprovacao getCertificadoAprovacao() {
        return certificadoAprovacao;
    }

    public Integer getVidaUtilDias() {
        return vidaUtilDias;
    }

    public EspecificacaoEPI getEspecificacao() {
        return especificacao;
    }
}
