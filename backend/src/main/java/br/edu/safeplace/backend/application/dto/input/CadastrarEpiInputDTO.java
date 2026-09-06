package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

public record CadastrarEpiInputDTO(
        String nome,
        String numeroCa,
        int quantidade,
        int estoqueMinimo,
        LocalDate dataValidadeCa,
        Integer vidaUtilDias
) {}
