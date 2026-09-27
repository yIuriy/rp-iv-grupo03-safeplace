package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Conteúdo de um aviso sobre uma ação do plano (UC07). O responsável pode ser um Colaborador ou um
 * Setor sem conta de acesso; por isso o aviso identifica o encarregado, mas é entregue pelo canal
 * definido na decisão 2.6 da correspondência do plano de ação (aviso interno aos usuários com acesso),
 * nunca por login do Colaborador.
 */
public record AvisoAcao(
        Integer idAcao,
        Integer idOcorrencia,
        MotivoAvisoAcao motivo,
        Colaborador responsavelColaborador,
        Setor responsavelSetor,
        LocalDate prazo,
        LocalDate dataAviso) {
}
