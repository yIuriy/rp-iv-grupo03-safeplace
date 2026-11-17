import 'dart:math' as math;

import 'package:material_ui/material_ui.dart';

/// Contraste entre duas cores pela fórmula da WCAG 2.1 (de 1:1 a 21:1). RNF11 pede nível AA:
/// pelo menos 4.5 para texto normal e 3 para texto grande e bordas de controles.
double contraste(Color a, Color b) {
  final claro = math.max(_luminancia(a), _luminancia(b));
  final escuro = math.min(_luminancia(a), _luminancia(b));
  return (claro + 0.05) / (escuro + 0.05);
}

/// Luminância relativa (WCAG 2.1, "relative luminance"); ignora a transparência.
double _luminancia(Color cor) =>
    0.2126 * _linear(cor.r) + 0.7152 * _linear(cor.g) + 0.0722 * _linear(cor.b);

double _linear(double canal) => canal <= 0.04045
    ? canal / 12.92
    : math.pow((canal + 0.055) / 1.055, 2.4).toDouble();
