// Gera lib/shared/tema/tokens.g.dart a partir de frontend/design-system/tokens.json (#316).
//
// O web lê os mesmos valores em tokens.css; aqui eles viram constantes Dart. Rode depois de
// atualizar o tokens.json:
//
//   dart run tool/gerar_tokens.dart
//
// O teste test/shared/tema/tokens_test.dart falha se o arquivo gerado estiver desatualizado.
import 'dart:convert';
import 'dart:io';

const caminhoTokens = '../frontend/design-system/tokens.json';
const caminhoGerado = 'lib/shared/tema/tokens.g.dart';

void main() {
  final json = jsonDecode(
    File(caminhoTokens).readAsStringSync(),
  ) as Map<String, dynamic>;
  File(caminhoGerado).writeAsStringSync(gerarTokens(json));
  stdout.writeln('Gerado $caminhoGerado a partir de $caminhoTokens.');
}

/// Código Dart das constantes, em ordem estável (a do arquivo de tokens).
String gerarTokens(Map<String, dynamic> json) {
  final variaveis = (json['variables'] as List).cast<Map<String, dynamic>>();
  final tipografia = (json['typography'] as List).cast<Map<String, dynamic>>();
  final cores = variaveis.where((v) => v['type'] == 'color');
  final dimensoes = variaveis.where((v) => v['type'] == 'float');

  final saida = StringBuffer()
    ..writeln(
      '// GERADO por tool/gerar_tokens.dart a partir de frontend/design-system/tokens.json',
    )
    ..writeln(
      '// (SafePlace ${json['version']}, exportado em ${json['exportedOn']}). Não edite à mão.',
    )
    ..writeln('// ignore_for_file: constant_identifier_names')
    ..writeln()
    ..writeln("import 'package:material_ui/material_ui.dart';")
    ..writeln()
    ..writeln(
      '/// Cores do design system. Os nomes semânticos (brand, surface, text, feedback...) apontam',
    )
    ..writeln('/// para a paleta, como os aliases do tokens.json.')
    ..writeln('abstract final class TokensCores {');
  for (final cor in cores) {
    final hex = (cor['value'] as String).substring(1).toUpperCase();
    final alias = cor['alias'] == null ? '' : ' // ${cor['alias']}';
    saida.writeln(
      '  static const ${identificador(cor['name'] as String)} = Color(0xFF$hex);$alias',
    );
  }
  saida
    ..writeln('}')
    ..writeln()
    ..writeln(
      '/// Espaçamentos, raios, tamanhos e medidas de layout, em pixels lógicos.',
    )
    ..writeln('abstract final class TokensDimensoes {');
  for (final medida in dimensoes) {
    saida.writeln(
      '  static const double ${identificador(medida['name'] as String)} = ${_numero(medida['value'])};',
    );
  }
  saida
    ..writeln('}')
    ..writeln()
    ..writeln(
      '/// Estilos de texto do Figma. A cor fica com o tema, como nas classes sp-type-* do web.',
    )
    ..writeln('abstract final class TokensTipografia {');
  for (final estilo in tipografia) {
    final tamanho = estilo['size'] as num;
    final altura = (estilo['lineHeight'] as num) / tamanho;
    saida
      ..writeln(
        '  static const ${identificador(estilo['name'] as String)} = TextStyle(',
      )
      ..writeln("    fontFamily: '${estilo['family']}',")
      ..writeln('    fontWeight: FontWeight.w${estilo['weight']},')
      ..writeln('    fontSize: ${_numero(tamanho)},')
      ..writeln('    height: ${_numero(altura)},')
      ..writeln('    letterSpacing: ${_numero(estilo['letterSpacing'])},')
      ..writeln('  );');
  }
  saida.writeln('}');
  return saida.toString();
}

/// "palette/infoBg" → paletteInfoBg; "feedback/info-bg" → feedbackInfoBg; "space/4" → space4;
/// "SafePlace/Heading/XL" → headingXl.
String identificador(String nome) {
  final partes = nome
      .replaceFirst(RegExp(r'^SafePlace/'), '')
      .split(RegExp(r'[/\-]'))
      .where((p) => p.isNotEmpty)
      .toList();
  final buffer = StringBuffer(_minuscula(partes.first));
  for (final parte in partes.skip(1)) {
    buffer.write(
      parte[0].toUpperCase() +
          (parte == parte.toUpperCase()
              ? parte.substring(1).toLowerCase()
              : parte.substring(1)),
    );
  }
  return buffer.toString();
}

String _minuscula(String parte) => parte == parte.toUpperCase()
    ? parte.toLowerCase()
    : parte[0].toLowerCase() + parte.substring(1);

String _numero(Object? valor) {
  final numero = valor as num;
  if (numero == numero.roundToDouble()) return numero.toInt().toString();
  return double.parse(numero.toStringAsFixed(4)).toString();
}
