// Validação local do plano de ação (#321), portada de `frontend/src/features/ocorrencias/
// validacaoPlano.ts`, com as mesmas mensagens: nova ação com ao menos uma medida, prazo não anterior
// a hoje nem à data do fato, um responsável e antecedência do alerta quando ativo; prorrogação com
// novo prazo posterior ao atual e justificativa. As regras definitivas, como a ação preventiva em
// acidente grave, são do backend; aqui se evita um envio que já se sabe inválido.

import '../../../shared/formularios/data.dart';
import '../data/acao_plano.dart';

/// Maior entre hoje e o dia do fato: o prazo de uma ação não pode ser anterior a nenhum dos dois.
DateTime prazoMinimo({DateTime? dataDoFato, DateTime? agora}) {
  final hojeSemHora = hoje(agora);
  if (dataDoFato == null) return hojeSemHora;
  final diaDoFato = DateTime(dataDoFato.year, dataDoFato.month, dataDoFato.day);
  return diaDoFato.isAfter(hojeSemHora) ? diaDoFato : hojeSemHora;
}

String? validarMedidas(String? corretiva, String? preventiva) =>
    (corretiva ?? '').trim().isEmpty && (preventiva ?? '').trim().isEmpty
    ? 'Informe a medida corretiva, a preventiva ou ambas.'
    : null;

/// Prazo `dd/mm/aaaa` da nova ação.
String? validarPrazoDaAcao(String? valor, DateTime minimo) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe o prazo da ação.';
  final prazo = lerData(digitado);
  if (prazo == null) return 'Informe uma data válida, no formato dd/mm/aaaa.';
  return prazo.isBefore(minimo)
      ? 'O prazo não pode ser anterior a hoje nem à data do fato.'
      : null;
}

String? validarTipoDeResponsavel(TipoResponsavel? tipo) => tipo == null
    ? 'Indique se o responsável é um colaborador ou um setor.'
    : null;

String? validarResponsavel(TipoResponsavel? tipo, int? id) {
  if (tipo == null || id != null) return null;
  return tipo == TipoResponsavel.setor
      ? 'Selecione o setor responsável.'
      : 'Selecione o colaborador responsável.';
}

/// Antecedência do alerta: número inteiro, zero ou mais; só conta com o alerta ativo.
String? validarAntecedencia(bool ativo, String? valor) {
  if (!ativo) return null;
  final numero = int.tryParse((valor ?? '').trim());
  return numero == null || numero < 0
      ? 'Com o alerta ativo, informe a antecedência em dias (número inteiro, zero ou mais).'
      : null;
}

/// Novo prazo `dd/mm/aaaa` da prorrogação: depois do prazo atual e não antes de hoje.
String? validarNovoPrazo(
  String? valor,
  DateTime prazoAtual, {
  DateTime? agora,
}) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe o novo prazo.';
  final novo = lerData(digitado);
  if (novo == null) return 'Informe uma data válida, no formato dd/mm/aaaa.';
  final atual = DateTime(prazoAtual.year, prazoAtual.month, prazoAtual.day);
  if (!novo.isAfter(atual)) {
    return 'O novo prazo deve ser posterior ao prazo atual.';
  }
  return novo.isBefore(hoje(agora))
      ? 'O novo prazo não pode ser anterior a hoje.'
      : null;
}

String? validarJustificativa(String? valor) => (valor ?? '').trim().isEmpty
    ? 'A justificativa técnica é obrigatória para prorrogar.'
    : null;

/// Reatribuição: um responsável escolhido e diferente do atual (o backend também recusa).
String? validarNovoResponsavel(AcaoPlano acao, TipoResponsavel tipo, int? id) {
  if (id == null) return 'Selecione o novo responsável.';
  return acao.responsavelTipo == tipo && acao.responsavelId == id
      ? 'Escolha um responsável diferente do atual.'
      : null;
}
