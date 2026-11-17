import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/shared/tema/app_theme.dart';
import 'package:safeplace/shared/tema/contraste.dart';
import 'package:safeplace/shared/tema/cores_safeplace.dart';
import 'package:safeplace/shared/tema/tokens.g.dart';

void main() {
  final tema = AppTheme.claro();
  final esquema = tema.colorScheme;

  group('Contraste WCAG 2.1 AA (RNF11)', () {
    test('fórmula: preto sobre branco é 21:1 e cor sobre ela mesma é 1:1', () {
      expect(
        contraste(const Color(0xFF000000), const Color(0xFFFFFFFF)),
        closeTo(21, 0.01),
      );
      expect(
        contraste(TokensCores.brandPrimary, TokensCores.brandPrimary),
        closeTo(1, 0.001),
      );
    });

    test('confere os valores medidos no foundations.md', () {
      expect(
        contraste(TokensCores.textPrimary, TokensCores.surfaceWhite),
        closeTo(14.89, 0.01),
      );
      expect(
        contraste(TokensCores.textInverse, TokensCores.brandPrimary),
        closeTo(9.93, 0.01),
      );
    });

    // Texto normal precisa de 4.5:1.
    final paresDeTexto = <String, (Color, Color)>{
      'onSurface / surface': (esquema.onSurface, esquema.surface),
      'onSurface / fundo da página': (
        esquema.onSurface,
        tema.scaffoldBackgroundColor,
      ),
      'onSurfaceVariant / surface': (esquema.onSurfaceVariant, esquema.surface),
      'onSurfaceVariant / fundo da página': (
        esquema.onSurfaceVariant,
        tema.scaffoldBackgroundColor,
      ),
      'onPrimary / primary': (esquema.onPrimary, esquema.primary),
      'onPrimaryContainer / primaryContainer': (
        esquema.onPrimaryContainer,
        esquema.primaryContainer,
      ),
      'onSecondary / secondary': (esquema.onSecondary, esquema.secondary),
      'onError / error': (esquema.onError, esquema.error),
      'onErrorContainer / errorContainer': (
        esquema.onErrorContainer,
        esquema.errorContainer,
      ),
      'onTertiaryContainer / tertiaryContainer': (
        esquema.onTertiaryContainer,
        esquema.tertiaryContainer,
      ),
      'primary sobre surface (botões Secondary e Quiet)': (
        esquema.primary,
        esquema.surface,
      ),
      'primary sobre primaryContainer (botão pressionado)': (
        esquema.primary,
        esquema.primaryContainer,
      ),
      'info / infoFundo': (
        CoresSafePlace.padrao.info,
        CoresSafePlace.padrao.infoFundo,
      ),
      'sucesso / sucessoFundo': (
        CoresSafePlace.padrao.sucesso,
        CoresSafePlace.padrao.sucessoFundo,
      ),
      'alerta / alertaFundo': (
        CoresSafePlace.padrao.alerta,
        CoresSafePlace.padrao.alertaFundo,
      ),
      'perigo / perigoFundo': (
        CoresSafePlace.padrao.perigo,
        CoresSafePlace.padrao.perigoFundo,
      ),
      'texto claro / perigo (botão Danger)': (
        TokensCores.textInverse,
        CoresSafePlace.padrao.perigo,
      ),
      'texto claro / snackbar': (
        TokensCores.textInverse,
        tema.snackBarTheme.backgroundColor!,
      ),
    };
    for (final MapEntry(key: nome, value: (frente, fundo))
        in paresDeTexto.entries) {
      test(
        '$nome tem pelo menos 4.5:1',
        () => expect(contraste(frente, fundo), greaterThanOrEqualTo(4.5)),
      );
    }

    test('borda dos campos tem pelo menos 3:1 (componente de interface)', () {
      expect(
        contraste(esquema.outline, esquema.surface),
        greaterThanOrEqualTo(3),
      );
    });
  });

  group('Tema a partir dos tokens', () {
    test('cores principais', () {
      expect(esquema.primary, TokensCores.brandPrimary);
      expect(esquema.error, TokensCores.feedbackDanger);
      expect(tema.scaffoldBackgroundColor, TokensCores.surfacePage);
      expect(tema.extension<CoresSafePlace>(), CoresSafePlace.padrao);
    });

    test('títulos em Manrope e corpo em Inter', () {
      expect(tema.textTheme.headlineMedium?.fontFamily, 'Manrope');
      expect(tema.textTheme.headlineMedium?.fontSize, 28);
      expect(tema.textTheme.bodyLarge?.fontFamily, 'Inter');
      expect(tema.textTheme.bodyLarge?.fontSize, 16);
      expect(tema.textTheme.labelLarge?.fontWeight, FontWeight.w600);
      expect(tema.textTheme.bodyLarge?.color, TokensCores.textPrimary);
    });

    test('botões com 44 de altura e raio 8', () {
      final estilo = tema.filledButtonTheme.style!;
      expect(
        estilo.minimumSize?.resolve({})?.height,
        TokensDimensoes.sizeControl,
      );
      final forma = estilo.shape?.resolve({}) as RoundedRectangleBorder;
      expect(
        forma.borderRadius,
        BorderRadius.circular(TokensDimensoes.radius8),
      );
      expect(estilo.backgroundColor?.resolve({}), TokensCores.brandPrimary);
      expect(
        estilo.backgroundColor?.resolve({WidgetState.disabled}),
        TokensCores.controlDisabledBg,
      );
      expect(
        AppTheme.botaoPerigo.backgroundColor?.resolve({}),
        TokensCores.feedbackDanger,
      );
    });

    test('campos com rótulo visível, foco e erro de 2', () {
      final campos = tema.inputDecorationTheme;
      expect(campos.floatingLabelBehavior, FloatingLabelBehavior.always);
      expect(
        (campos.focusedBorder as OutlineInputBorder).borderSide.width,
        TokensDimensoes.strokeFocus,
      );
      expect(
        (campos.focusedBorder as OutlineInputBorder).borderSide.color,
        TokensCores.brandPrimary,
      );
      expect(
        (campos.errorBorder as OutlineInputBorder).borderSide.color,
        TokensCores.feedbackDanger,
      );
    });
  });

  testWidgets('context.cores devolve as cores do SafePlace do tema', (
    tester,
  ) async {
    late CoresSafePlace lidas;
    await tester.pumpWidget(
      MaterialApp(
        theme: tema,
        home: Builder(
          builder: (context) {
            lidas = context.cores;
            return const SizedBox();
          },
        ),
      ),
    );

    expect(lidas.alerta, TokensCores.feedbackWarning);
    expect(lidas.destaque, TokensCores.brandAccent);
  });
}
