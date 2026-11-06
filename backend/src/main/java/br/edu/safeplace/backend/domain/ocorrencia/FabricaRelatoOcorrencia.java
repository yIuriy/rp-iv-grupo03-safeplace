package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Abstract Factory (GoF, criacional) para relato de ocorrências (#239).
 *
 * <p>Cada tipo de ocorrência forma uma família coerente de objetos que precisam combinar entre si:
 * a entidade de domínio ({@link Ocorrencia}), a notificação interna ao gestor
 * ({@link NotificacaoOcorrencia}) e a descrição/entidade para auditoria. Uma fábrica concreta por
 * tipo instancia a família inteira, permitindo que os casos de uso operem apenas sobre esta interface.
 *
 * @param <I> Tipo do objeto de entrada contendo os dados do relato
 * @param <O> Tipo concreto da ocorrência produzida pela família
 */
public interface FabricaRelatoOcorrencia<I, O extends Ocorrencia> {

    /** Identificador do tipo de ocorrência atendido por esta fábrica. */
    TipoOcorrencia getTipo();

    /**
     * Cria a ocorrência da família a partir do objeto de entrada e das associações resolvidas.
     *
     * @param input DTO de entrada completo com os dados do relato
     * @param area Área de risco resolvida
     * @param colaborador Colaborador envolvido resolvido
     * @param relator Colaborador autenticado que registrou o relato
     * @param protocolo Protocolo CAT gerado pelo servidor (ou {@code null} se não aplicável)
     * @return Ocorrência da família (Acidente ou Incidente)
     */
    O criarOcorrencia(
            I input,
            AreaRisco area,
            Colaborador colaborador,
            Colaborador relator,
            NumeroProtocoloCAT protocolo);

    /**
     * Cria a notificação interna para o gestor a partir da ocorrência registrada.
     *
     * @param ocorrencia Ocorrência pertencente a esta família
     * @return Notificação configurada para o tipo específico
     */
    NotificacaoOcorrencia criarNotificacao(Ocorrencia ocorrencia);

    /** Descrição da operação de cadastro na trilha de auditoria. */
    String descricaoAuditoria();

    /** Nome da entidade persistida na auditoria (ex.: "Acidente" ou "Incidente"). */
    String entidadeAuditoria();
}
