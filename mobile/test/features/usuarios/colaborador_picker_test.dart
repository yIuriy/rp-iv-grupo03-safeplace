import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/features/usuarios/data/usuario.dart';
import 'package:safeplace/features/usuarios/presentation/colaborador_picker.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';
import 'package:safeplace/shared/tema/app_theme.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import 'apoio_usuarios.dart';

void main() {
  late ApiUsuariosFalsa api;

  setUp(() => api = ApiUsuariosFalsa());

  /// Opções da lista que abre ao tocar no campo.
  Finder opcao(String nome) =>
      find.descendant(of: find.byType(ListTile), matching: find.text(nome));

  Future<void> abrirLista(WidgetTester tester, String rotulo) async {
    await tester.tap(find.bySemanticsLabel(rotulo));
    await tester.pumpAndSettle();
  }

  group('tela de exemplo (/usuarios/exemplo-seletor)', () {
    testWidgets('exige a escolha e mostra o colaborador escolhido', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios/exemplo-seletor',
      );

      await tocar(tester, find.text('Confirmar'));
      expect(find.text(ColaboradorPicker.mensagemObrigatorio), findsOneWidget);

      await abrirLista(tester, 'Colaborador envolvido');
      await tester.tap(opcao('João da Silva'));
      await tester.pumpAndSettle();
      await tocar(tester, find.text('Confirmar'));

      expect(find.text('Colaborador escolhido: João da Silva'), findsOneWidget);
      expect(
        find.text('O formulário enviaria colaboradorId = 10.'),
        findsOneWidget,
      );
      expect(find.text(ColaboradorPicker.mensagemObrigatorio), findsNothing);
    });
  });

  group('busca e opções', () {
    Usuario? escolhido;

    Future<void> montar(
      WidgetTester tester, {
      int? colaboradorId,
      bool somenteAtivos = true,
    }) async {
      tester.view.physicalSize = const Size(1080, 2400);
      tester.view.devicePixelRatio = 2.625;
      addTearDown(tester.view.reset);
      escolhido = null;
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            armazenamentoSessaoProvider.overrideWithValue(
              ArmazenamentoSessaoEmMemoria(sessaoSupervisor),
            ),
            apiClientProvider.overrideWithValue(api),
            armazenamentoCacheProvider.overrideWithValue(
              ArmazenamentoCacheEmMemoria(),
            ),
          ],
          child: MaterialApp(
            theme: AppTheme.claro(),
            home: Scaffold(
              body: Padding(
                padding: const EdgeInsets.all(16),
                child: ColaboradorPicker(
                  colaboradorId: colaboradorId,
                  somenteAtivos: somenteAtivos,
                  aoMudar: (c) => escolhido = c,
                ),
              ),
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();
    }

    testWidgets('lista só os ativos, com o CPF oculto', (tester) async {
      await montar(tester);
      await abrirLista(tester, 'Colaborador');

      expect(opcao('Ana Souza'), findsOneWidget);
      expect(opcao('João da Silva'), findsOneWidget);
      expect(opcao('Bruno Lima'), findsNothing);
      expect(find.textContaining('CPF •••.444.777-••'), findsOneWidget);
      expect(find.textContaining('111.444.777-35'), findsNothing);
      expect(
        api.chamadas('GET', '/usuarios/colaboradores').single.consulta,
        isNull,
      );
    });

    testWidgets('busca por nome sem acento e por CPF, com ou sem máscara', (
      tester,
    ) async {
      await montar(tester);
      await abrirLista(tester, 'Colaborador');
      final busca = find.widgetWithText(TextField, 'Buscar por nome ou CPF');

      await tester.enterText(busca, 'joao');
      await tester.pump();
      expect(opcao('João da Silva'), findsOneWidget);
      expect(opcao('Ana Souza'), findsNothing);

      await tester.enterText(busca, '12345678909');
      await tester.pump();
      expect(opcao('Ana Souza'), findsOneWidget);
      expect(opcao('João da Silva'), findsNothing);

      await tester.enterText(busca, '111.444');
      await tester.pump();
      expect(opcao('João da Silva'), findsOneWidget);

      await tester.tap(opcao('João da Silva'));
      await tester.pumpAndSettle();
      expect(escolhido?.id, 10);
      expect(find.text('João da Silva'), findsOneWidget);
    });

    testWidgets('mostra a escolha inicial pelo id, mesmo de um inativo', (
      tester,
    ) async {
      await montar(tester, colaboradorId: 12);

      expect(find.text('Bruno Lima'), findsOneWidget);
      await abrirLista(tester, 'Colaborador');
      expect(opcao('Bruno Lima'), findsOneWidget);
    });

    testWidgets('somenteAtivos: false oferece também os inativos', (
      tester,
    ) async {
      await montar(tester, somenteAtivos: false);
      await abrirLista(tester, 'Colaborador');

      expect(opcao('Bruno Lima'), findsOneWidget);
      expect(find.textContaining('Inativo'), findsOneWidget);
    });

    testWidgets('falha ao carregar mostra o erro e tenta de novo', (
      tester,
    ) async {
      api.responder = servidorFora;
      await montar(tester);

      expect(
        find.text('Não foi possível carregar os colaboradores.'),
        findsOneWidget,
      );
      api.responder = responderPadrao;
      await tester.tap(find.text('Tentar novamente'));
      await tester.pumpAndSettle();

      expect(
        find.text('Não foi possível carregar os colaboradores.'),
        findsNothing,
      );
      await abrirLista(tester, 'Colaborador');
      expect(opcao('Ana Souza'), findsOneWidget);
    });
  });
}
