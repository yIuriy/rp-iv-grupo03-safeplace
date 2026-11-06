package br.edu.safeplace.backend.suporte;

import java.time.LocalDate;

import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.EspecificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.FabricaEpi;
import br.edu.safeplace.backend.domain.epi.FabricaEpiPadrao;
import br.edu.safeplace.backend.domain.epi.LoteEPI;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.StatusEpi;

/**
 * Test Data Builder de {@link Epi} (#225). Cria o EPI pela {@link FabricaEpi}, como a produção, e só nos
 * testes simula um registro já gravado: id e situação lidos do banco são papel do JPA, então são preenchidos
 * por reflexão. Cada teste informa apenas o que importa para o cenário.
 */
public final class EpiDeTeste {

    private static final FabricaEpi FABRICA = new FabricaEpiPadrao();

    private Integer id;
    private String nome = "Luva de Proteção";
    private String numeroCa = "12345";
    private LocalDate validadeCa = LocalDate.now().plusYears(1);
    private int quantidade = 10;
    private int estoqueMinimo = 1;
    private StatusEpi status;
    private boolean semStatus;
    private Integer vidaUtilDias = 365;
    private String descricao;
    private ClassificacaoEPI classificacao;
    private String localizacao;
    private LoteEPI lote;

    private EpiDeTeste() {
    }

    public static EpiDeTeste umEpi() {
        return new EpiDeTeste();
    }

    public EpiDeTeste comId(Integer id) {
        this.id = id;
        return this;
    }

    public EpiDeTeste comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public EpiDeTeste comCa(String numeroCa) {
        this.numeroCa = numeroCa;
        return this;
    }

    public EpiDeTeste comValidadeCa(LocalDate validadeCa) {
        this.validadeCa = validadeCa;
        return this;
    }

    public EpiDeTeste comQuantidade(int quantidade) {
        this.quantidade = quantidade;
        return this;
    }

    public EpiDeTeste comEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
        return this;
    }

    /** Situação de um registro já gravado; sem ela, vale a situação inicial definida pela fábrica. */
    public EpiDeTeste comStatus(StatusEpi status) {
        this.status = status;
        return this;
    }

    /** Registro corrompido, sem situação: cenário das validações que recusam o status ausente. */
    public EpiDeTeste semStatus() {
        this.semStatus = true;
        return this;
    }

    public EpiDeTeste comVidaUtilDias(Integer vidaUtilDias) {
        this.vidaUtilDias = vidaUtilDias;
        return this;
    }

    public EpiDeTeste comDescricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public EpiDeTeste comClassificacao(ClassificacaoEPI classificacao) {
        this.classificacao = classificacao;
        return this;
    }

    public EpiDeTeste comLocalizacao(String localizacao) {
        this.localizacao = localizacao;
        return this;
    }

    /** Lote completo; sem ele, o lote é montado a partir do CA, da validade do CA e da quantidade. */
    public EpiDeTeste comLote(LoteEPI lote) {
        this.lote = lote;
        return this;
    }

    public Epi build() {
        EspecificacaoEPI especificacao = new EspecificacaoEPI(descricao, estoqueMinimo, classificacao);
        Epi epi = FABRICA.criar(nome, especificacao, loteDoCenario(), quantidade, vidaUtilDias, localizacao);
        if (id != null) {
            ReflectionTestUtils.setField(epi, "codigoEPI", id);
        }
        if (status != null || semStatus) {
            ReflectionTestUtils.setField(epi, "status", status);
        }
        return epi;
    }

    private LoteEPI loteDoCenario() {
        if (lote != null) {
            return lote;
        }
        return new LoteEPI(null, null, null, null, null, quantidade,
                ModeloEPI.deCadastroLegado(numeroCa, validadeCa));
    }
}
