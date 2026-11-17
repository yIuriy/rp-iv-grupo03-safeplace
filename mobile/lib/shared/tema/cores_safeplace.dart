import 'package:material_ui/material_ui.dart';

import 'tokens.g.dart';

/// Cores do SafePlace que o [ColorScheme] do Material não tem lugar próprio (#316): os pares de
/// feedback (texto e fundo), o destaque lima, o fundo suave menta e as cores de texto e borda.
///
/// Uso: `context.cores.alerta`, `context.cores.perigoFundo`. Status nunca é só cor: sempre com
/// texto junto (foundations.md, "Accessibility and behavior").
@immutable
class CoresSafePlace extends ThemeExtension<CoresSafePlace> {
  const CoresSafePlace({
    required this.destaque,
    required this.suave,
    required this.pagina,
    required this.textoSecundario,
    required this.borda,
    required this.bordaControle,
    required this.fundoDesabilitado,
    required this.info,
    required this.infoFundo,
    required this.sucesso,
    required this.sucessoFundo,
    required this.alerta,
    required this.alertaFundo,
    required this.perigo,
    required this.perigoFundo,
    required this.perigoPressionado,
  });

  /// Valores do tokens.json; o tema claro usa estes.
  static const padrao = CoresSafePlace(
    destaque: TokensCores.brandAccent,
    suave: TokensCores.brandSoft,
    pagina: TokensCores.surfacePage,
    textoSecundario: TokensCores.textSecondary,
    borda: TokensCores.borderDefault,
    bordaControle: TokensCores.controlBorder,
    fundoDesabilitado: TokensCores.controlDisabledBg,
    info: TokensCores.feedbackInfo,
    infoFundo: TokensCores.feedbackInfoBg,
    sucesso: TokensCores.feedbackSuccess,
    sucessoFundo: TokensCores.feedbackSuccessBg,
    alerta: TokensCores.feedbackWarning,
    alertaFundo: TokensCores.feedbackWarningBg,
    perigo: TokensCores.feedbackDanger,
    perigoFundo: TokensCores.feedbackDangerBg,
    perigoPressionado: TokensCores.feedbackDangerHover,
  );

  final Color destaque;
  final Color suave;
  final Color pagina;
  final Color textoSecundario;
  final Color borda;
  final Color bordaControle;
  final Color fundoDesabilitado;
  final Color info;
  final Color infoFundo;
  final Color sucesso;
  final Color sucessoFundo;
  final Color alerta;
  final Color alertaFundo;
  final Color perigo;
  final Color perigoFundo;
  final Color perigoPressionado;

  @override
  CoresSafePlace copyWith({
    Color? destaque,
    Color? suave,
    Color? pagina,
    Color? textoSecundario,
    Color? borda,
    Color? bordaControle,
    Color? fundoDesabilitado,
    Color? info,
    Color? infoFundo,
    Color? sucesso,
    Color? sucessoFundo,
    Color? alerta,
    Color? alertaFundo,
    Color? perigo,
    Color? perigoFundo,
    Color? perigoPressionado,
  }) {
    return CoresSafePlace(
      destaque: destaque ?? this.destaque,
      suave: suave ?? this.suave,
      pagina: pagina ?? this.pagina,
      textoSecundario: textoSecundario ?? this.textoSecundario,
      borda: borda ?? this.borda,
      bordaControle: bordaControle ?? this.bordaControle,
      fundoDesabilitado: fundoDesabilitado ?? this.fundoDesabilitado,
      info: info ?? this.info,
      infoFundo: infoFundo ?? this.infoFundo,
      sucesso: sucesso ?? this.sucesso,
      sucessoFundo: sucessoFundo ?? this.sucessoFundo,
      alerta: alerta ?? this.alerta,
      alertaFundo: alertaFundo ?? this.alertaFundo,
      perigo: perigo ?? this.perigo,
      perigoFundo: perigoFundo ?? this.perigoFundo,
      perigoPressionado: perigoPressionado ?? this.perigoPressionado,
    );
  }

  @override
  CoresSafePlace lerp(CoresSafePlace? other, double t) {
    if (other == null) return this;
    Color misturar(Color a, Color b) => Color.lerp(a, b, t)!;
    return CoresSafePlace(
      destaque: misturar(destaque, other.destaque),
      suave: misturar(suave, other.suave),
      pagina: misturar(pagina, other.pagina),
      textoSecundario: misturar(textoSecundario, other.textoSecundario),
      borda: misturar(borda, other.borda),
      bordaControle: misturar(bordaControle, other.bordaControle),
      fundoDesabilitado: misturar(fundoDesabilitado, other.fundoDesabilitado),
      info: misturar(info, other.info),
      infoFundo: misturar(infoFundo, other.infoFundo),
      sucesso: misturar(sucesso, other.sucesso),
      sucessoFundo: misturar(sucessoFundo, other.sucessoFundo),
      alerta: misturar(alerta, other.alerta),
      alertaFundo: misturar(alertaFundo, other.alertaFundo),
      perigo: misturar(perigo, other.perigo),
      perigoFundo: misturar(perigoFundo, other.perigoFundo),
      perigoPressionado: misturar(perigoPressionado, other.perigoPressionado),
    );
  }
}

extension CoresDoContexto on BuildContext {
  /// Cores do SafePlace do tema atual; sem a extensão no tema, usa os valores padrão.
  CoresSafePlace get cores =>
      Theme.of(this).extension<CoresSafePlace>() ?? CoresSafePlace.padrao;
}
