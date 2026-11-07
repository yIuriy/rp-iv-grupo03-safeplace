package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Modelo certificado de EPI, fonte única do CA (decisão #124); entidade JPA da tabela modelos_epi (#258). */
@Entity
@Table(name = "modelos_epi")
public class ModeloEPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int ca;

    private String marca;

    @Column(name = "validade_ca", nullable = false)
    private LocalDate validadeCA;

    /** Exclusivo do JPA (decisão #254). */
    protected ModeloEPI() {
    }

    public ModeloEPI(int ca, String marca, LocalDate validadeCA) {
        this(null, ca, marca, validadeCA);
    }

    public ModeloEPI(Integer id, int ca, String marca, LocalDate validadeCA) {
        this.id = id;
        this.ca = ca;
        this.marca = marca;
        this.validadeCA = validadeCA;
    }

    /**
     * Adapta o formato textual legado da API ao CA inteiro do modelo UML. Texto fora do formato vira
     * CA 0, recusado como "CA inválido."; o formato é validado antes por NumeroCaValidador (#264).
     */
    public static ModeloEPI deCadastroLegado(String numero, LocalDate validadeCA) {
        return deCadastroLegado(numero, null, validadeCA);
    }

    /** Igual a {@link #deCadastroLegado(String, LocalDate)}, preservando a marca informada no cadastro. */
    public static ModeloEPI deCadastroLegado(String numero, String marca, LocalDate validadeCA) {
        return new ModeloEPI(converterNumero(numero), normalizarMarca(marca), validadeCA);
    }

    /** Dígitos do CA, com prefixo "CA-" opcional; qualquer outro texto vira 0. */
    public static int converterNumero(String numero) {
        String normalizado = numeroSemPrefixo(numero);
        if (normalizado == null || !normalizado.matches("[0-9]+")) {
            return 0;
        }
        try {
            return Integer.parseInt(normalizado);
        } catch (NumberFormatException acimaDoLimite) {
            return 0;
        }
    }

    /** Remove espaços e o prefixo "CA-"; nulo continua nulo. */
    public static String numeroSemPrefixo(String numero) {
        if (numero == null) {
            return null;
        }
        String normalizado = numero.strip();
        return normalizado.startsWith("CA-") ? normalizado.substring(3) : normalizado;
    }

    /**
     * O CA tem fonte única no modelo (decisão #124): um novo cadastro com o mesmo CA reutiliza este
     * modelo. Validade ou marca divergentes contradizem o registro existente e são recusadas; a marca
     * ausente no modelo existente é completada pela informada. Devolve este mesmo modelo, já gravado,
     * para que o lote novo o referencie em vez de uma cópia (#258).
     */
    public ModeloEPI reutilizarPara(ModeloEPI informado) {
        if (informado == null || informado.ca != ca) {
            throw new IllegalArgumentException("O modelo informado não corresponde ao CA " + ca + ".");
        }
        if (!validadeCA.equals(informado.validadeCA)) {
            throw new IllegalArgumentException("CA " + ca + " já cadastrado com validade " + validadeCA
                    + "; informe a mesma validade do Certificado de Aprovação.");
        }
        if (marca != null && informado.marca != null && !marca.equalsIgnoreCase(informado.marca)) {
            throw new IllegalArgumentException("CA " + ca + " já cadastrado com a marca " + marca
                    + "; informe a mesma marca do modelo.");
        }
        if (marca == null) {
            marca = informado.marca;
        }
        return this;
    }

    private static String normalizarMarca(String marca) {
        return marca == null || marca.isBlank() ? null : marca.strip();
    }

    public void validarParaCadastroEm(LocalDate dataReferencia) {
        if (!verificarCA(dataReferencia)) {
            throw new IllegalArgumentException("Não é permitido cadastrar EPI com CA vencido.");
        }
    }

    public boolean verificarCA(LocalDate data) {
        if (data == null)
            throw new IllegalArgumentException("Data de referência é obrigatória.");
        return !validadeCA.isBefore(data);
    }

    public int getCa() {
        return ca;
    }

    public Integer getId() { return id; }

    public String getMarca() {
        return marca;
    }

    public LocalDate getValidadeCA() {
        return validadeCA;
    }
}
