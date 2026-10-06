package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.epi.exception.DadosInsuficientesParaProjecaoException;

/**
 * Serviço de domínio puro responsável pelo cálculo do desgaste e projeção de substituição de EPIs
 * (RF21, UC10/DS10.1, US21).
 *
 * <p>Implementa o contrato matemático formal com taxa de utilização real baseada em empréstimos,
 * multiplicadores de agressividade do ambiente (NivelPerigo) e teto intransponível de CA.
 * Dados ausentes ou inconsistentes geram pendência, nunca estimativa fictícia (DS10, 2.3).</p>
 */
public class SubstituicaoInteligenteService {

    /** DS10, 2.4: antecedência fixa dos alertas preventivos no MVP. */
    public static final long DIAS_ANTECEDENCIA_PADRAO = 30L;

    /**
     * Retorna o fator multiplicador de risco ambiental do setor conforme NivelPerigo.
     */
    public double obterMultiplicadorAmbiente(NivelPerigo nivel) {
        if (nivel == null) {
            throw new IllegalArgumentException("Nível de perigo do setor é obrigatório.");
        }
        return switch (nivel) {
            case BAIXO -> 1.0;
            case MEDIO -> 1.25;
            case ALTO -> 1.5;
            case CRITICO -> 2.0;
        };
    }

    /**
     * Executa o cálculo da projeção de descarte de um EPI (DS10.1).
     *
     * @param epi O equipamento analisado.
     * @param periculosidadeSetor O grau de perigo do setor onde o EPI opera.
     * @param emprestimos Histórico de empréstimos do equipamento.
     * @param dataReferencia Data base da projeção (se nula, assume hoje).
     * @param diasAmostragem Período de amostragem informado pelo Gestor, em dias. Se nulo ou não
     *                       positivo, o período vai da primeira retirada registrada até a data de
     *                       referência, sem valor padrão arbitrado.
     * @return Entidade de domínio ProjecaoSubstituicao com as datas e justificativa geradas.
     * @throws DadosInsuficientesParaProjecaoException Quando faltam dados obrigatórios para o cálculo.
     */
    public ProjecaoSubstituicao calcularProjecao(Epi epi, NivelPerigo periculosidadeSetor,
                                                 List<Emprestimo> emprestimos, LocalDate dataReferencia,
                                                 Integer diasAmostragem) {
        if (epi == null) {
            throw new IllegalArgumentException("EPI é obrigatório para a projeção.");
        }
        Integer epiId = epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }

        // 1. Validação de durabilidade nominal (UC10 Exceção I)
        if (epi.getVidaUtilDias() == null || epi.getVidaUtilDias() <= 0) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui durabilidade nominal (vida útil em dias) cadastrada.",
                            epi.getNome(), epiId));
        }

        // 2. Validação de condições ambientais do setor
        if (periculosidadeSetor == null) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui setor com nível de periculosidade definido.",
                            epi.getNome(), epiId));
        }

        // 3. Validação de Certificado de Aprovação (CA): ausente ou vencido não gera estimativa (DS10, 2.3)
        LocalDate validadeCa = epi.getDataValidadeCa();
        if (validadeCa == null) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui Certificado de Aprovação (CA) com validade cadastrada.",
                            epi.getNome(), epiId));
        }
        if (validadeCa.isBefore(dataReferencia)) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui CA válido: o CA venceu em %s.",
                            epi.getNome(), epiId, validadeCa));
        }

        // 4. Validação do histórico de empréstimos até a data de referência
        LocalDate referencia = dataReferencia;
        List<Emprestimo> historico = emprestimos == null ? List.of() : emprestimos.stream()
                .filter(emp -> emp.getDataRetirada() != null
                        && !emp.getDataRetirada().toLocalDate().isAfter(referencia))
                .toList();
        if (historico.isEmpty()) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui registros de empréstimo para apuração da frequência de uso.",
                            epi.getNome(), epiId));
        }

        // 5. Período de amostragem: informado pelo Gestor ou derivado do próprio histórico
        LocalDate fimAmostragem = dataReferencia;
        LocalDate inicioAmostragem = (diasAmostragem != null && diasAmostragem > 0)
                ? dataReferencia.minusDays(diasAmostragem)
                : historico.stream().map(emp -> emp.getDataRetirada().toLocalDate())
                        .min(LocalDate::compareTo).orElseThrow();
        long diasDoPeriodo = Math.max(1, ChronoUnit.DAYS.between(inicioAmostragem, fimAmostragem));

        // 6. Apuração dos dias de uso: no período (taxa U) e em todo o histórico (desgaste acumulado)
        long diasUsoNoPeriodo = 0;
        long totalDiasUsados = 0;
        for (Emprestimo emp : historico) {
            LocalDate inicio = emp.getDataRetirada().toLocalDate();
            LocalDate fim = emp.getDataDevolucaoReal() != null
                    ? emp.getDataDevolucaoReal().toLocalDate()
                    : dataReferencia;
            if (fim.isAfter(dataReferencia)) {
                fim = dataReferencia;
            }
            totalDiasUsados += diasDeUso(inicio, fim);

            LocalDate inicioNoPeriodo = inicio.isBefore(inicioAmostragem) ? inicioAmostragem : inicio;
            if (!fim.isBefore(inicioNoPeriodo)) {
                diasUsoNoPeriodo += diasDeUso(inicioNoPeriodo, fim);
            }
        }

        if (diasUsoNoPeriodo == 0) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui uso registrado no período de amostragem de %s a %s.",
                            epi.getNome(), epiId, inicioAmostragem, fimAmostragem));
        }

        // Taxa diária de utilização U (0.0 < U <= 1.0, DS10 2.1)
        double taxaUso = Math.min(1.0, (double) diasUsoNoPeriodo / (double) diasDoPeriodo);

        // 7. Aplicação do multiplicador da matriz de risco ambiental
        double fatorAmbiente = obterMultiplicadorAmbiente(periculosidadeSetor);
        double taxaDesgasteDiario = taxaUso * fatorAmbiente;

        // Desgaste nominal consumido até o momento
        double desgasteConsumido = totalDiasUsados * fatorAmbiente;
        double vidaUtilRestante = Math.max(0, epi.getVidaUtilDias() - desgasteConsumido);

        long diasAteDesgasteFisico = (long) Math.ceil(vidaUtilRestante / taxaDesgasteDiario);
        LocalDate dataFisicaEstimada = dataReferencia.plusDays(diasAteDesgasteFisico);

        // 8. Aplicação da regra prioritária do CA (UC10 Regra 1)
        LocalDate dataEstimadaDescarte;
        String justificativa;

        if (dataFisicaEstimada.isAfter(validadeCa)) {
            dataEstimadaDescarte = validadeCa;
            justificativa = String.format(
                    "Substituição antecipada pelo vencimento legal do CA em %s, antes do término da vida útil física projetada (%s).",
                    validadeCa, dataFisicaEstimada);
        } else {
            dataEstimadaDescarte = dataFisicaEstimada;
            justificativa = String.format(
                    "Substituição por desgaste acelerado em ambiente %s (fator %.2f), taxa de uso diário de %.1f%%.",
                    periculosidadeSetor.name(), fatorAmbiente, taxaUso * 100.0);
        }

        return ProjecaoSubstituicao.nova(epiId, dataReferencia, dataEstimadaDescarte, justificativa);
    }

    /** Dias corridos de um intervalo de uso; retirada e devolução no mesmo dia contam como 1 dia. */
    private static long diasDeUso(LocalDate inicio, LocalDate fim) {
        return Math.max(1, ChronoUnit.DAYS.between(inicio, fim));
    }

    /**
     * Emite alertas preventivos para um EPI baseado na projeção e níveis de estoque (RF21, UC10),
     * com a antecedência fixa de 30 dias do DS10, 2.4. A projeção pode ser nula quando o cálculo
     * gerou pendência; estoque crítico e CA continuam sendo avaliados.
     */
    public List<AlertaSubstituicaoEpi> emitirAlertas(Epi epi, ProjecaoSubstituicao projecao,
                                                     LocalDate dataReferencia) {
        List<AlertaSubstituicaoEpi> alertas = new ArrayList<>();
        if (epi == null) {
            return alertas;
        }
        Integer epiId = epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
        String nomeEpi = epi.getNome();
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }

        // 1. Alerta de estoque crítico
        if (epi.isEstoqueCritico()) {
            alertas.add(AlertaSubstituicaoEpi.estoqueCritico(epiId, nomeEpi, epi.getQuantidade(), epi.getEstoqueMinimo()));
        }

        // 2. Alerta de data estimada de descarte próxima
        if (projecao != null) {
            long diasAteDescarte = ChronoUnit.DAYS.between(dataReferencia, projecao.getDataEstimadaDescarte());
            if (diasAteDescarte <= DIAS_ANTECEDENCIA_PADRAO) {
                alertas.add(AlertaSubstituicaoEpi.descarteProximo(epiId, nomeEpi, projecao.getDataEstimadaDescarte(), diasAteDescarte));
            }
        }

        // 3. Alerta de CA: validadeCA <= T_ref + 30 dias (DS10, 2.4), inclusive CA já vencido
        if (epi.getDataValidadeCa() != null) {
            long diasAteCa = ChronoUnit.DAYS.between(dataReferencia, epi.getDataValidadeCa());
            if (diasAteCa <= DIAS_ANTECEDENCIA_PADRAO) {
                alertas.add(AlertaSubstituicaoEpi.caProximoVencimento(epiId, nomeEpi, epi.getDataValidadeCa(), diasAteCa));
            }
        }

        return alertas;
    }
}
