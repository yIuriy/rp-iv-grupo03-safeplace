package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import br.edu.safeplace.backend.domain.epi.exception.TransicaoStatusEpiInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Saldo de um lote de EPI (decisão #124) e entidade JPA da tabela epis (#258). CA e validade do CA vêm
 * apenas do modelo do lote.
 */
@Entity
@Table(name = "epis")
public class Epi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer codigoEPI;

    @Column(columnDefinition = "text")
    private String localizacao;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false)
    private int quantidade;

    @Embedded
    private EspecificacaoEPI especificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusEpi status;

    @Column(name = "vida_util_dias")
    private Integer vidaUtilDias;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteEPI lote;

    /** O histórico é gravado pela manutenção (ManutencaoEpi ainda não é entidade, #259). */
    @Transient
    private final List<ManutencaoEpi> historicoManutencao = new ArrayList<>();

    /** Sem persistência: pela decisão #124, o registro tem um único lote. */
    @Transient
    private final List<LoteEPI> lotesAdicionais = new ArrayList<>();

    /** Exclusivo do JPA (decisão #254). */
    protected Epi() {
    }

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
        // O CA vigente no cadastro é verificado por EpiValidador antes de salvar (issue #264).
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

    /** Compatibilidade com a assinatura reduzida do diagrama. */
    public Epi(int codigoEPI, String localizacao, int quantidade, StatusEpi status) {
        this(codigoEPI, "EPI-" + codigoEPI, "1", quantidade, 0,
                status != null ? status : StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), null, null, null, localizacao);
    }

    private Epi(
            Integer id,
            String nome,
            LoteEPI lote,
            int quantidade,
            EspecificacaoEPI especificacao,
            StatusEpi status,
            Integer vidaUtilDias, String localizacao) {
        // Regras de cadastro em EpiValidador, antes do repository (issue #264).
        this.codigoEPI = id;
        this.nome = nome;
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

    /** Usado só pela {@link FabricaEpiPadrao}: criar um EPI novo passa pela {@link FabricaEpi} (#225). */
    Epi(String nome, EspecificacaoEPI especificacao, LoteEPI lote, int quantidade, StatusEpi status,
            Integer vidaUtilDias, String localizacao) {
        this.nome = nome;
        this.especificacao = especificacao;
        this.lote = lote;
        this.quantidade = quantidade;
        this.status = status;
        this.vidaUtilDias = vidaUtilDias;
        this.localizacao = localizacao;
    }

    public MovimentacaoEstoque adicionarEstoque(int qtd, String motivo) {
        return adicionarEstoque(qtd, motivo, null);
    }

    public MovimentacaoEstoque adicionarEstoque(int qtd, String motivo, Colaborador responsavel) {
        if (qtd <= 0) {
            throw new IllegalArgumentException(
                    "Quantidade a adicionar deve ser maior que zero.");
        }

        if (qtd > Integer.MAX_VALUE - this.quantidade) {
            throw new IllegalArgumentException(
                    "Entrada excede o limite de quantidade do estoque.");
        }

        // UC06: toda movimentação registra o motivo. A regra é da operação, e não da criação do
        // objeto, para que o saldo nunca mude sem uma movimentação válida (issue #264).
        exigirMotivo(motivo);

        int novoSaldo = this.quantidade + qtd;

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                this,
                this.lote,
                responsavel,
                TipoMovimentacao.ENTRADA,
                qtd,
                LocalDateTime.now(),
                motivo);

        this.quantidade = novoSaldo;
        acompanharSaldo();

        return movimentacao;
    }

    private static void exigirMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo da movimentação é obrigatório.");
        }
    }

    public MovimentacaoEstoque removerEstoque(int qtd, String motivo) {
        return removerEstoque(qtd, motivo, null);
    }

    public MovimentacaoEstoque removerEstoque(int qtd, String motivo, Colaborador responsavel) {
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

        // UC06: toda movimentação registra o motivo. A regra é da operação, e não da criação do
        // objeto, para que o saldo nunca mude sem uma movimentação válida (issue #264).
        exigirMotivo(motivo);

        int novoSaldo = this.quantidade - qtd;

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                this,
                this.lote,
                responsavel,
                TipoMovimentacao.SAIDA,
                qtd,
                LocalDateTime.now(),
                motivo);

        this.quantidade = novoSaldo;
        acompanharSaldo();

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
        transicionarPara(StatusEpi.EM_MANUTENCAO);
    }

    /**
     * Registra uma manutenção concluída (UC01). Um EPI disponível passa antes por {@code EM_MANUTENCAO}, para que a
     * conclusão parta sempre da situação de manutenção.
     */
    public void registrarManutencao(ManutencaoEpi manutencao, LocalDate dataReferencia) {
        if (this.status == StatusEpi.DISPONIVEL) {
            enviarParaManutencao(dataReferencia);
        }
        concluirManutencao(manutencao);
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
        this.historicoManutencao.add(manutencao);
        if (manutencao.getResultado() == ResultadoManutencao.APROVADO) {
            transicionarPara(StatusEpi.DISPONIVEL);
        }
        // Reprovação preserva a indisponibilidade atual e não executa descarte (RF14).
        // O estado definitivo da reprovação depende da decisão de domínio #124.
    }

    public List<Epi> buscarEPIs() {
        return List.of(this);
    }

    public Epi buscarEPI(int codigoEPI) {
        return this.codigoEPI != null && this.codigoEPI == codigoEPI ? this : null;
    }

    public List<ManutencaoEpi> obterHistoricoManutencao() {
        return Collections.unmodifiableList(historicoManutencao);
    }

    public void adicionarManutencao(ManutencaoEpi manutencao) {
        if (manutencao != null) {
            historicoManutencao.add(manutencao);
        }
    }

    public void registrarDevolucaoManutencao(ManutencaoEpi manutencao) {
        adicionarManutencao(manutencao);
        if (this.quantidade == 0) {
            transicionarPara(StatusEpi.EM_MANUTENCAO);
        }
    }

    public void registrarDevolucaoDescarte() {
        if (this.quantidade == 0) {
            transicionarPara(StatusEpi.DESCARTADO);
        }
    }

    public boolean validarVinculoEPIsObrigatorios() {
        return status != StatusEpi.DESCARTADO && lote.getModelo().verificarCA(LocalDate.now());
    }

    public boolean compararComQuantidadeMinima() {
        return isEstoqueCritico();
    }

    public boolean compararComQuantMinima() {
        return compararComQuantidadeMinima();
    }

    public void atualizarQuantidade(EspecificacaoEPI especificacao, int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
        }
        if (especificacao != null) {
            this.especificacao = especificacao;
        }
        this.quantidade = quantidade;
        acompanharSaldo();
    }

    /**
     * Único ponto de mudança de situação (State, #224): a situação atual decide se o destino é permitido.
     * Pedir a situação em que o EPI já está não muda nada.
     */
    void transicionarPara(StatusEpi novoStatus) {
        if (novoStatus == this.status) {
            return;
        }
        if (this.status != null && !this.status.podeTransicaoPara(novoStatus)) {
            throw new TransicaoStatusEpiInvalidaException(this.codigoEPI, this.status, novoStatus);
        }
        this.status = novoStatus;
    }

    private void acompanharSaldo() {
        if (this.status != null) {
            transicionarPara(this.status.aposMudancaDeSaldo(this.quantidade));
        }
    }

    public List<Epi> buscarEPIsAbaixoDaQuantidadeMinima() {
        return compararComQuantidadeMinima() ? List.of(this) : List.of();
    }

    public void adicionarLote(LoteEPI lote) {
        if (lote != null) {
            lotesAdicionais.add(lote);
        }
    }

    public List<LoteEPI> getLotes() {
        return Collections.unmodifiableList(lotesAdicionais);
    }

    public LoteEPI buscarLote(int codigo) {
        return lotesAdicionais.stream()
                .filter(lote -> lote.getModelo().getCa() == codigo
                        || Integer.toString(codigo).equals(lote.getNumeroLote()))
                .findFirst()
                .orElse(null);
    }

    public int validarBaixa(int codigo, int quantidade) {
        if (codigoEPI != null && codigoEPI != codigo) {
            throw new IllegalArgumentException("Código do EPI informado não corresponde ao EPI atual.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade para baixa deve ser maior que zero.");
        }
        if (quantidade > this.quantidade) {
            throw new SaldoInsuficienteException("Saldo insuficiente em estoque. Saldo atual: "
                    + this.quantidade + ", quantidade solicitada: " + quantidade);
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

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Epi epi)) {
            return false;
        }
        return getId() != null && getId().equals(epi.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
