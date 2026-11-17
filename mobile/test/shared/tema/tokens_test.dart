import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/shared/tema/tokens.g.dart';

import '../../../tool/gerar_tokens.dart';

/// tokens.g.dart é gerado do tokens.json do web; os dois não podem se desencontrar (#316).
void main() {
  final json = jsonDecode(
    File(caminhoTokens).readAsStringSync(),
  ) as Map<String, dynamic>;

  String semEspacos(String codigo) => codigo.replaceAll(RegExp(r'\s+'), '');

  test('tokens.g.dart está em dia com o tokens.json (rode: dart run tool/gerar_tokens.dart)', () {
    final gerado = File(caminhoGerado).readAsStringSync();

    // A comparação ignora espaços porque o dart format reorganiza as linhas do arquivo gerado.
    expect(semEspacos(gerado), semEspacos(gerarTokens(json)));
  });

  test('todas as variáveis e estilos de texto viram constantes', () {
    final codigo = gerarTokens(json);

    expect(
      'static const'.allMatches(codigo).length,
      (json['variables'] as List).length + (json['typography'] as List).length,
    );
  });

  test('nomes do Figma viram identificadores Dart', () {
    expect(identificador('palette/infoBg'), 'paletteInfoBg');
    expect(identificador('feedback/info-bg'), 'feedbackInfoBg');
    expect(identificador('space/4'), 'space4');
    expect(identificador('SafePlace/Heading/XL'), 'headingXl');
    expect(identificador('SafePlace/Body/MD'), 'bodyMd');
  });

  test('valores conferem com o design system', () {
    expect(TokensCores.brandPrimary, const Color(0xFF174B3F));
    expect(TokensCores.brandPrimary, TokensCores.palettePine);
    expect(TokensCores.feedbackDangerBg, const Color(0xFFFBECEF));
    expect(TokensDimensoes.sizeControl, 44);
    expect(TokensDimensoes.radius8, 8);
    expect(TokensTipografia.headingMd.fontFamily, 'Manrope');
    expect(TokensTipografia.headingMd.fontSize, 28);
    expect(TokensTipografia.headingMd.height! * 28, closeTo(36, 0.01));
    expect(TokensTipografia.labelMd.fontWeight, FontWeight.w600);
  });
}
