// Validação local do relato e da consulta de ocorrências, portada de `validacaoRelato.ts` e
// `criteriosConsulta.ts` do web (#318). Tipo, data e hora do fato (nunca no futuro), área,
// colaborador e descrição são obrigatórios. O acidente exige causa e gravidade, e o incidente exige
// situação de risco e potencial de dano. A regra definitiva é do backend, e aqui se evita um envio
// que já se sabe inválido. Área e colaborador são validados pelos próprios seletores.

import '../../../shared/formularios/data.dart';
import '../../../shared/formularios/hora.dart';
import '../../areas_risco/data/area_risco.dart';
import '../data/ocorrencia.dart';

String? validarTipoOcorrencia(TipoOcorrencia? tipo) =>
    tipo == null ? 'Selecione se foi um acidente ou um incidente.' : null;

/// `dd/mm/aaaa`, existente no calendário e não posterior a hoje.
String? validarDataDoFato(String? valor, {DateTime? agora}) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe a data do fato.';
  final data = lerData(digitado);
  if (data == null) return 'Informe uma data válida, no formato dd/mm/aaaa.';
  return data.isAfter(hoje(agora))
      ? 'A data do fato não pode estar no futuro.'
      : null;
}

/// `hh:mm`. Num fato de hoje, a hora também não pode passar de agora.
String? validarHoraDoFato(
  String? valor, {
  required String data,
  DateTime? agora,
}) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe a hora do fato.';
  if (!horaValida(digitado)) {
    return 'Informe uma hora válida, no formato hh:mm.';
  }
  final dia = lerData(data);
  final momento = agora ?? DateTime.now();
  if (dia == null || dia != hoje(momento)) return null;
  final [hora, minuto] = digitado.split(':').map(int.parse).toList();
  final fato = DateTime(dia.year, dia.month, dia.day, hora, minuto);
  return fato.isAfter(momento)
      ? 'A hora do fato não pode estar no futuro.'
      : null;
}

String? validarDescricaoDoFato(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Descreva o que aconteceu.' : null;

String? validarCausaRaiz(CausaRaiz? causa) =>
    causa == null ? 'Indique a causa do acidente.' : null;

String? validarGravidade(NivelPerigo? gravidade) =>
    gravidade == null ? 'Indique a gravidade do acidente.' : null;

String? validarSituacaoRisco(String? valor) => (valor ?? '').trim().isEmpty
    ? 'Descreva a situação de risco observada.'
    : null;

String? validarPotencialDano(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Informe o potencial de dano.' : null;

/// `dd/mm/aaaa` e `hh:mm`, já validados, viram `aaaa-mm-ddThh:mm:00` (o `LocalDateTime` da API) só
/// reordenando o texto.
String montarDataHoraDoFato(String data, String hora) {
  final [dia, mes, ano] = data.trim().split('/');
  return '$ano-$mes-${dia}T${hora.trim()}:00';
}

/// Data opcional de um filtro: vazia passa, e digitada precisa existir no calendário.
String? validarDataDoFiltro(String? valor) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return null;
  return lerData(digitado) == null
      ? 'Informe uma data válida, no formato dd/mm/aaaa.'
      : null;
}

/// Período invertido é recusado antes do envio, como o backend também recusa.
String? validarPeriodo(DateTime? inicio, DateTime? fim) =>
    inicio != null && fim != null && inicio.isAfter(fim)
    ? 'A data final deve ser igual ou posterior à data inicial.'
    : null;
