package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Aviso interno sobre uma ação do plano, entregue ao Gestor de Segurança (UC07, regra 2 e cenário
 * alternativo II; decisão 2.6 da correspondência do plano de ação). O responsável pode ser um
 * Colaborador ou um Setor sem acesso ao sistema: o aviso o identifica para que o Gestor o comunique.
 */
public record NotificacaoAcao(
        Integer id,
        Integer idAcao,
        Integer idOcorrencia,
        MotivoAvisoAcao motivo,
        String responsavelTipo,
        Integer responsavelId,
        String responsavelNome,
        LocalDate prazo,
        LocalDate dataAviso,
        String mensagem,
        LocalDateTime criadaEm) {

    public static final String RESPONSAVEL_COLABORADOR = "COLABORADOR";
    public static final String RESPONSAVEL_SETOR = "SETOR";

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static NotificacaoAcao deAviso(AvisoAcao aviso) {
        if (aviso == null || aviso.idAcao() == null) {
            throw new IllegalArgumentException("O aviso exige uma ação já registrada.");
        }
        if (aviso.prazo() == null || aviso.dataAviso() == null) {
            throw new IllegalArgumentException("O aviso exige o prazo da ação e a data do aviso.");
        }
        Colaborador colaborador = aviso.responsavelColaborador();
        Setor setor = aviso.responsavelSetor();
        if ((colaborador == null) == (setor == null)) {
            throw new IllegalArgumentException("O aviso exige um único responsável: colaborador ou setor.");
        }

        String tipo = colaborador != null ? RESPONSAVEL_COLABORADOR : RESPONSAVEL_SETOR;
        Integer idResponsavel = colaborador != null ? colaborador.getId() : setor.getId();
        String nomeResponsavel = colaborador != null ? colaborador.getNome() : setor.getNome();
        String responsavel = colaborador != null
                ? nomeResponsavel + " (colaborador)"
                : "setor " + nomeResponsavel;

        return new NotificacaoAcao(null, aviso.idAcao(), aviso.idOcorrencia(), aviso.motivo(), tipo, idResponsavel,
                nomeResponsavel, aviso.prazo(), aviso.dataAviso(), mensagem(aviso, responsavel), null);
    }

    private static String mensagem(AvisoAcao aviso, String responsavel) {
        String acao = "Ação #" + aviso.idAcao()
                + (aviso.idOcorrencia() != null ? " da ocorrência #" + aviso.idOcorrencia() : "");
        String prazo = aviso.prazo().format(DATA);
        if (aviso.motivo() == MotivoAvisoAcao.REATRIBUICAO) {
            return acao + " reatribuída a " + responsavel + ". Prazo: " + prazo + ". Comunique o novo responsável.";
        }
        if (aviso.prazo().isBefore(aviso.dataAviso())) {
            return acao + " em atraso: o prazo venceu em " + prazo + " e a execução não foi confirmada. "
                    + "Responsável: " + responsavel + ".";
        }
        return acao + " com prazo em " + prazo + ". Responsável: " + responsavel + ".";
    }
}
