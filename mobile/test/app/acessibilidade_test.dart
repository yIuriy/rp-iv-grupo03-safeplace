// Testes de acessibilidade (#335, RNF11) nas telas principais: login, início, EPIs, detalhe do EPI
// e empréstimos.
// Conferem as diretrizes do flutter_test (alvo de toque de 48 dp, rótulo em todo alvo tocável,
// contraste de texto) e que nenhuma tela estoura com o texto do sistema em 200%.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/main.dart';

import '../apoio.dart';
import '../features/emprestimos/emprestimos_page_test.dart'
    show emprestimoAtrasadoJson, emprestimoNoPrazoJson;
import '../features/epis/apoio_epis.dart';
import '../features/epis/epi_test.dart' show capaceteJson;
import '../features/epis/epis_page_test.dart' show luvaCriticaJson;
import '../features/usuarios/apoio_usuarios.dart' show abrirApp, gestorJson;

void main() {
  final api = ApiEpisFalsa(
    (r) => switch (r.caminho) {
      '/usuarios/me' => gestorJson,
      '/epis' => [capaceteJson, luvaCriticaJson],
      '/epis/1' => capaceteJson,
      '/emprestimos' => [emprestimoNoPrazoJson, emprestimoAtrasadoJson],
      _ => <Object?>[],
    },
  );

  void usarEscala(WidgetTester tester, double escala) {
    tester.platformDispatcher.textScaleFactorTestValue = escala;
    addTearDown(tester.platformDispatcher.clearTextScaleFactorTestValue);
  }

  Future<void> conferirDiretrizes(WidgetTester tester, double escala) async {
    // Garante que a tela foi montada com a escala de texto pedida.
    final contexto = tester.element(find.byType(Scaffold).first);
    expect(MediaQuery.textScalerOf(contexto).scale(10), 10 * escala);
    await expectLater(tester, meetsGuideline(androidTapTargetGuideline));
    await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
    await expectLater(tester, meetsGuideline(textContrastGuideline));
  }

  Future<void> abrirLogin(WidgetTester tester) async {
    tester.view.physicalSize = const Size(1080, 2400);
    tester.view.devicePixelRatio = 2.625;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          armazenamentoSessaoProvider.overrideWithValue(
            ArmazenamentoSessaoEmMemoria(),
          ),
        ],
        child: const SafePlaceApp(),
      ),
    );
    await tester.pumpAndSettle();
  }

  for (final escala in [1.0, 2.0]) {
    group('texto em ${(escala * 100).round()}%', () {
      testWidgets('login', (tester) async {
        final semantica = tester.ensureSemantics();
        usarEscala(tester, escala);
        await abrirLogin(tester);
        expect(find.text('Entrar no SafePlace'), findsOneWidget);
        await conferirDiretrizes(tester, escala);
        semantica.dispose();
      });

      for (final rota in ['/', '/epis', '/epis/1', '/emprestimos']) {
        testWidgets('tela $rota', (tester) async {
          final semantica = tester.ensureSemantics();
          usarEscala(tester, escala);
          await abrirApp(tester, sessao: sessaoGestor, api: api, rota: rota);
          await conferirDiretrizes(tester, escala);
          semantica.dispose();
        });
      }
    });
  }
}
