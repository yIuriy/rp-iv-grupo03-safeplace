package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;

public class Epi {
    private final Integer codigoEPI;
    private final String localizacao;
    private final String nome;
    private int quantidade;
    private final EspecificacaoEPI especificacao;
    private StatusEpi status;
    private final Integer vidaUtilDias;
    private final LoteEPI lote;

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
            Integer vidaUtilDias,
            String descricao,
            ClassificacaoEPI classificacao) {
        this(id, nome, numeroCa, quantidade, estoqueMinimo, status, dataValidadeCa,
                vidaUtilDias, descricao, classificacao, null);
    }

    public Epi(Integer id, String nome, String numeroCa, int quantidade, int estoqueMinimo,
               StatusEpi status, LocalDate dataValidadeCa, Integer vidaUtilDias,
               String descricao, ClassificacaoEPI classificacao, String localizacao) {
        this(
                id,
                nome,
                loteLegado(numeroCa, dataValidadeCa, quantidade),
                quantidade,
                new EspecificacaoEPI(descricao, estoqueMinimo, classificacao),
                status,
                vidaUtilDias, localizacao);

        if (id == null) {
            this.lote.getModelo().validarParaCadastroEm(LocalDate.now());
        }
    }

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
                numeroCa,
                quantidade,
                estoqueMinimo,
                status,
                dataValidadeCa,
                vidaUtilDias,
                null,
                null);
    }

    private Epi(
            Integer id,
            String nome,
            LoteEPI lote,
            int quantidade,
            EspecificacaoEPI especificacao,
            StatusEpi status,
            Integer vidaUtilDias, String localizacao) {
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

        this.codigoEPI = id;
        this.nome = nome;
        if (lote == null) {
            throw new IllegalArgumentException("Lote do EPI é obrigatório.");
        }
        this.lote = lote;
        this.localizacao = localizacao;
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
            Integer vidaUtilDias,
            String descricao,
            ClassificacaoEPI classificacao) {
        return novo(
                nome,
                numeroCa,
                quantidade,
                estoqueMinimo,
                dataValidadeCa,
                vidaUtilDias,
                LocalDate.now(), 
                descricao, 
                classificacao);
    }

    public static Epi novo(
            String nome,
            String numeroCa,
            int quantidade,
            int estoqueMinimo,
            LocalDate dataValidadeCa,
            Integer vidaUtilDias,
            String descricao,
            ClassificacaoEPI classificacao,
            String localizacao) {
        return novo(nome, numeroCa, quantidade, estoqueMinimo, dataValidadeCa,
                vidaUtilDias, LocalDate.now(), descricao, classificacao, localizacao);
    }

    public static Epi novo(
            String nome,
            String numeroCa,
            int quantidade,
            int estoqueMinimo,
            LocalDate dataValidadeCa,
            Integer vidaUtilDias,
            LocalDate dataCadastro,
            String descricao,
            ClassificacaoEPI classificacao) {
        return novo(nome, numeroCa, quantidade, estoqueMinimo, dataValidadeCa,
                vidaUtilDias, dataCadastro, descricao, classificacao, null);
    }

    public static Epi novo(String nome, String numeroCa, int quantidade, int estoqueMinimo,
                           LocalDate dataValidadeCa, Integer vidaUtilDias, LocalDate dataCadastro,
                           String descricao, ClassificacaoEPI classificacao, String localizacao) {
        LoteEPI lote = loteLegado(numeroCa, dataValidadeCa, quantidade);
        lote.getModelo().validarParaCadastroEm(dataCadastro);

        StatusEpi statusInicial = quantidade > 0
                ? StatusEpi.DISPONIVEL
                : StatusEpi.ESGOTADO;

        return new Epi(
                null,
                nome,
                lote,
                quantidade,
                new EspecificacaoEPI(descricao, estoqueMinimo, classificacao),
                statusInicial,
                vidaUtilDias, localizacao);
    }

    public static Epi novo(String nome, LoteEPI lote, int quantidade, int estoqueMinimo,
                           Integer vidaUtilDias, String descricao,
                           ClassificacaoEPI classificacao, String localizacao) {
        lote.getModelo().validarParaCadastroEm(LocalDate.now());
        return new Epi(null, nome, lote, quantidade,
                new EspecificacaoEPI(descricao, estoqueMinimo, classificacao),
                quantidade > 0 ? StatusEpi.DISPONIVEL : StatusEpi.ESGOTADO,
                vidaUtilDias, localizacao);
    }

    public static Epi reconstituir(Integer codigoEPI, String nome, LoteEPI lote,
                                   int quantidade, int estoqueMinimo, StatusEpi status,
                                   Integer vidaUtilDias, String descricao,
                                   ClassificacaoEPI classificacao, String localizacao) {
        return new Epi(codigoEPI, nome, lote, quantidade,
                new EspecificacaoEPI(descricao, estoqueMinimo, classificacao),
                status, vidaUtilDias, localizacao);
    }

    public MovimentacaoEstoque adicionarEstoque(int qtd, String motivo) {
        return adicionarEstoque(qtd, motivo, null);
    }

    public MovimentacaoEstoque adicionarEstoque(int qtd, String motivo, Integer responsavelId) {
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
                this.codigoEPI,
                this.lote.getId(),
                responsavelId,
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
        return removerEstoque(qtd, motivo, null);
    }

    public MovimentacaoEstoque removerEstoque(int qtd, String motivo, Integer responsavelId) {
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
                this.codigoEPI,
                this.lote.getId(),
                responsavelId,
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

    public void validarCaValido() {
        validarCaValido(LocalDate.now());
    }

    public void validarCaValido(LocalDate dataReferencia) {
        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        if (!lote.getModelo().verificarCA(referencia)) {
            throw new CertificadoAprovacaoVencidoException(this.codigoEPI, this.nome, getNumeroCa(), getDataValidadeCa());
        }
    }

    public void enviarParaManutencao() {
        enviarParaManutencao(LocalDate.now());
    }

    public void enviarParaManutencao(LocalDate dataReferencia) {
        validarCaValido(dataReferencia);
        if (this.status != StatusEpi.DISPONIVEL) {
            throw new EpiIndisponivelParaManutencaoException(
                    this.codigoEPI, this.status, "Apenas EPIs com status DISPONIVEL podem ser enviados para manutenção."
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
                    this.codigoEPI, this.status, "Apenas EPIs com status EM_MANUTENCAO podem concluir manutenção."
            );
        }
        if (manutencao.getResultado() == ResultadoManutencao.APROVADO) {
            this.status = StatusEpi.DISPONIVEL;
        }
        // Reprovação preserva a indisponibilidade atual e não executa descarte (RF14).
        // O estado definitivo da reprovação depende da decisão de domínio #124.
    }

    public Integer getCodigoEPI() {
        return codigoEPI;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    /** Alias mantido para os contratos HTTP e JPA existentes. */
    public Integer getId() {
        return codigoEPI;
    }

    public String getNome() {
        return nome;
    }

    public String getNumeroCa() {
        return Integer.toString(lote.getModelo().getCa());
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
        return lote.getModelo().getValidadeCA();
    }

    public ModeloEPI getModelo() {
        return lote.getModelo();
    }

    public LoteEPI getLote() {
        return lote;
    }

    public Integer getVidaUtilDias() {
        return vidaUtilDias;
    }

    public EspecificacaoEPI getEspecificacao() {
        return especificacao;
    }

    private static LoteEPI loteLegado(String numeroCa, LocalDate dataValidadeCa, int quantidade) {
        return new LoteEPI(null, null, null, null, quantidade,
                ModeloEPI.deCadastroLegado(numeroCa, dataValidadeCa));
    }
}
