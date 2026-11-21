// Validação local do cadastro de EPI, com as regras do `CriarEpiRequest` do backend: nome, CA,
// quantidade, estoque mínimo, validade do CA e classificação obrigatórios. Quantidades são inteiros
// de 0 para cima, a vida útil é opcional e começa em 1 dia, e as datas de lote são opcionais. O web
// confia no `required` do HTML e deixa a classificação opcional, mas o backend a exige.

import '../../../shared/formularios/data.dart';
import '../data/epi.dart';

String? validarNomeEpi(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Informe o nome do EPI.' : null;

String? validarNumeroCa(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Informe o número do CA.' : null;

/// Quantidade inicial e estoque mínimo: inteiro, 0 ou mais.
String? validarQuantidadeInicial(String? valor) => _inteiroAPartirDe(valor, 0)
    ? null
    : 'Informe um número inteiro, 0 ou mais.';

String? validarEstoqueMinimo(String? valor) => _inteiroAPartirDe(valor, 0)
    ? null
    : 'Informe um número inteiro, 0 ou mais.';

/// Opcional. Quando preenchida, inteiro de 1 dia para cima.
String? validarVidaUtil(String? valor) {
  if ((valor ?? '').trim().isEmpty) return null;
  return _inteiroAPartirDe(valor, 1)
      ? null
      : 'Informe a vida útil em dias, um número inteiro maior que zero.';
}

/// `dd/mm/aaaa` obrigatório.
String? validarValidadeCa(String? valor) {
  if ((valor ?? '').trim().isEmpty) return 'Informe a validade do CA.';
  return validarDataOpcional(valor);
}

/// Datas de fabricação e validade do lote: vazias passam, preenchidas precisam existir.
String? validarDataOpcional(String? valor) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty || lerData(digitado) != null) return null;
  return 'Informe uma data válida, no formato dd/mm/aaaa.';
}

String? validarClassificacao(ClassificacaoEpi? valor) =>
    valor == null ? 'Escolha a classificação do EPI.' : null;

bool _inteiroAPartirDe(String? valor, int minimo) {
  final numero = int.tryParse((valor ?? '').trim());
  return numero != null && numero >= minimo;
}
