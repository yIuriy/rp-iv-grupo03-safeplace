import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import 'apoio_usuarios.dart';

void main() {
  late ApiUsuariosFalsa api;

  setUp(() => api = ApiUsuariosFalsa());

  testWidgets('mostra os dados da conta com o CPF oculto e as ações recentes', (
    tester,
  ) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/perfil');

    expect(
      find.descendant(
        of: find.byType(AppBar),
        matching: find.text('Meu perfil'),
      ),
      findsOneWidget,
    );
    expect(find.text('Gestor Demo'), findsOneWidget);
    expect(find.text('Gestor de Segurança'), findsOneWidget);
    expect(find.text('gestor.demo@safeplace.com'), findsOneWidget);
    expect(find.text('•••.533.447-••'), findsOneWidget);
    expect(find.text('15/01/1980'), findsOneWidget);
    expect(find.text('07/09/2026'), findsOneWidget);

    await tester.scrollUntilVisible(find.text('Supervisor 2'), 200);
    expect(find.text('Cadastro'), findsOneWidget);
    expect(find.text('05/10/2026 20:53'), findsOneWidget);
    expect(find.text('Cadastro de Supervisor'), findsOneWidget);
    await tester.scrollUntilVisible(find.text('Empréstimo de EPI 4'), 200);
    expect(find.text('Alteração'), findsOneWidget);
    expect(find.text('Sem descrição'), findsOneWidget);

    expect(api.chamadas('GET', '/usuarios/me/historico').single.consulta, {
      'limite': 20,
    });
    expect(find.text('Ver mais ações'), findsNothing);
  });

  testWidgets('"Ver mais ações" pede mais 20, até o limite', (tester) async {
    api.responder = (r) => r.caminho == '/usuarios/me/historico'
        ? [
            for (var i = 0; i < (r.consulta!['limite']! as int); i++)
              {
                'id': i,
                'tipoOperacao': 'CADASTRO',
                'entidade': 'Colaborador',
                'entidadeId': i,
              },
          ]
        : responderPadrao(r);

    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/perfil');
    await tocar(tester, find.text('Ver mais ações'));

    expect(
      api.chamadas('GET', '/usuarios/me/historico').map((r) => r.consulta),
      [
        {'limite': 20},
        {'limite': 40},
      ],
    );
  });

  testWidgets('sem ações, mostra o estado vazio', (tester) async {
    api.responder = (r) => r.caminho == '/usuarios/me/historico'
        ? <Object?>[]
        : responderPadrao(r);

    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/perfil');
    await tester.scrollUntilVisible(find.text('Nenhuma ação registrada'), 200);

    expect(find.text('Nenhuma ação registrada'), findsOneWidget);
  });

  testWidgets('falha no histórico não esconde os dados do perfil', (
    tester,
  ) async {
    api.responder = (r) => r.caminho == '/usuarios/me/historico'
        ? throw falhaComMensagem(500, 'erro interno')
        : responderPadrao(r);

    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/perfil');

    expect(find.text('Gestor Demo'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Não foi possível carregar as suas ações.'),
      200,
    );
    expect(find.text('Tentar novamente'), findsOneWidget);
  });

  testWidgets('modo offline: perfil e histórico da cópia, com uma faixa só', (
    tester,
  ) async {
    final cache = ArmazenamentoCacheEmMemoria();
    final copia = CacheOffline(cache);
    await copia.guardar(sessaoGestor.email, 'usuarios/me', gestorJson);
    await copia.guardar(
      sessaoGestor.email,
      'usuarios/me/historico?limite=20',
      historicoJson,
    );
    api.responder = servidorFora;

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/perfil',
      cache: cache,
    );

    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
    expect(find.text('Gestor Demo'), findsOneWidget);
    await tester.scrollUntilVisible(find.text('Supervisor 2'), 200);
    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
  });
}
