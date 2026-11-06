package br.edu.safeplace.backend.suporte;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Objetos associados mínimos para os testes que só precisam do id de um EPI ou de uma pessoa, desde que as
 * associações passaram a ser por objeto (#260). Id nulo devolve associação ausente.
 */
public final class ReferenciasDeTeste {

    private ReferenciasDeTeste() {
    }

    public static Epi epi(Integer id) {
        return id == null ? null : EpiDeTeste.umEpi().comId(id).build();
    }

    public static Colaborador pessoa(Integer id) {
        return id == null ? null : new Colaborador(id, "52998224725", "Pessoa " + id, LocalDate.of(1990, 1, 1),
                null, true, LocalDateTime.now(), LocalDateTime.now());
    }
}
