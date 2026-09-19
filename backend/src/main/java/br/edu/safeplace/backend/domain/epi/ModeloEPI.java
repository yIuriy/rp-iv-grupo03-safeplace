package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

public class ModeloEPI {

    private final int ca;
    private final String marca;
    private final LocalDate validadeCA;

    public ModeloEPI(int ca, String marca, LocalDate validadeCA) {
        if (validadeCA == null) {
            throw new IllegalArgumentException(
                    "Data de validade do CA é obrigatória."
            );
        }

        this.ca = ca;
        this.marca = marca;
        this.validadeCA = validadeCA;
    }

    /** Adapta o formato textual legado da API ao CA inteiro do modelo UML. */
    public static ModeloEPI deCadastroLegado(String numero, LocalDate validadeCA) {
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
            return new ModeloEPI(Integer.parseInt(normalizado), null, validadeCA);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Número do CA excede o limite inteiro do modelo.", ex);
        }
    }

    public void validarParaCadastroEm(LocalDate dataReferencia) {
        if (!verificarCA(dataReferencia)) {
            throw new IllegalArgumentException("Não é permitido cadastrar EPI com CA vencido.");
        }
    }

    public boolean verificarCA() {
        return verificarCA(LocalDate.now());
    }

    public boolean verificarCA(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            throw new IllegalArgumentException(
                    "Data de referência é obrigatória."
            );
        }

        return !validadeCA.isBefore(dataReferencia);
    }

    public int getCa() {
        return ca;
    }

    public String getMarca() {
        return marca;
    }

    public LocalDate getValidadeCA() {
        return validadeCA;
    }
}