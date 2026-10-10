package br.edu.safeplace.backend.domain.usuario;

public enum Perfil {
    SUPERVISOR,
    GESTOR_SEGURANCA;

    public boolean exigeCredenciais() {
        return true;
    }
}
