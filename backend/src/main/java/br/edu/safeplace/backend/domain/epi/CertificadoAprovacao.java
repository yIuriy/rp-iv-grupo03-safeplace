package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

public record CertificadoAprovacao(
        String numero,
        LocalDate dataValidade) {

    public CertificadoAprovacao {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException(
                    "Número do CA é obrigatório.");
        }

        String numeroNormalizado = numero.strip();

        if (numeroNormalizado.startsWith(("CA-"))) {
            numeroNormalizado = numeroNormalizado.substring(3);
        }

        // Ao chegar aqui, o CA- já vai ter sido retirado
        if (!numeroNormalizado.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "Número do CA deve conter apenas dígitos, com prefixo CA- opcional.");
        }

        if (dataValidade == null) {
            throw new IllegalArgumentException(
                    "Data de validade do CA é obrigatória.");
        }

        numero = numeroNormalizado;
    }

    public boolean estaVencidoEm(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            throw new IllegalArgumentException(
                    "Data de referência é obrigatória.");
        }

        return dataValidade.isBefore(dataReferencia);
    }

    public void validarParaCadastroEm(LocalDate dataReferencia) {
        if (estaVencidoEm(dataReferencia)) {
            throw new IllegalArgumentException(
                    "Não é permitido cadastrar EPI com CA vencido.");
        }
    }
}
