package br.edu.safeplace.backend.validacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Acumula os problemas encontrados pelos elos da cadeia. Um elo pode interromper a cadeia quando
 * as regras seguintes não fazem sentido sem a dele (por exemplo, sem colaborador não há o que
 * verificar nas certificações).
 */
public final class ResultadoValidacao {

    private final List<Problema> problemas = new ArrayList<>();
    private boolean interrompido;

    public void adicionar(Problema problema) {
        problemas.add(problema);
    }

    /** Registra o problema e impede que os próximos elos sejam executados. */
    public void interromper(Problema problema) {
        adicionar(problema);
        interrompido = true;
    }

    public boolean interrompido() {
        return interrompido;
    }

    public boolean valido() {
        return problemas.isEmpty();
    }

    public List<Problema> problemas() {
        return List.copyOf(problemas);
    }

    public List<String> mensagens() {
        return problemas.stream().map(Problema::mensagem).toList();
    }
}
