import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/shared/tema/app_theme.dart';

/// Cada família e peso que o tema usa precisa de um arquivo declarado no pubspec (#316); sem ele,
/// o Flutter cai na fonte padrão sem avisar.
void main() {
  final pubspec = File('pubspec.yaml').readAsStringSync();

  test('os pesos usados pelo tema estão declarados no pubspec e existem', () {
    final usados = <String>{};
    final estilos = AppTheme.textos;
    for (final estilo in [
      estilos.displayLarge,
      estilos.displayMedium,
      estilos.displaySmall,
      estilos.headlineLarge,
      estilos.headlineMedium,
      estilos.headlineSmall,
      estilos.titleLarge,
      estilos.titleMedium,
      estilos.titleSmall,
      estilos.bodyLarge,
      estilos.bodyMedium,
      estilos.bodySmall,
      estilos.labelLarge,
      estilos.labelMedium,
      estilos.labelSmall,
    ]) {
      usados.add('${estilo!.fontFamily} ${estilo.fontWeight!.value}');
    }

    for (final uso in usados) {
      final [familia, peso] = uso.split(' ');
      final bloco = RegExp(
        '- family: $familia'
        r'\s+fonts:((?:\s+- asset: \S+\s+weight: \d+)+)',
      ).firstMatch(pubspec);
      expect(bloco, isNotNull, reason: 'família $familia ausente no pubspec');
      final arquivo = RegExp(
        r'asset: (\S+)\s+weight: '
        '$peso',
      ).firstMatch(bloco!.group(1)!);
      expect(arquivo, isNotNull, reason: '$familia $peso ausente no pubspec');
      expect(
        File(arquivo!.group(1)!).existsSync(),
        isTrue,
        reason: '${arquivo.group(1)} não existe',
      );
    }
  });

  test('licenças OFL das fontes estão nos assets', () {
    expect(File('assets/fonts/OFL-Inter.txt').existsSync(), isTrue);
    expect(File('assets/fonts/OFL-Manrope.txt').existsSync(), isTrue);
    expect(pubspec, contains('assets/fonts/OFL-Inter.txt'));
  });
}
