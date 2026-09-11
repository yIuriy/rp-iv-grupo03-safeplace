package br.edu.safeplace.backend.domain.usuario;

public enum Perfil {
    COLABORADOR,
    SUPERVISOR,
    GESTOR_SEGURANCA;

    public boolean exigeCredenciais() {
        return this == SUPERVISOR || this == GESTOR_SEGURANCA;
    }
}
