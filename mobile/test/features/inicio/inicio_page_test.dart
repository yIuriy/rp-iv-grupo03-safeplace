import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/main.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';

/// Início é a tela de exemplo do modo offline (#316, critério de aceite "modo avião").
void main() {
  late ApiFalsa api;
  late ArmazenamentoCacheEmMemoria cache;

  setUp(() {
    api = ApiFalsa(
      (_) => {
        'nome': 'Gestor Demo',
        'email': 'gestor.demo@safeplace.com',
        'cpf': '52998224725',
      },
    );
    cache = ArmazenamentoCacheEmMemoria();
  });

  Future<void> abrir(WidgetTester tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          armazenamentoSessaoProvider.overrideWithValue(
            ArmazenamentoSessaoEmMemoria(sessaoGestor),
          ),
          apiClientProvider.overrideWithValue(api),
          armazenamentoCacheProvider.overrideWithValue(cache),
        ],
        child: const SafePlaceApp(),
      ),
    );
    await tester.pumpAndSettle();
  }

  FilledButton atalho(WidgetTester tester, String rotulo) =>
      tester.widget<FilledButton>(
        find.ancestor(
          of: find.text(rotulo),
          matching: find.byType(FilledButton),
        ),
      );

  testWidgets('com conexão: dados da conta, CPF oculto e atalhos habilitados', (
    tester,
  ) async {
    await abrir(tester);

    expect(find.text('Seus dados'), findsOneWidget);
    expect(find.text('gestor.demo@safeplace.com'), findsOneWidget);
    expect(find.text('•••.982.247-••'), findsOneWidget);
    expect(find.text('Modo offline: exibindo dados locais'), findsNothing);
    expect(atalho(tester, 'Registrar ocorrência').onPressed, isNotNull);
  });

  testWidgets(
    'modo avião: mostra a cópia, a faixa de offline e desabilita os cadastros',
    (tester) async {
      await CacheOffline(
        cache,
        agora: () => DateTime(2026, 11, 17, 8, 5),
      ).guardar(sessaoGestor.email, 'usuarios/me', {
        'nome': 'Gestor Demo',
        'email': 'copia@safeplace.com',
      });
      api.responder = semConexao;

      await abrir(tester);

      expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
      expect(
        find.textContaining('Última sincronização em 17/11/2026 08:05.'),
        findsOneWidget,
      );
      expect(find.text('copia@safeplace.com'), findsOneWidget);
      expect(atalho(tester, 'Registrar ocorrência').onPressed, isNull);
      expect(atalho(tester, 'Novo empréstimo de EPI').onPressed, isNull);
      expect(
        find.text('Os cadastros voltam quando a conexão voltar.'),
        findsOneWidget,
      );
    },
  );

  testWidgets(
    'sem conexão e sem cópia: falha com tentar novamente, que recarrega',
    (tester) async {
      api.responder = semConexao;
      await abrir(tester);

      expect(
        find.text('Não foi possível carregar seus dados.'),
        findsOneWidget,
      );

      api.responder = (_) => {
        'nome': 'Gestor Demo',
        'email': 'gestor.demo@safeplace.com',
      };
      await tester.tap(find.text('Tentar novamente'));
      await tester.pumpAndSettle();

      expect(find.text('gestor.demo@safeplace.com'), findsOneWidget);
    },
  );

  testWidgets('atalho leva ao módulo de ocorrências', (tester) async {
    await abrir(tester);

    await tester.tap(find.text('Registrar ocorrência'));
    await tester.pumpAndSettle();

    expect(
      find.descendant(
        of: find.byType(AppBar),
        matching: find.text('Ocorrências'),
      ),
      findsOneWidget,
    );
  });
}
