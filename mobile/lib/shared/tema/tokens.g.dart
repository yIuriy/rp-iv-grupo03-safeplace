// GERADO por tool/gerar_tokens.dart a partir de frontend/design-system/tokens.json
// (SafePlace 1.0.0, exportado em 2026-09-06). Não edite à mão.
// ignore_for_file: constant_identifier_names

import 'package:material_ui/material_ui.dart';

/// Cores do design system. Os nomes semânticos (brand, surface, text, feedback...) apontam
/// para a paleta, como os aliases do tokens.json.
abstract final class TokensCores {
  static const palettePine = Color(0xFF174B3F);
  static const paletteDeep = Color(0xFF102F29);
  static const paletteMint = Color(0xFFDDF2E7);
  static const paletteLime = Color(0xFFD7EB83);
  static const palettePaper = Color(0xFFF6F7F2);
  static const paletteWhite = Color(0xFFFFFFFF);
  static const paletteInk = Color(0xFF172B26);
  static const paletteMuted = Color(0xFF52645C);
  static const paletteBorder = Color(0xFFCBD7D0);
  static const paletteInfo = Color(0xFF225C86);
  static const paletteInfoBg = Color(0xFFEAF3FA);
  static const paletteSuccess = Color(0xFF246644);
  static const paletteSuccessBg = Color(0xFFE8F3EB);
  static const paletteWarning = Color(0xFF805600);
  static const paletteWarningBg = Color(0xFFFFF2CC);
  static const paletteDanger = Color(0xFFA4303B);
  static const paletteDangerBg = Color(0xFFFBECEF);
  static const brandPrimary = Color(0xFF174B3F); // palette/pine
  static const brandDeep = Color(0xFF102F29); // palette/deep
  static const brandSoft = Color(0xFFDDF2E7); // palette/mint
  static const brandAccent = Color(0xFFD7EB83); // palette/lime
  static const surfacePage = Color(0xFFF6F7F2); // palette/paper
  static const surfaceWhite = Color(0xFFFFFFFF); // palette/white
  static const textPrimary = Color(0xFF172B26); // palette/ink
  static const textSecondary = Color(0xFF52645C); // palette/muted
  static const textInverse = Color(0xFFFFFFFF); // palette/white
  static const borderDefault = Color(0xFFCBD7D0); // palette/border
  static const feedbackInfo = Color(0xFF225C86); // palette/info
  static const feedbackInfoBg = Color(0xFFEAF3FA); // palette/infoBg
  static const feedbackSuccess = Color(0xFF246644); // palette/success
  static const feedbackSuccessBg = Color(0xFFE8F3EB); // palette/successBg
  static const feedbackWarning = Color(0xFF805600); // palette/warning
  static const feedbackWarningBg = Color(0xFFFFF2CC); // palette/warningBg
  static const feedbackDanger = Color(0xFFA4303B); // palette/danger
  static const feedbackDangerBg = Color(0xFFFBECEF); // palette/dangerBg
  static const controlBorder = Color(0xFF52645C); // palette/muted
  static const controlDisabledBg = Color(0xFFCBD7D0); // palette/border
  static const paletteDangerHover = Color(0xFF7E2230);
  static const feedbackDangerHover = Color(0xFF7E2230); // palette/dangerHover
}

/// Espaçamentos, raios, tamanhos e medidas de layout, em pixels lógicos.
abstract final class TokensDimensoes {
  static const double space4 = 4;
  static const double space8 = 8;
  static const double space12 = 12;
  static const double space16 = 16;
  static const double space24 = 24;
  static const double space32 = 32;
  static const double space48 = 48;
  static const double space64 = 64;
  static const double space80 = 80;
  static const double radius8 = 8;
  static const double radius16 = 16;
  static const double radius24 = 24;
  static const double sizeControl = 44;
  static const double sizeCompact = 36;
  static const double sizeIcon = 20;
  static const double strokeDefault = 1;
  static const double strokeFocus = 2;
  static const double radius4 = 4;
  static const double space0 = 0;
  static const double layoutHeader = 64;
  static const double layoutSidebar = 216;
  static const double layoutContentPadding = 32;
  static const double layoutMobilePadding = 16;
  static const double layoutGutter = 24;
  static const double layoutTabletBreakpoint = 768;
  static const double layoutDesktopBreakpoint = 1200;
  static const double layoutMobileMin = 360;
  static const double focusOffset = 4;
}

/// Estilos de texto do Figma. A cor fica com o tema, como nas classes sp-type-* do web.
abstract final class TokensTipografia {
  static const display = TextStyle(
    fontFamily: 'Manrope',
    fontWeight: FontWeight.w700,
    fontSize: 80,
    height: 1.1,
    letterSpacing: 0,
  );
  static const headingXl = TextStyle(
    fontFamily: 'Manrope',
    fontWeight: FontWeight.w700,
    fontSize: 56,
    height: 1.1429,
    letterSpacing: 0,
  );
  static const headingLg = TextStyle(
    fontFamily: 'Manrope',
    fontWeight: FontWeight.w600,
    fontSize: 40,
    height: 1.2,
    letterSpacing: 0,
  );
  static const headingMd = TextStyle(
    fontFamily: 'Manrope',
    fontWeight: FontWeight.w600,
    fontSize: 28,
    height: 1.2857,
    letterSpacing: 0,
  );
  static const headingSm = TextStyle(
    fontFamily: 'Manrope',
    fontWeight: FontWeight.w600,
    fontSize: 20,
    height: 1.4,
    letterSpacing: 0,
  );
  static const bodyLg = TextStyle(
    fontFamily: 'Inter',
    fontWeight: FontWeight.w400,
    fontSize: 20,
    height: 1.5,
    letterSpacing: 0,
  );
  static const bodyMd = TextStyle(
    fontFamily: 'Inter',
    fontWeight: FontWeight.w400,
    fontSize: 16,
    height: 1.5,
    letterSpacing: 0,
  );
  static const bodySm = TextStyle(
    fontFamily: 'Inter',
    fontWeight: FontWeight.w400,
    fontSize: 14,
    height: 1.4286,
    letterSpacing: 0,
  );
  static const labelMd = TextStyle(
    fontFamily: 'Inter',
    fontWeight: FontWeight.w600,
    fontSize: 14,
    height: 1.4286,
    letterSpacing: 0,
  );
  static const labelSm = TextStyle(
    fontFamily: 'Inter',
    fontWeight: FontWeight.w500,
    fontSize: 12,
    height: 1.5,
    letterSpacing: 0,
  );
}
