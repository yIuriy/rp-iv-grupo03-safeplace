import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import 'core/router.dart';

void main() {
  runApp(const ProviderScope(child: SafePlaceApp()));
}

class SafePlaceApp extends ConsumerWidget {
  const SafePlaceApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'SafePlace',
      debugShowCheckedModeBanner: false,
      // Cor `brand/primary` de `frontend/design-system/tokens.json`. O tema completo é da #316.
      theme: ThemeData(colorSchemeSeed: const Color(0xFF174B3F)),
      routerConfig: ref.watch(routerProvider),
    );
  }
}
