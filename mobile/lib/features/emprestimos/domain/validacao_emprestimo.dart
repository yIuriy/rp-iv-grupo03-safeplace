// Validação local dos formulários de entrega e devolução de EPI (UC12, passos 3 a 5; US11).
// Portado de `frontend/src/features/emprestimos/validacaoEntrega.ts`.
// As regras definitivas (saldo, CA, duplicidade, capacitação) são do backend;
// aqui se evita chamadas com dados incompletos ou datas no passado.

import '../../../shared/formularios/data.dart';
import '../data/emprestimo.dart';

/// Valida seleção de colaborador ativo para entrega.
String? validarColaborador(int? colaboradorId) =>
    colaboradorId == null ? 'Selecione o colaborador que recebe o EPI.' : null;

/// Valida seleção de EPI disponível para entrega.
String? validarEpi(int? epiId) =>
    epiId == null ? 'Selecione o EPI que será entregue.' : null;

/// Valida a data prevista de devolução: obrigatória e não anterior a hoje.
String? validarDataDevolucaoPrevista(DateTime? data, [DateTime? dataHoje]) {
  if (data == null) return 'Informe a data prevista de devolução.';
  final hojeReferencia = hoje(dataHoje);
  if (data.isBefore(hojeReferencia)) {
    return 'A devolução prevista não pode ser anterior a hoje, dia da entrega.';
  }
  return null;
}

/// Valida a data digitada em formato texto (`dd/mm/aaaa`).
String? validarDataDevolucaoTexto(String? texto, [DateTime? dataHoje]) {
  final digitado = (texto ?? '').trim();
  if (digitado.isEmpty) return 'Informe a data prevista de devolução.';
  final data = lerData(digitado);
  if (data == null) {
    return 'Informe uma data válida, no formato dd/mm/aaaa.';
  }
  return validarDataDevolucaoPrevista(data, dataHoje);
}

/// Justificativa condicional obrigatória ao confirmar duplicidade de posse (US11, critério 6).
String? validarJustificativa(String? valor) =>
    (valor ?? '').trim().isEmpty
        ? 'Informe a justificativa da substituição.'
        : null;

/// Condição obrigatória ao registrar devolução (UC12, alternativo I).
String? validarEstadoDevolucao(EstadoDevolucao? estado) =>
    estado == null ? 'Informe a condição do item devolvido.' : null;

/// Agrupamento de erros de validação local do formulário de entrega.
class ErrosEntrega {
  const ErrosEntrega({
    this.colaborador,
    this.epi,
    this.dataDevolucaoPrevista,
  });

  final String? colaborador;
  final String? epi;
  final String? dataDevolucaoPrevista;

  bool get temErros =>
      colaborador != null || epi != null || dataDevolucaoPrevista != null;
}

/// Executa a validação combinada de todos os campos de entrega.
ErrosEntrega validarEntrega({
  required int? colaboradorId,
  required int? epiId,
  required DateTime? dataDevolucaoPrevista,
  DateTime? dataHoje,
}) {
  return ErrosEntrega(
    colaborador: validarColaborador(colaboradorId),
    epi: validarEpi(epiId),
    dataDevolucaoPrevista: validarDataDevolucaoPrevista(
      dataDevolucaoPrevista,
      dataHoje,
    ),
  );
}
