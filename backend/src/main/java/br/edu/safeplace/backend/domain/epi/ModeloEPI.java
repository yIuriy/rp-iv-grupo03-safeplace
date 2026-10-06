package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

public class ModeloEPI {

    private final Integer id;
    private final int ca;
    private final String marca;
    private final LocalDate validadeCA;

    public ModeloEPI(int ca, String marca, LocalDate validadeCA) {
        this(null, ca, marca, validadeCA);
    }

    public ModeloEPI(Integer id, int ca, String marca, LocalDate validadeCA) {
        if (ca <= 0)
            throw new IllegalArgumentException("CA inválido.");
        if (validadeCA == null)
            throw new IllegalArgumentException("Data de validade do CA é obrigatória.");

        this.id = id;
        this.ca = ca;
        this.marca = marca;
        this.validadeCA = validadeCA;
    }

    /** Adapta o formato textual legado da API ao CA inteiro do modelo UML. */
    public static ModeloEPI deCadastroLegado(String numero, LocalDate validadeCA) {
        return deCadastroLegado(numero, null, validadeCA);
    }

    /** Igual a {@link #deCadastroLegado(String, LocalDate)}, preservando a marca informada no cadastro. */
    public static ModeloEPI deCadastroLegado(String numero, String marca, LocalDate validadeCA) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Número do CA é obrigatório.");
        }
        String normalizado = numero.strip();
        if (normalizado.startsWith("CA-")) {
            normalizado = normalizado.substring(3);
        }
        if (!normalizado.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "Número do CA deve conter apenas dígitos, com prefixo CA- opcional.");
        }
        try {
            return new ModeloEPI(Integer.parseInt(normalizado), normalizarMarca(marca), validadeCA);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Número do CA excede o limite inteiro do modelo.", ex);
        }
    }

    /**
     * O CA tem fonte única no modelo (decisão #124): um novo cadastro com o mesmo CA reutiliza este
     * modelo. Validade ou marca divergentes contradizem o registro existente e são recusadas; a marca
     * ausente no modelo existente é completada pela informada.
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
        return new ModeloEPI(id, ca, marca != null ? marca : informado.marca, validadeCA);
    }

    private static String normalizarMarca(String marca) {
        return marca == null || marca.isBlank() ? null : marca.strip();
    }

    public void validarParaCadastroEm(LocalDate dataReferencia) {
        if (!verificarCA(dataReferencia)) {
            throw new IllegalArgumentException("Não é permitido cadastrar EPI com CA vencido.");
        }
    }

    public boolean verificarCA() {
        return verificarCA(LocalDate.now());
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
