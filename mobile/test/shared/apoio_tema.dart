import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/shared/tema/app_theme.dart';

/// Monta o widget dentro de um app com o tema do SafePlace, numa tela de celular.
Future<void> montarComTema(WidgetTester tester, Widget filho) async {
  await tester.pumpWidget(
    MaterialApp(
      theme: AppTheme.claro(),
      home: Scaffold(
        body: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: filho,
        ),
      ),
    ),
  );
}
