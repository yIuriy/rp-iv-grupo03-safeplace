import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:mocktail/mocktail.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/features/autenticacao/data/autenticacao_api.dart';
import 'package:safeplace/main.dart';

import '../../apoio.dart';

class _ApiFalsa extends Mock implements AutenticacaoApi {}

void main() {
  late _ApiFalsa api;
  late ArmazenamentoSessaoEmMemoria armazenamento;

  Future<void> abrirApp(WidgetTester tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          armazenamentoSessaoProvider.overrideWithValue(armazenamento),
          autenticacaoApiProvider.overrideWithValue(api),
        ],
        child: const SafePlaceApp(),
      ),
    );
    await tester.pumpAndSettle();
  }

  Finder campo(String rotulo) => find.widgetWithText(TextFormField, rotulo);

  setUp(() {
    api = _ApiFalsa();
    armazenamento = ArmazenamentoSessaoEmMemoria();
  });

  testWidgets('sem sessão, abre o login', (tester) async {
    await abrirApp(tester);
    expect(find.text('Entrar no SafePlace'), findsOneWidget);
  });

  testWidgets('campos vazios mostram as pendências sem chamar a API', (
    tester,
  ) async {
    await abrirApp(tester);
    await tester.tap(find.text('Entrar'));
    await tester.pump();

    expect(find.text('Informe o e-mail de acesso.'), findsOneWidget);
    expect(find.text('Informe a senha.'), findsOneWidget);
    verifyNever(() => api.autenticar(any(), any()));
  });

  testWidgets('login válido abre o início com nome e perfil', (tester) async {
    when(() => api.autenticar(sessaoGestor.email, 'segredo'))
        .thenAnswer((_) async => sessaoGestor);
    await abrirApp(tester);

    await tester.enterText(campo('E-mail'), '  ${sessaoGestor.email} ');
    await tester.enterText(campo('Senha'), 'segredo');
    await tester.tap(find.text('Entrar'));
    await tester.pumpAndSettle();

    expect(find.textContaining('Gestor Demo'), findsOneWidget);
    expect(find.textContaining('Gestor de Segurança'), findsOneWidget);
    expect(armazenamento.sessao, sessaoGestor);
  });

  testWidgets('401 no login mostra e-mail ou senha incorretos', (tester) async {
    when(() => api.autenticar(any(), any()))
        .thenThrow(falhaHttp(401, {'status': 401}));
    await abrirApp(tester);

    await tester.enterText(campo('E-mail'), 'x@y.com');
    await tester.enterText(campo('Senha'), 'errada');
    await tester.tap(find.text('Entrar'));
    await tester.pumpAndSettle();

    expect(find.textContaining('E-mail ou senha incorretos'), findsOneWidget);
  });

  testWidgets('sair volta ao login com aviso', (tester) async {
    armazenamento.sessao = sessaoGestor;
    await abrirApp(tester);

    await tester.tap(find.text('Sair'));
    await tester.pumpAndSettle();

    expect(find.text('Você saiu do SafePlace'), findsOneWidget);
    expect(armazenamento.sessao, isNull);
  });
}
