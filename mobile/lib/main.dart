import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import 'app/dependencias.dart';
import 'app/router.dart';
import 'shared/tema/app_theme.dart';
import 'shared/tema/fontes.dart';

void main() {
  registrarLicencasDasFontes();
  runApp(
    ProviderScope(overrides: sobreposicoesDoApp, child: const SafePlaceApp()),
  );
}

class SafePlaceApp extends ConsumerWidget {
  const SafePlaceApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'SafePlace',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.claro(),
      // pt-BR nos textos do Material (#335): o TalkBack lê "Voltar" e "Abrir menu de navegação"
      // em vez dos rótulos em inglês.
      locale: const Locale('pt', 'BR'),
      supportedLocales: const [Locale('pt', 'BR')],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
      routerConfig: ref.watch(routerProvider),
    );
  }
}
