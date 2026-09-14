package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;

public class Epi {
    private final Integer id;
    private final String nome;
    private int quantidade;
    private EspecificacaoEPI especificacao;
    private StatusEpi status;
    private final Integer vidaUtilDias;
    private final CertificadoAprovacao certificadoAprovacao;
    private String localizacao;
    private final List<ManutencaoEpi> historicoManutencao = new ArrayList<>();
    private final List<LoteEPI> lotes = new ArrayList<>();

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
            ClassificacaoEPI classificacao,
            String localizacao) {
        this(
                id,
                nome,
                new CertificadoAprovacao(numeroCa, dataValidadeCa),
                quantidade,
                new EspecificacaoEPI(descricao, estoqueMinimo, classificacao),
                status,
                vidaUtilDias,
                localizacao);

        if (id == null) {
            this.certificadoAprovacao.validarParaCadastroEm(LocalDate.now());
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
            Integer vidaUtilDias,
            String descricao,
            ClassificacaoEPI classificacao) {
        this(
                id,
                nome,
                numeroCa,
                quantidade,
                estoqueMinimo,
                status,
                dataValidadeCa,
                vidaUtilDias,
                descricao,
                classificacao,
                null);
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
                null,
                null);
    }

    /**
     * Construtor alinhado com o diagrama de classes (EPI).
     */
    public Epi(
            int codigoEPI,
            String localizacao,
            int quantidade,
            StatusEpi status) {
        this(
                codigoEPI,
                "EPI-" + codigoEPI,
                new CertificadoAprovacao("0000", LocalDate.now().plusYears(1)),
                quantidade,
                new EspecificacaoEPI("Especificação EPI", 0, null),
                status != null ? status : StatusEpi.DISPONIVEL,
                null,
                localizacao);
    }

    private Epi(
            Integer id,
            String nome,
            CertificadoAprovacao certificadoAprovacao,
            int quantidade,
            EspecificacaoEPI especificacao,
            StatusEpi status,
            Integer vidaUtilDias) {
        this(
                id,
                nome,
                certificadoAprovacao,
                quantidade,
                especificacao,
                status,
                vidaUtilDias,
                null);
    }

    private Epi(
            Integer id,
            String nome,
            CertificadoAprovacao certificadoAprovacao,
            int quantidade,
            EspecificacaoEPI especificacao,
            StatusEpi status,
            Integer vidaUtilDias,
            String localizacao) {
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
        this.localizacao = localizacao;
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
                classificacao,
                null);
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
        return novo(
                nome,
                numeroCa,
                quantidade,
                estoqueMinimo,
                dataValidadeCa,
                vidaUtilDias,
                dataCadastro,
                descricao,
                classificacao,
                null);
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
            ClassificacaoEPI classificacao,
            String localizacao) {
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
                new EspecificacaoEPI(descricao, estoqueMinimo, classificacao),
                statusInicial,
                vidaUtilDias,
                localizacao);
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

    public void validarCaValido() {
        validarCaValido(LocalDate.now());
    }

    public void validarCaValido(LocalDate dataReferencia) {
        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        if (getDataValidadeCa() != null && getDataValidadeCa().isBefore(referencia)) {
            throw new CertificadoAprovacaoVencidoException(this.id, this.nome, getNumeroCa(), getDataValidadeCa());
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
        this.historicoManutencao.add(manutencao);
        if (manutencao.getResultado() == ResultadoManutencao.APROVADO) {
            this.status = StatusEpi.DISPONIVEL;
        } else if (manutencao.getResultado() == ResultadoManutencao.REPROVADO) {
            this.status = StatusEpi.DESCARTADO;
            if (this.quantidade > 0) {
                this.quantidade--;
            }
        }
    }

    /**
     * Operações especificadas no diagrama de classes (EPI).
     */
    public List<Epi> buscarEPIs() {
        return List.of(this);
    }

    public Epi buscarEPI(int codigoEPI) {
        if (this.id != null && this.id == codigoEPI) {
            return this;
        }
        return null;
    }

    public List<ManutencaoEpi> obterHistoricoManutencao() {
        return Collections.unmodifiableList(historicoManutencao);
    }

    public void adicionarManutencao(ManutencaoEpi manutencao) {
        if (manutencao != null) {
            this.historicoManutencao.add(manutencao);
        }
    }

    public boolean validarVinculoEPIsObrigatorios() {
        if (this.status == StatusEpi.DESCARTADO) {
            return false;
        }
        if (this.certificadoAprovacao != null && this.certificadoAprovacao.estaVencidoEm(LocalDate.now())) {
            return false;
        }
        return true;
    }

    public boolean compararComQuantidadeMinima() {
        return isEstoqueCritico();
    }

    public boolean compararComQuantMinima() {
        return compararComQuantidadeMinima();
    }

    public void atualizarQuantidade(EspecificacaoEPI tipo, int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException(
                    "Quantidade em estoque não pode ser negativa.");
        }
        if (tipo != null) {
            this.especificacao = tipo;
        }
        this.quantidade = quantidade;
        if (this.quantidade == 0 && this.status == StatusEpi.DISPONIVEL) {
            this.status = StatusEpi.ESGOTADO;
        } else if (this.quantidade > 0 && this.status == StatusEpi.ESGOTADO) {
            this.status = StatusEpi.DISPONIVEL;
        }
    }

    public List<Epi> buscarEPIsAbaixoDaQuantidadeMinima() {
        if (compararComQuantidadeMinima()) {
            return List.of(this);
        }
        return List.of();
    }

    public void adicionarLote(LoteEPI lote) {
        if (lote != null) {
            this.lotes.add(lote);
        }
    }

    public List<LoteEPI> getLotes() {
        return Collections.unmodifiableList(lotes);
    }

    public LoteEPI buscarLote(int codigo) {
        for (LoteEPI lote : lotes) {
            if (lote.getModelo() != null && lote.getModelo().getCa() == codigo) {
                return lote;
            }
            try {
                if (Integer.parseInt(lote.getNumeroLote()) == codigo) {
                    return lote;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public int validarBaixa(int codigo, int quantidade) {
        if (this.id != null && this.id != codigo) {
            throw new IllegalArgumentException("Código do EPI informado não corresponde ao EPI atual.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade para baixa deve ser maior que zero.");
        }
        if (quantidade > this.quantidade) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente em estoque. Saldo atual: "
                            + this.quantidade
                            + ", quantidade solicitada: "
                            + quantidade);
        }
        return this.quantidade - quantidade;
    }

    public boolean darBaixa(int quantidade) {
        if (quantidade <= 0 || quantidade > this.quantidade) {
            return false;
        }
        removerEstoque(quantidade, "Baixa de estoque");
        return true;
    }

    public Integer getId() {
        return id;
    }

    public int getCodigoEPI() {
        return id != null ? id : 0;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
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

    public int getQuantidate() {
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
