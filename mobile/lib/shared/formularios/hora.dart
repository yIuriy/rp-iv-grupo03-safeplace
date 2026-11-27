// Hora digitada nos formulários (`hh:mm`), usada pela manutenção de EPI e pelo relato de
// ocorrência (#318).

import 'package:flutter/services.dart';

final _hora = RegExp(r'^([01]\d|2[0-3]):([0-5]\d)$');

/// `hh:mm` de 00:00 a 23:59.
bool horaValida(String texto) => _hora.hasMatch(texto.trim());

/// `hh:mm` enquanto se digita: só números, com os dois-pontos depois da hora.
class MascaraHora extends TextInputFormatter {
  const MascaraHora();

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue antigo,
    TextEditingValue novo,
  ) {
    var digitos = novo.text.replaceAll(RegExp(r'\D'), '');
    if (digitos.length > 4) digitos = digitos.substring(0, 4);
    final texto = digitos.length > 2
        ? '${digitos.substring(0, 2)}:${digitos.substring(2)}'
        : digitos;
    return TextEditingValue(
      text: texto,
      selection: TextSelection.collapsed(offset: texto.length),
    );
  }
}
