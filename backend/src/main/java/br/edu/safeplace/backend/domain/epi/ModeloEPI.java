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