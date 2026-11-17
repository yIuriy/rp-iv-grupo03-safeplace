import 'package:material_ui/material_ui.dart';

import 'cores_safeplace.dart';
import 'tokens.g.dart';

/// Tema do app a partir do design system do SafePlace (#316), o mesmo do web
/// (frontend/design-system). Os valores vêm de [TokensCores], [TokensDimensoes] e
/// [TokensTipografia], gerados do tokens.json; nada de cor ou medida solta aqui.
///
/// Botões, na mesma lógica do web (component-contracts.md):
/// - Primary: `FilledButton`, confirma a operação.
/// - Secondary: `OutlinedButton`, complementa.
/// - Quiet: `TextButton`, cancela ou volta.
/// - Danger: `FilledButton(style: AppTheme.botaoPerigo)`, nomeia uma operação destrutiva.
abstract final class AppTheme {
  static ThemeData claro() {
    const esquema = ColorScheme(
      brightness: Brightness.light,
      primary: TokensCores.brandPrimary,
      onPrimary: TokensCores.textInverse,
      primaryContainer: TokensCores.brandSoft,
      onPrimaryContainer: TokensCores.brandDeep,
      secondary: TokensCores.brandAccent,
      onSecondary: TokensCores.brandDeep,
      secondaryContainer: TokensCores.brandSoft,
      onSecondaryContainer: TokensCores.brandDeep,
      tertiary: TokensCores.feedbackInfo,
      onTertiary: TokensCores.textInverse,
      tertiaryContainer: TokensCores.feedbackInfoBg,
      onTertiaryContainer: TokensCores.feedbackInfo,
      error: TokensCores.feedbackDanger,
      onError: TokensCores.textInverse,
      errorContainer: TokensCores.feedbackDangerBg,
      onErrorContainer: TokensCores.feedbackDanger,
      surface: TokensCores.surfaceWhite,
      onSurface: TokensCores.textPrimary,
      onSurfaceVariant: TokensCores.textSecondary,
      surfaceContainerLowest: TokensCores.surfaceWhite,
      surfaceContainerLow: TokensCores.surfacePage,
      surfaceContainer: TokensCores.surfacePage,
      surfaceContainerHigh: TokensCores.surfacePage,
      surfaceContainerHighest: TokensCores.brandSoft,
      outline: TokensCores.controlBorder,
      outlineVariant: TokensCores.borderDefault,
      inverseSurface: TokensCores.brandDeep,
      onInverseSurface: TokensCores.textInverse,
      inversePrimary: TokensCores.brandSoft,
      surfaceTint: Colors.transparent,
    );

    final texto = textos.apply(
      bodyColor: TokensCores.textPrimary,
      displayColor: TokensCores.textPrimary,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: esquema,
      scaffoldBackgroundColor: TokensCores.surfacePage,
      fontFamily: 'Inter',
      textTheme: texto,
      extensions: const [CoresSafePlace.padrao],
      iconTheme: const IconThemeData(
        size: TokensDimensoes.sizeIcon,
        color: TokensCores.textPrimary,
      ),
      dividerTheme: const DividerThemeData(
        color: TokensCores.borderDefault,
        thickness: TokensDimensoes.strokeDefault,
        space: TokensDimensoes.space16,
      ),
      filledButtonTheme: FilledButtonThemeData(style: _botaoPrimario),
      outlinedButtonTheme: OutlinedButtonThemeData(style: _botaoSecundario),
      textButtonTheme: TextButtonThemeData(style: _botaoDiscreto),
      iconButtonTheme: IconButtonThemeData(
        style: IconButton.styleFrom(
          minimumSize: const Size.square(TokensDimensoes.sizeControl),
          iconSize: TokensDimensoes.sizeIcon,
          foregroundColor: TokensCores.textPrimary,
        ),
      ),
      inputDecorationTheme: _campos,
      appBarTheme: AppBarTheme(
        backgroundColor: TokensCores.surfaceWhite,
        foregroundColor: TokensCores.textPrimary,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        toolbarHeight: TokensDimensoes.layoutHeader,
        centerTitle: false,
        titleTextStyle: texto.titleLarge,
        shape: const Border(
          bottom: BorderSide(color: TokensCores.borderDefault),
        ),
      ),
      cardTheme: const CardThemeData(
        color: TokensCores.surfaceWhite,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(
            Radius.circular(TokensDimensoes.radius8),
          ),
          side: BorderSide(color: TokensCores.borderDefault),
        ),
      ),
      listTileTheme: ListTileThemeData(
        titleTextStyle: texto.bodyLarge,
        subtitleTextStyle: texto.bodyMedium?.copyWith(
          color: TokensCores.textSecondary,
        ),
        iconColor: TokensCores.textSecondary,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: TokensDimensoes.layoutMobilePadding,
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: TokensCores.surfaceWhite,
        surfaceTintColor: Colors.transparent,
        indicatorColor: TokensCores.brandSoft,
        height: TokensDimensoes.layoutHeader,
        labelTextStyle: WidgetStatePropertyAll(texto.labelMedium),
        iconTheme: WidgetStateProperty.resolveWith(
          (estados) => IconThemeData(
            size: TokensDimensoes.sizeIcon,
            color: estados.contains(WidgetState.selected)
                ? TokensCores.brandPrimary
                : TokensCores.textSecondary,
          ),
        ),
      ),
      navigationDrawerTheme: NavigationDrawerThemeData(
        backgroundColor: TokensCores.surfaceWhite,
        surfaceTintColor: Colors.transparent,
        indicatorColor: TokensCores.brandSoft,
        labelTextStyle: WidgetStatePropertyAll(texto.labelLarge),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: TokensCores.surfaceWhite,
        surfaceTintColor: Colors.transparent,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.all(
            Radius.circular(TokensDimensoes.radius16),
          ),
        ),
        titleTextStyle: texto.titleLarge,
        contentTextStyle: texto.bodyLarge,
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        backgroundColor: TokensCores.brandDeep,
        contentTextStyle: texto.bodyMedium?.copyWith(
          color: TokensCores.textInverse,
        ),
        actionTextColor: TokensCores.brandAccent,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.all(
            Radius.circular(TokensDimensoes.radius8),
          ),
        ),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: TokensCores.surfacePage,
        selectedColor: TokensCores.brandSoft,
        side: const BorderSide(color: TokensCores.borderDefault),
        labelStyle: texto.labelMedium,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.all(
            Radius.circular(TokensDimensoes.radius8),
          ),
        ),
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: TokensCores.brandPrimary,
      ),
      checkboxTheme: CheckboxThemeData(
        fillColor: _selecionado(TokensCores.brandPrimary),
      ),
      radioTheme: RadioThemeData(
        fillColor: _selecionado(TokensCores.brandPrimary),
      ),
      switchTheme: SwitchThemeData(
        trackColor: _selecionado(
          TokensCores.brandPrimary,
          senao: TokensCores.borderDefault,
        ),
      ),
    );
  }

  /// Mapa dos estilos do Figma para os papéis de texto do Material. Body/LG (20) fica só em
  /// [TokensTipografia.bodyLg]: no celular, o corpo padrão é o Body/MD (16).
  static final textos = TextTheme(
    displayLarge: TokensTipografia.display,
    displayMedium: TokensTipografia.headingXl,
    displaySmall: TokensTipografia.headingLg,
    headlineLarge: TokensTipografia.headingLg,
    headlineMedium: TokensTipografia.headingMd,
    headlineSmall: TokensTipografia.headingSm,
    titleLarge: TokensTipografia.headingSm,
    titleMedium: TokensTipografia.bodyMd.copyWith(fontWeight: FontWeight.w600),
    titleSmall: TokensTipografia.labelMd,
    bodyLarge: TokensTipografia.bodyMd,
    bodyMedium: TokensTipografia.bodySm,
    bodySmall: TokensTipografia.labelSm.copyWith(fontWeight: FontWeight.w400),
    labelLarge: TokensTipografia.labelMd,
    labelMedium: TokensTipografia.labelSm,
    labelSmall: TokensTipografia.labelSm,
  );

  /// Botão Danger do web: vermelho com texto claro, mais escuro ao pressionar.
  static final botaoPerigo = _botaoBase.copyWith(
    backgroundColor: WidgetStateProperty.resolveWith(
      (estados) => estados.contains(WidgetState.disabled)
          ? TokensCores.controlDisabledBg
          : estados.contains(WidgetState.pressed)
          ? TokensCores.feedbackDangerHover
          : TokensCores.feedbackDanger,
    ),
    foregroundColor: _habilitado(TokensCores.textInverse),
  );

  // Botões: 44 de altura, 16 de respiro lateral, Label/MD (Inter 600 14/20), raio 8.
  static final _botaoBase = ButtonStyle(
    minimumSize: const WidgetStatePropertyAll(
      Size(64, TokensDimensoes.sizeControl),
    ),
    padding: const WidgetStatePropertyAll(
      EdgeInsets.symmetric(horizontal: TokensDimensoes.space16),
    ),
    textStyle: const WidgetStatePropertyAll(TokensTipografia.labelMd),
    iconSize: const WidgetStatePropertyAll(TokensDimensoes.sizeIcon),
    shape: const WidgetStatePropertyAll(
      RoundedRectangleBorder(
        borderRadius: BorderRadius.all(
          Radius.circular(TokensDimensoes.radius8),
        ),
      ),
    ),
    elevation: const WidgetStatePropertyAll(0),
  );

  static final _botaoPrimario = _botaoBase.copyWith(
    backgroundColor: WidgetStateProperty.resolveWith(
      (estados) => estados.contains(WidgetState.disabled)
          ? TokensCores.controlDisabledBg
          : estados.contains(WidgetState.pressed)
          ? TokensCores.brandDeep
          : TokensCores.brandPrimary,
    ),
    foregroundColor: _habilitado(TokensCores.textInverse),
  );

  static final _botaoSecundario = _botaoBase.copyWith(
    backgroundColor: WidgetStateProperty.resolveWith(
      (estados) => estados.contains(WidgetState.pressed)
          ? TokensCores.brandSoft
          : TokensCores.surfaceWhite,
    ),
    foregroundColor: _habilitado(TokensCores.brandPrimary),
    side: WidgetStateProperty.resolveWith(
      (estados) => BorderSide(
        color: estados.contains(WidgetState.disabled)
            ? TokensCores.borderDefault
            : TokensCores.controlBorder,
      ),
    ),
  );

  static final _botaoDiscreto = _botaoBase.copyWith(
    backgroundColor: WidgetStateProperty.resolveWith(
      (estados) => estados.contains(WidgetState.pressed)
          ? TokensCores.brandSoft
          : Colors.transparent,
    ),
    foregroundColor: _habilitado(TokensCores.brandPrimary),
  );

  // Campos: rótulo sempre visível, borda control/border, foco e erro com 2 de espessura (o web
  // usa borda 1 + sombra interna 1), fundo cinza quando desabilitado.
  static const _raio = BorderRadius.all(
    Radius.circular(TokensDimensoes.radius8),
  );

  static final _campos = InputDecorationTheme(
    filled: true,
    fillColor: TokensCores.surfaceWhite,
    floatingLabelBehavior: FloatingLabelBehavior.always,
    contentPadding: const EdgeInsets.symmetric(
      horizontal: TokensDimensoes.space12,
      vertical: TokensDimensoes.space12,
    ),
    labelStyle: TokensTipografia.labelMd.copyWith(
      color: TokensCores.textPrimary,
    ),
    floatingLabelStyle: WidgetStateTextStyle.resolveWith(
      (estados) => TokensTipografia.labelMd.copyWith(
        color: estados.contains(WidgetState.error)
            ? TokensCores.feedbackDanger
            : TokensCores.textPrimary,
      ),
    ),
    hintStyle: TokensTipografia.bodyMd.copyWith(
      color: TokensCores.textSecondary,
    ),
    helperStyle: TokensTipografia.bodySm.copyWith(
      color: TokensCores.textSecondary,
    ),
    errorStyle: TokensTipografia.bodySm.copyWith(
      color: TokensCores.feedbackDanger,
    ),
    errorMaxLines: 3,
    helperMaxLines: 3,
    border: const OutlineInputBorder(
      borderRadius: _raio,
      borderSide: BorderSide(color: TokensCores.controlBorder),
    ),
    enabledBorder: const OutlineInputBorder(
      borderRadius: _raio,
      borderSide: BorderSide(color: TokensCores.controlBorder),
    ),
    focusedBorder: const OutlineInputBorder(
      borderRadius: _raio,
      borderSide: BorderSide(
        color: TokensCores.brandPrimary,
        width: TokensDimensoes.strokeFocus,
      ),
    ),
    errorBorder: const OutlineInputBorder(
      borderRadius: _raio,
      borderSide: BorderSide(color: TokensCores.feedbackDanger),
    ),
    focusedErrorBorder: const OutlineInputBorder(
      borderRadius: _raio,
      borderSide: BorderSide(
        color: TokensCores.feedbackDanger,
        width: TokensDimensoes.strokeFocus,
      ),
    ),
    disabledBorder: const OutlineInputBorder(
      borderRadius: _raio,
      borderSide: BorderSide(color: TokensCores.borderDefault),
    ),
  );

  /// Cor normal habilitado; texto secundário quando desabilitado.
  static WidgetStateProperty<Color> _habilitado(Color cor) =>
      WidgetStateProperty.resolveWith(
        (estados) => estados.contains(WidgetState.disabled)
            ? TokensCores.textSecondary
            : cor,
      );

  static WidgetStateProperty<Color> _selecionado(
    Color cor, {
    Color senao = Colors.transparent,
  }) => WidgetStateProperty.resolveWith(
    (estados) => estados.contains(WidgetState.selected) ? cor : senao,
  );
}
