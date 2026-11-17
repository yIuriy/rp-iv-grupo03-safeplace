import 'package:flutter/services.dart';

/// CPF no formulário (#316), igual ao web (`frontend/src/features/usuarios/formatos.ts` e
/// `validacaoPessoa.ts`): na tela `000.000.000-00`, no envio só os 11 dígitos.

/// Só os dígitos, no máximo 11.
String digitosDoCpf(String valor) {
  final digitos = valor.replaceAll(RegExp(r'\D'), '');
  return digitos.length > 11 ? digitos.substring(0, 11) : digitos;
}

/// Máscara progressiva `000.000.000-00`: serve para valores parciais, enquanto a pessoa digita.
String mascararCpf(String valor) {
  final d = digitosDoCpf(valor);
  final blocos = [
    for (var inicio = 0; inicio < 9 && inicio < d.length; inicio += 3)
      d.substring(inicio, inicio + 3 > d.length ? d.length : inicio + 3),
  ].join('.');
  return d.length > 9 ? '$blocos-${d.substring(9)}' : blocos;
}

/// Exibição em listas: `•••.456.789-••`, como no web. Fora de 11 dígitos, devolve o original.
String ocultarCpf(String cpf) {
  final d = digitosDoCpf(cpf);
  return d.length == 11
      ? '•••.${d.substring(3, 6)}.${d.substring(6, 9)}-••'
      : cpf;
}

/// Mensagem de erro do CPF, ou `null` se estiver válido. Mesmas mensagens do web: primeiro o
/// tamanho, depois os dígitos verificadores. Sequências repetidas (111.111.111-11) são recusadas,
/// como no `isValidCPF` usado pelo web e no `@CPF` do backend.
String? validarCpf(String? valor) {
  final d = digitosDoCpf(valor ?? '');
  if (d.length != 11) return 'Informe um CPF com 11 dígitos.';
  if (!cpfValido(d)) return 'CPF inválido: confira os dígitos.';
  return null;
}

bool cpfValido(String cpf) {
  final d = digitosDoCpf(cpf);
  if (d.length != 11 || RegExp(r'^(\d)\1{10}$').hasMatch(d)) return false;
  final numeros = d.split('').map(int.parse).toList();
  int verificador(int tamanho) {
    var soma = 0;
    for (var i = 0; i < tamanho; i++) {
      soma += numeros[i] * (tamanho + 1 - i);
    }
    final resto = (soma * 10) % 11;
    return resto == 10 ? 0 : resto;
  }

  return verificador(9) == numeros[9] && verificador(10) == numeros[10];
}

/// Aplica a máscara enquanto a pessoa digita, no `inputFormatters` do campo. O cursor vai para o
/// fim, que é onde se digita um CPF.
class MascaraCpf extends TextInputFormatter {
  const MascaraCpf();

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue anterior,
    TextEditingValue novo,
  ) {
    final mascarado = mascararCpf(novo.text);
    return TextEditingValue(
      text: mascarado,
      selection: TextSelection.collapsed(offset: mascarado.length),
    );
  }
}
