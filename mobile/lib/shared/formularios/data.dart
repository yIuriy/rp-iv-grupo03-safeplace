import 'package:flutter/services.dart';

/// Datas no formulário (#317): na tela `dd/mm/aaaa`, como o web mostra, e na API `aaaa-mm-dd`, o
/// `LocalDate` do backend.

String _dois(int valor) => valor.toString().padLeft(2, '0');

/// `dd/mm/aaaa`.
String formatarData(DateTime data) =>
    '${_dois(data.day)}/${_dois(data.month)}/${data.year.toString().padLeft(4, '0')}';

/// `aaaa-mm-dd`, para enviar à API.
String dataIso(DateTime data) =>
    '${data.year.toString().padLeft(4, '0')}-${_dois(data.month)}-${_dois(data.day)}';

/// Lê o `dd/mm/aaaa` digitado. Data incompleta ou inexistente (31/02/2000) devolve `null`.
DateTime? lerData(String texto) {
  final partes = RegExp(r'^(\d{2})/(\d{2})/(\d{4})$').firstMatch(texto.trim());
  if (partes == null) return null;
  final dia = int.parse(partes[1]!);
  final mes = int.parse(partes[2]!);
  final ano = int.parse(partes[3]!);
  final data = DateTime(ano, mes, dia);
  // DateTime aceita 31/02 e "rola" para março; a data só vale se voltar igual.
  return data.year == ano && data.month == mes && data.day == dia ? data : null;
}

/// Hoje à meia-noite, para comparar só a data.
DateTime hoje([DateTime? agora]) {
  final momento = agora ?? DateTime.now();
  return DateTime(momento.year, momento.month, momento.day);
}

/// Máscara progressiva `dd/mm/aaaa`: serve para valores parciais, enquanto a pessoa digita.
String mascararData(String valor) {
  var digitos = valor.replaceAll(RegExp(r'\D'), '');
  if (digitos.length > 8) digitos = digitos.substring(0, 8);
  final saida = StringBuffer();
  for (var i = 0; i < digitos.length; i++) {
    if (i == 2 || i == 4) saida.write('/');
    saida.write(digitos[i]);
  }
  return saida.toString();
}

/// Aplica a máscara de data enquanto a pessoa digita, no `inputFormatters` do campo.
class MascaraData extends TextInputFormatter {
  const MascaraData();

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue anterior,
    TextEditingValue novo,
  ) {
    final mascarado = mascararData(novo.text);
    return TextEditingValue(
      text: mascarado,
      selection: TextSelection.collapsed(offset: mascarado.length),
    );
  }
}
