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
      routerConfig: ref.watch(routerProvider),
    );
  }
}
