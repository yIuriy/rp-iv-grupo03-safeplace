import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// As fontes do app (Inter e Manrope) usam a SIL Open Font License, que pede o texto da licença
/// junto do programa. Ele aparece na tela de licenças do Flutter (`showLicensePage`).
void registrarLicencasDasFontes() {
  LicenseRegistry.addLicense(() async* {
    for (final (fonte, arquivo) in const [
      ('Inter', 'assets/fonts/OFL-Inter.txt'),
      ('Manrope', 'assets/fonts/OFL-Manrope.txt'),
    ]) {
      yield LicenseEntryWithLineBreaks([
        fonte,
      ], await rootBundle.loadString(arquivo));
    }
  });
}
