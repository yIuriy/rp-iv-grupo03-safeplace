package br.edu.safeplace.backend.application.port.out;

/**
 * Porta de saída para geração da senha inicial de usuários com acesso (RF23).
 * A aleatoriedade é um efeito externo ao núcleo, por isso fica atrás de uma porta,
 * no mesmo desenho já usado por {@link CodificadorSenhaPorta}.
 */
public interface GeradorSenhaPorta {
    String gerar();
}
