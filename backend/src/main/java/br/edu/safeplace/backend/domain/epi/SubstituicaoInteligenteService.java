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
 * multiplicadores de agressividade do ambiente (NivelPerigo) e teto intransponível de CA.</p>
 */
public class SubstituicaoInteligenteService {

    public static final long DIAS_ANTECEDENCIA_PADRAO = 30L;
    public static final int DIAS_AMOSTRAGEM_PADRAO = 90;

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
     * @param diasAmostragem Janela temporal para apuração da taxa de uso (se nula/<=0, assume 90).
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

        // 3. Validação de Certificado de Aprovação (CA)
        LocalDate validadeCa = epi.getDataValidadeCa();
        if (validadeCa == null) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui Certificado de Aprovação (CA) com validade cadastrada.",
                            epi.getNome(), epiId));
        }

        // 4. Validação do histórico de empréstimos
        if (emprestimos == null || emprestimos.isEmpty()) {
            throw new DadosInsuficientesParaProjecaoException(epiId,
                    String.format("EPI '%s' (ID %d) não possui registros de empréstimo para apuração da frequência de uso.",
                            epi.getNome(), epiId));
        }

        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }

        int janela = (diasAmostragem != null && diasAmostragem > 0) ? diasAmostragem : DIAS_AMOSTRAGEM_PADRAO;

        // 5. Apuração dos dias de uso efetivo no histórico
        long totalDiasUsados = 0;
        for (Emprestimo emp : emprestimos) {
            LocalDate inicio = emp.getDataRetirada().toLocalDate();
            LocalDate fim;
            if (emp.getDataDevolucaoReal() != null) {
                fim = emp.getDataDevolucaoReal().toLocalDate();
            } else {
                fim = dataReferencia;
            }

            long dias = ChronoUnit.DAYS.between(inicio, fim);
            if (dias <= 0) {
                dias = 1; // Mesmo dia conta como 1 dia de jornada
            }
            totalDiasUsados += dias;
        }

        // Taxa diária de utilização U (0.0 < U <= 1.0)
        double taxaUso = (double) totalDiasUsados / (double) janela;
        if (taxaUso > 1.0) {
            taxaUso = 1.0;
        }
        if (taxaUso < 0.05) {
            taxaUso = 0.05; // Margem mínima de utilização operacional
        }

        // 6. Aplicação do multiplicador da matriz de risco ambiental
        double fatorAmbiente = obterMultiplicadorAmbiente(periculosidadeSetor);
        double taxaDesgasteDiario = taxaUso * fatorAmbiente;

        // Desgaste nominal consumido até o momento
        double desgasteConsumido = totalDiasUsados * fatorAmbiente;
        double vidaUtilRestante = epi.getVidaUtilDias() - desgasteConsumido;
        if (vidaUtilRestante < 0) {
            vidaUtilRestante = 0;
        }

        long diasAteDesgasteFisico = (long) Math.ceil(vidaUtilRestante / taxaDesgasteDiario);
        LocalDate dataFisicaEstimada = dataReferencia.plusDays(diasAteDesgasteFisico);

        // 7. Aplicação da regra prioritária do CA (UC10 Regra 1)
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

    /**
     * Emite alertas preventivos para um EPI baseado na projeção e níveis de estoque (RF21, UC10).
     */
    public List<AlertaSubstituicaoEpi> emitirAlertas(Epi epi, ProjecaoSubstituicao projecao,
                                                     LocalDate dataReferencia, Long diasAntecedencia) {
        List<AlertaSubstituicaoEpi> alertas = new ArrayList<>();
        if (epi == null) {
            return alertas;
        }
        Integer epiId = epi.getId() != null ? epi.getId() : epi.getCodigoEPI();
        String nomeEpi = epi.getNome();
        if (dataReferencia == null) {
            dataReferencia = LocalDate.now();
        }
        long antecedencia = diasAntecedencia != null && diasAntecedencia > 0 ? diasAntecedencia : DIAS_ANTECEDENCIA_PADRAO;

        // 1. Alerta de estoque crítico
        if (epi.isEstoqueCritico()) {
            alertas.add(AlertaSubstituicaoEpi.estoqueCritico(epiId, nomeEpi, epi.getQuantidade(), epi.getEstoqueMinimo()));
        }

        // 2. Alerta de data estimada de descarte próxima
        if (projecao != null) {
            long diasAteDescarte = ChronoUnit.DAYS.between(dataReferencia, projecao.getDataEstimadaDescarte());
            if (diasAteDescarte <= antecedencia) {
                alertas.add(AlertaSubstituicaoEpi.descarteProximo(epiId, nomeEpi, projecao.getDataEstimadaDescarte(), diasAteDescarte));
            }
        }

        // 3. Alerta de vencimento de CA próximo
        if (epi.getDataValidadeCa() != null) {
            long diasAteCa = ChronoUnit.DAYS.between(dataReferencia, epi.getDataValidadeCa());
            if (diasAteCa >= 0 && diasAteCa <= antecedencia) {
                alertas.add(AlertaSubstituicaoEpi.caProximoVencimento(epiId, nomeEpi, epi.getDataValidadeCa(), diasAteCa));
            }
        }

        return alertas;
    }
}
