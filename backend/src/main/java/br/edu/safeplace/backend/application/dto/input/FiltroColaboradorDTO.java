package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.usuario.CpfValidador;

/**
 * Filtros opcionais da listagem de colaboradores. Campos ausentes significam "sem filtro".
 */
public record FiltroColaboradorDTO(
        String nome,
        String cpf) {

    public static final FiltroColaboradorDTO SEM_FILTRO = new FiltroColaboradorDTO(null, null);

    /**
     * Normaliza os filtros: texto em branco vira ausência de filtro e o CPF é sanitizado, para
     * que a busca funcione com ou sem máscara.
     */
    public FiltroColaboradorDTO {
        nome = nome != null && !nome.isBlank() ? nome.trim() : null;
        cpf = cpf != null && !cpf.isBlank() ? CpfValidador.sanitizar(cpf) : null;
    }
}
