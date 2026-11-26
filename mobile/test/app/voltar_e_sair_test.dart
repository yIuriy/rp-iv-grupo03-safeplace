// Testes de voltar e sair (#335): seta de voltar nas sub-rotas, voltar do Android levando ao
// início, confirmação de "Sair" e aviso de descarte nos formulários em tela cheia.

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/app/router.dart';

import '../apoio.dart';
import '../features/epis/apoio_epis.dart';
import '../features/epis/epi_test.dart' show capaceteJson;
import '../features/usuarios/apoio_usuarios.dart'
    show abrirApp, campo, gestorJson, tocar;

void main() {
  late ApiEpisFalsa api;

  setUp(() {
    api = ApiEpisFalsa(
      (r) => switch (r.caminho) {
        '/usuarios/me' => gestorJson,
        '/epis' => [capaceteJson],
        '/epis/1' => capaceteJson,
        _ => <Object?>[],
      },
    );
  });

  String caminho(router) => router.routerDelegate.currentConfiguration.uri.path;

  /// Simula o botão ou gesto de voltar do Android.
  Future<void> voltarDoSistema(WidgetTester tester) async {
    await tester.binding.handlePopRoute();
    await tester.pumpAndSettle();
  }

  testWidgets(
    'detalhe do EPI mostra a seta de voltar na barra, que leva à lista',
    (tester) async {
      final router = await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/epis/1',
      );

      expect(find.byTooltip('Abrir menu de navegação'), findsNothing);
      await tocar(tester, find.byTooltip('Voltar'));

      expect(caminho(router), '/epis');
      expect(find.byTooltip('Abrir menu de navegação'), findsOneWidget);
    },
  );

  testWidgets(
    'voltar do Android num módulo leva ao início, e no início sai do app',
    (tester) async {
      var saiuDoApp = false;
      tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(
        SystemChannels.platform,
        (chamada) async {
          if (chamada.method == 'SystemNavigator.pop') saiuDoApp = true;
          return null;
        },
      );
      addTearDown(
        () => tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(
          SystemChannels.platform,
          null,
        ),
      );
      final router = await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/epis',
      );

      await voltarDoSistema(tester);
      expect(caminho(router), Rotas.inicio);
      expect(saiuDoApp, isFalse);

      await voltarDoSistema(tester);
      expect(saiuDoApp, isTrue);
    },
  );

  testWidgets('"Sair" pede confirmação, e "Cancelar" mantém a sessão', (
    tester,
  ) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/');

    await tocar(tester, find.widgetWithText(TextButton, 'Sair'));
    expect(find.text('Sair do SafePlace?'), findsOneWidget);
    await tocar(tester, find.text('Cancelar'));
    expect(find.text('Olá, ${sessaoGestor.nome}'), findsOneWidget);

    await tocar(tester, find.widgetWithText(TextButton, 'Sair'));
    await tocar(tester, find.widgetWithText(FilledButton, 'Sair'));
    expect(find.text('Entrar no SafePlace'), findsOneWidget);
  });

  group('formulário em tela cheia', () {
    Future<void> abrirCadastro(WidgetTester tester) async {
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis');
      await tocar(tester, find.widgetWithText(FilledButton, 'Cadastrar EPI'));
    }

    testWidgets('sem nada digitado, fecha direto', (tester) async {
      await abrirCadastro(tester);

      await tocar(tester, find.byTooltip('Fechar'));

      expect(find.text('Descartar o preenchimento?'), findsNothing);
      expect(campo('Nome do EPI'), findsNothing);
    });

    testWidgets(
      'com dados digitados, o X e o voltar do Android pedem confirmação',
      (tester) async {
        await abrirCadastro(tester);
        await tester.enterText(campo('Nome do EPI'), 'Protetor auricular');
        await tester.pump();

        await tocar(tester, find.byTooltip('Fechar'));
        expect(find.text('Descartar o preenchimento?'), findsOneWidget);
        await tocar(tester, find.text('Continuar editando'));
        expect(find.text('Protetor auricular'), findsOneWidget);

        await voltarDoSistema(tester);
        expect(find.text('Descartar o preenchimento?'), findsOneWidget);
        await tocar(tester, find.widgetWithText(FilledButton, 'Descartar'));
        expect(campo('Nome do EPI'), findsNothing);
        expect(find.text(capaceteJson['nome']! as String), findsOneWidget);
      },
    );
  });
}
