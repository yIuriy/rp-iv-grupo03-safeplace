package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Epi {
    private final Integer id;
    private final String nome;
    private final String numeroCa;
    private int quantidade;
    private final int estoqueMinimo;
    private StatusEpi status;
    private final LocalDate dataValidadeCa;
    private final Integer vidaUtilDias;

    public Epi(Integer id, String nome, String numeroCa, int quantidade, int estoqueMinimo,
               StatusEpi status, LocalDate dataValidadeCa, Integer vidaUtilDias) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do EPI é obrigatório.");
        }
        if (numeroCa == null || numeroCa.isBlank()) {
            throw new IllegalArgumentException("Número do CA é obrigatório.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
        }
        if (estoqueMinimo < 0) {
            throw new IllegalArgumentException("Estoque mínimo não pode ser negativo.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status do EPI é obrigatório.");
        }

        this.id = id;
        this.nome = nome;
        this.numeroCa = numeroCa;
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
        this.status = status;
        this.dataValidadeCa = dataValidadeCa;
        this.vidaUtilDias = vidaUtilDias;
    }

    public static Epi novo(String nome, String numeroCa, int quantidade, int estoqueMinimo,
                           LocalDate dataValidadeCa, Integer vidaUtilDias) {
        StatusEpi statusInicial = quantidade > 0 ? StatusEpi.DISPONIVEL : StatusEpi.ESGOTADO;
        return new Epi(null, nome, numeroCa, quantidade, estoqueMinimo, statusInicial, dataValidadeCa, vidaUtilDias);
    }

    public MovimentacaoEstoque adicionarEstoque(int qtd, String motivo) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade a adicionar deve ser maior que zero.");
        }
        this.quantidade += qtd;
        if (this.status == StatusEpi.ESGOTADO && this.quantidade > 0) {
            this.status = StatusEpi.DISPONIVEL;
        }
        return new MovimentacaoEstoque(null, this.id, TipoMovimentacao.ENTRADA, qtd, LocalDateTime.now(), motivo);
    }

    public MovimentacaoEstoque removerEstoque(int qtd, String motivo) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade a retirar deve ser maior que zero.");
        }
        if (qtd > this.quantidade) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente em estoque. Saldo atual: " + this.quantidade + ", quantidade solicitada: " + qtd
            );
        }
        this.quantidade -= qtd;
        if (this.quantidade == 0 && this.status == StatusEpi.DISPONIVEL) {
            this.status = StatusEpi.ESGOTADO;
        }
        return new MovimentacaoEstoque(null, this.id, TipoMovimentacao.SAIDA, qtd, LocalDateTime.now(), motivo);
    }

    public boolean isEstoqueCritico() {
        return this.quantidade <= this.estoqueMinimo;
    }

    public void validarCaValido() {
        validarCaValido(LocalDate.now());
    }

    public void validarCaValido(LocalDate dataReferencia) {
        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        if (this.dataValidadeCa != null && this.dataValidadeCa.isBefore(referencia)) {
            throw new CertificadoAprovacaoVencidoException(this.id, this.nome, this.numeroCa, this.dataValidadeCa);
        }
    }

    public void enviarParaManutencao() {
        enviarParaManutencao(LocalDate.now());
    }

    public void enviarParaManutencao(LocalDate dataReferencia) {
        validarCaValido(dataReferencia);
        if (this.status != StatusEpi.DISPONIVEL) {
            throw new EpiIndisponivelParaManutencaoException(
                    this.id, this.status, "Apenas EPIs com status DISPONIVEL podem ser enviados para manutenção."
            );
        }
        this.status = StatusEpi.EM_MANUTENCAO;
    }

    public void concluirManutencao(ManutencaoEpi manutencao) {
        if (manutencao == null) {
            throw new IllegalArgumentException("Registro de manutenção é obrigatório.");
        }
        if (this.status != StatusEpi.EM_MANUTENCAO) {
            throw new EpiIndisponivelParaManutencaoException(
                    this.id, this.status, "Apenas EPIs com status EM_MANUTENCAO podem concluir manutenção."
            );
        }
        if (manutencao.getResultado() == ResultadoManutencao.APROVADO) {
            this.status = StatusEpi.DISPONIVEL;
        } else if (manutencao.getResultado() == ResultadoManutencao.REPROVADO) {
            this.status = StatusEpi.DESCARTADO;
            if (this.quantidade > 0) {
                this.quantidade--;
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroCa() {
        return numeroCa;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public StatusEpi getStatus() {
        return status;
    }

    public LocalDate getDataValidadeCa() {
        return dataValidadeCa;
    }

    public Integer getVidaUtilDias() {
        return vidaUtilDias;
    }
}
