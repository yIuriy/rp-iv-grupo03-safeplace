// Validação local do formulário de manutenção (`RegistrarManutencaoRequest`): data, hora, descrição e
// resultado obrigatórios. O tipo (preventiva ou corretiva) é opcional, como no web. A data enviada é
// montada pelo texto digitado, sem passar por `DateTime` (mesmo cuidado da #250).

import '../../../shared/formularios/data.dart';
import '../data/manutencao.dart';

final _hora = RegExp(r'^([01]\d|2[0-3]):([0-5]\d)$');

/// `dd/mm/aaaa`, obrigatória e existente no calendário.
String? validarDataManutencao(String? valor) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe a data da manutenção.';
  return lerData(digitado) == null
      ? 'Informe uma data válida, no formato dd/mm/aaaa.'
      : null;
}

/// `hh:mm`, de 00:00 a 23:59.
String? validarHoraManutencao(String? valor) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe a hora da manutenção.';
  return _hora.hasMatch(digitado)
      ? null
      : 'Informe uma hora válida, no formato hh:mm.';
}

String? validarDescricaoManutencao(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Informe a descrição da manutenção.' : null;

String? validarResultado(ResultadoManutencao? valor) =>
    valor == null ? 'Escolha o resultado da manutenção.' : null;

/// `dd/mm/aaaa` e `hh:mm`, já validados, viram `aaaa-mm-ddThh:mm` só reordenando o texto.
String montarDataHoraIso(String data, String hora) {
  final [dia, mes, ano] = data.trim().split('/');
  return '$ano-$mes-${dia}T${hora.trim()}';
}
