import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import 'apoio_usuarios.dart';

void main() {
  late ApiUsuariosFalsa api;

  setUp(() => api = ApiUsuariosFalsa());

  Finder naBarra(String texto) =>
      find.descendant(of: find.byType(AppBar), matching: find.text(texto));

  Future<void> abrirAba(WidgetTester tester, String aba) async {
    await tester.tap(find.widgetWithText(Tab, aba));
    await tester.pumpAndSettle();
  }

  Future<void> preencher(
    WidgetTester tester, {
    String? nome,
    String? cpf,
    String? data,
    String? email,
    String rotuloEmail = 'E-mail de contato (opcional)',
  }) async {
    if (nome != null) await tester.enterText(campo('Nome completo'), nome);
    if (cpf != null) await tester.enterText(campo('CPF'), cpf);
    if (data != null) {
      await tester.enterText(campo('Data de nascimento'), data);
    }
    if (email != null) await tester.enterText(campo(rotuloEmail), email);
    await tester.pump();
  }

  group('acesso por perfil', () {
    testWidgets('Gestor vê as abas e a lista só com supervisores', (
      tester,
    ) async {
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/usuarios');

      expect(naBarra('Usuários'), findsOneWidget);
      expect(find.widgetWithText(Tab, 'Supervisores'), findsOneWidget);
      expect(find.widgetWithText(Tab, 'Colaboradores'), findsOneWidget);
      expect(find.text('Joana Ribeiro'), findsOneWidget);
      expect(find.text('Gestor Demo'), findsNothing);
      expect(find.text('João da Silva'), findsNothing);
      expect(find.text('Novo supervisor'), findsOneWidget);
    });

    testWidgets(
      'Supervisor vê só colaboradores: sem lista nem cadastro de supervisores',
      (tester) async {
        await abrirApp(
          tester,
          sessao: sessaoSupervisor,
          api: api,
          rota: '/usuarios',
        );

        expect(naBarra('Colaboradores'), findsOneWidget);
        expect(find.byType(TabBar), findsNothing);
        expect(find.text('Supervisores'), findsNothing);
        expect(find.text('Novo supervisor'), findsNothing);
        expect(find.text('Novo colaborador'), findsOneWidget);
        expect(find.text('João da Silva'), findsOneWidget);
        expect(api.chamadas('GET', '/usuarios'), isEmpty);
      },
    );
  });

  group('colaboradores', () {
    testWidgets('lista mostra CPF oculto, que se revela ao pedir', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );

      expect(find.text('•••.444.777-••'), findsOneWidget);
      expect(find.text('Inativo'), findsOneWidget);
      await tocar(tester, find.byTooltip('Exibir CPF de João da Silva'));
      expect(find.text('111.444.777-35'), findsOneWidget);
    });

    testWidgets('busca por nome e CPF vai ao servidor com os filtros', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );

      await tester.enterText(campo('Nome'), '  joão ');
      await tester.enterText(campo('CPF'), '11144477735');
      api.responder = (_) => [joaoJson];
      await tocar(tester, find.text('Buscar'));

      expect(api.recebidas.last.consulta, {
        'nome': 'joão',
        'cpf': '11144477735',
      });
      expect(find.text('João da Silva'), findsOneWidget);
      expect(find.text('Ana Souza'), findsNothing);

      api.responder = (_) => <Object?>[];
      await tester.enterText(campo('Nome'), 'ninguém');
      await tester.enterText(campo('CPF'), '');
      await tocar(tester, find.text('Buscar'));
      expect(find.text('Nenhum colaborador encontrado'), findsOneWidget);

      api.responder = responderPadrao;
      await tocar(tester, find.text('Limpar filtros'));
      expect(api.recebidas.last.consulta, isNull);
      expect(find.text('Ana Souza'), findsOneWidget);
    });

    testWidgets('CPF incompleto na busca é recusado sem chamar a API', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );
      final antes = api.recebidas.length;

      await tester.enterText(campo('CPF'), '111444');
      await tocar(tester, find.text('Buscar'));

      expect(
        find.text('Para buscar por CPF, informe os 11 dígitos.'),
        findsOneWidget,
      );
      expect(api.recebidas.length, antes);
    });

    testWidgets(
      'cadastro: valida localmente, envia sem senha nem perfil e recarrega a lista',
      (tester) async {
        await abrirApp(
          tester,
          sessao: sessaoSupervisor,
          api: api,
          rota: '/usuarios',
        );
        await tocar(tester, find.text('Novo colaborador'));

        await tocar(tester, find.text('Cadastrar colaborador'));
        expect(find.text('Informe o nome completo.'), findsOneWidget);
        expect(find.text('Informe um CPF com 11 dígitos.'), findsOneWidget);
        expect(find.text('Informe a data de nascimento.'), findsOneWidget);
        expect(find.text('Confira os campos destacados'), findsOneWidget);
        expect(api.chamadas('POST', '/usuarios/colaboradores'), isEmpty);

        api.responder = (r) => r.metodo == 'POST'
            ? {...anaJson, 'id': 20, 'nome': 'Carla Dias'}
            : responderPadrao(r);
        await preencher(
          tester,
          nome: 'Carla Dias',
          cpf: '12345678909',
          data: '03021995',
        );
        expect(
          tester
              .widget<EditableText>(
                find.descendant(
                  of: campo('Data de nascimento'),
                  matching: find.byType(EditableText),
                ),
              )
              .controller
              .text,
          '03/02/1995',
        );
        final listasAntes = api
            .chamadas('GET', '/usuarios/colaboradores')
            .length;
        await tocar(tester, find.text('Cadastrar colaborador'));

        expect(api.chamadas('POST', '/usuarios/colaboradores').single.corpo, {
          'nome': 'Carla Dias',
          'cpf': '12345678909',
          'dataNascimento': '1995-02-03',
        });
        expect(
          find.text('Carla Dias cadastrado sem conta de acesso.'),
          findsOneWidget,
        );
        expect(
          api.chamadas('GET', '/usuarios/colaboradores').length,
          greaterThan(listasAntes),
        );
      },
    );

    testWidgets('409 de CPF duplicado aparece no campo CPF', (tester) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );
      await tocar(tester, find.text('Novo colaborador'));
      api.responder = (r) => r.metodo == 'POST'
          ? throw falhaComMensagem(
              409,
              'CPF já cadastrado no sistema: 11144477735',
              erro: 'Conflito de Dados',
            )
          : responderPadrao(r);

      await preencher(
        tester,
        nome: 'Outro João',
        cpf: cpfJoao,
        data: '01/01/1990',
        email: 'outro@safeplace.com',
      );
      await tocar(tester, find.text('Cadastrar colaborador'));

      expect(
        noCampo('CPF', 'Já existe um cadastro com este CPF.'),
        findsOneWidget,
      );
      expect(
        find.text('Este e-mail já está em uso por outro cadastro.'),
        findsNothing,
      );
      expect(find.text('Outro João'), findsOneWidget, reason: 'dados mantidos');

      // Ao corrigir o CPF, o erro do servidor some.
      await tester.enterText(campo('CPF'), cpfAna);
      await tester.pumpAndSettle(); // o erro some com animação
      expect(find.text('Já existe um cadastro com este CPF.'), findsNothing);
    });

    testWidgets('409 de e-mail duplicado aparece no campo de e-mail', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );
      await tocar(tester, find.text('Novo colaborador'));
      api.responder = (r) => r.metodo == 'POST'
          ? throw falhaComMensagem(
              409,
              'Email já cadastrado no sistema: joao.silva@safeplace.com',
            )
          : responderPadrao(r);

      await preencher(
        tester,
        nome: 'Outro João',
        cpf: cpfAna,
        data: '01/01/1990',
        email: 'joao.silva@safeplace.com',
      );
      await tocar(tester, find.text('Cadastrar colaborador'));

      expect(
        noCampo(
          'E-mail de contato (opcional)',
          'Este e-mail já está em uso por outro cadastro.',
        ),
        findsOneWidget,
      );
      expect(find.text('Já existe um cadastro com este CPF.'), findsNothing);

      // Editar apaga o erro; voltar ao e-mail duplicado e enviar mostra o mesmo erro de novo.
      final email = campo('E-mail de contato (opcional)');
      await tester.enterText(email, 'joao.silva@safeplace.co');
      await tester.pumpAndSettle();
      expect(
        find.text('Este e-mail já está em uso por outro cadastro.'),
        findsNothing,
      );
      await tester.enterText(email, 'joao.silva@safeplace.com');
      await tester.pumpAndSettle();
      expect(
        find.text('Este e-mail já está em uso por outro cadastro.'),
        findsNothing,
      );
      await tocar(tester, find.text('Cadastrar colaborador'));
      expect(
        find.text('Este e-mail já está em uso por outro cadastro.'),
        findsOneWidget,
      );
    });

    testWidgets('falha sem campo aparece no aviso do formulário', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );
      await tocar(tester, find.text('Novo colaborador'));
      api.responder = (r) =>
          r.metodo == 'POST' ? throw const FalhaApi() : responderPadrao(r);

      await preencher(tester, nome: 'Carla', cpf: cpfAna, data: '01/01/1990');
      await tocar(tester, find.text('Cadastrar colaborador'));

      expect(find.text('Não foi possível concluir o cadastro'), findsOneWidget);
      expect(
        find.textContaining('Não foi possível falar com o servidor'),
        findsOneWidget,
      );
    });

    testWidgets(
      'edição: CPF somente leitura e o corpo leva só nome, nascimento e e-mail',
      (tester) async {
        await abrirApp(
          tester,
          sessao: sessaoSupervisor,
          api: api,
          rota: '/usuarios',
        );
        await tocar(tester, find.bySemanticsLabel('Editar João da Silva'));

        expect(naBarra('Editar colaborador'), findsOneWidget);
        final cpf = tester.widget<EditableText>(
          find.descendant(
            of: campo('CPF'),
            matching: find.byType(EditableText),
          ),
        );
        expect(cpf.readOnly, isTrue);
        expect(cpf.controller.text, '111.444.777-35');
        expect(find.text('18/06/1992'), findsOneWidget);

        api.responder = (r) => r.metodo == 'PUT'
            ? {...joaoJson, 'nome': 'João S. Silva'}
            : responderPadrao(r);
        await tester.enterText(campo('Nome completo'), 'João S. Silva');
        await tocar(tester, find.text('Salvar alterações'));

        final put = api.chamadas('PUT', '/usuarios/colaboradores/10').single;
        expect(put.corpo, {
          'nome': 'João S. Silva',
          'dataNascimento': '1992-06-18',
          'email': 'joao.silva@safeplace.com',
        });
        expect(find.text('João S. Silva atualizado.'), findsOneWidget);
      },
    );

    testWidgets('sair do formulário com dados pede confirmação', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/usuarios',
      );
      await tocar(tester, find.text('Novo colaborador'));
      await preencher(tester, nome: 'Rascunho');

      await tocar(tester, find.text('Cancelar'));
      expect(find.text('Descartar o cadastro?'), findsOneWidget);
      await tocar(tester, find.text('Continuar editando'));
      expect(find.text('Rascunho'), findsOneWidget);

      await tocar(tester, find.text('Cancelar'));
      await tocar(tester, find.text('Descartar'));
      expect(find.text('Novo colaborador'), findsOneWidget);
      expect(api.chamadas('POST', '/usuarios/colaboradores'), isEmpty);
    });
  });

  group('supervisores (Gestor)', () {
    testWidgets(
      'cadastro mostra a senha inicial uma única vez e volta à lista',
      (tester) async {
        await abrirApp(
          tester,
          sessao: sessaoGestor,
          api: api,
          rota: '/usuarios',
        );
        await tocar(tester, find.text('Novo supervisor'));
        expect(find.text('E-mail corporativo'), findsOneWidget);

        await preencher(
          tester,
          nome: 'Paulo Lima',
          cpf: cpfAna,
          data: '10/10/1985',
        );
        await tocar(tester, find.text('Cadastrar supervisor'));
        expect(
          find.text('Informe o e-mail corporativo usado no login.'),
          findsOneWidget,
        );

        api.responder = (r) => r.metodo == 'POST'
            ? {
                ...supervisorJson,
                'id': 30,
                'nome': 'Paulo Lima',
                'email': 'paulo@safeplace.com',
                'senhaInicial': 'Xk7!pQ2#mA9z',
              }
            : responderPadrao(r);
        await tester.enterText(
          campo('E-mail corporativo'),
          'paulo@safeplace.com',
        );
        await tocar(tester, find.text('Cadastrar supervisor'));

        expect(api.chamadas('POST', '/usuarios/supervisores').single.corpo, {
          'nome': 'Paulo Lima',
          'cpf': cpfAna,
          'dataNascimento': '1985-10-10',
          'email': 'paulo@safeplace.com',
        });
        expect(naBarra('Supervisor cadastrado'), findsOneWidget);
        expect(find.text('Xk7!pQ2#mA9z'), findsOneWidget);
        expect(find.textContaining('paulo@safeplace.com'), findsOneWidget);

        await tocar(tester, find.text('Concluir'));
        expect(find.text('Xk7!pQ2#mA9z'), findsNothing);
        expect(find.widgetWithText(Tab, 'Supervisores'), findsOneWidget);
        expect(find.text('Paulo Lima cadastrado.'), findsOneWidget);
      },
    );

    testWidgets('edição de supervisor usa a rota de supervisores', (
      tester,
    ) async {
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/usuarios');
      await tocar(tester, find.bySemanticsLabel('Editar Joana Ribeiro'));
      expect(naBarra('Editar supervisor'), findsOneWidget);

      api.responder = (r) =>
          r.metodo == 'PUT' ? supervisorJson : responderPadrao(r);
      await tocar(tester, find.text('Salvar alterações'));

      expect(api.chamadas('PUT', '/usuarios/supervisores/2'), hasLength(1));
    });

    testWidgets('a aba de colaboradores também está disponível ao Gestor', (
      tester,
    ) async {
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/usuarios');
      await abrirAba(tester, 'Colaboradores');

      expect(find.text('João da Silva'), findsOneWidget);
      expect(find.text('Novo colaborador'), findsOneWidget);
    });
  });

  testWidgets('modo offline: mostra a cópia e desabilita cadastrar e editar', (
    tester,
  ) async {
    final cache = ArmazenamentoCacheEmMemoria();
    await CacheOffline(cache)
        .guardar(sessaoSupervisor.email, 'usuarios/colaboradores', [joaoJson]);
    api.responder = servidorFora;

    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/usuarios',
      cache: cache,
    );

    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
    expect(find.text('João da Silva'), findsOneWidget);
    expect(botao<FilledButton>(tester, 'Novo colaborador').onPressed, isNull);
    expect(botao<OutlinedButton>(tester, 'Editar').onPressed, isNull);
    expect(
      find.text('Cadastros e alterações voltam quando a conexão voltar.'),
      findsOneWidget,
    );
  });

  testWidgets('sem conexão e sem cópia: falha com tentar novamente', (
    tester,
  ) async {
    api.responder = servidorFora;
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/usuarios',
    );

    expect(
      find.text('Não foi possível carregar os colaboradores.'),
      findsOneWidget,
    );
    api.responder = responderPadrao;
    await tocar(tester, find.text('Tentar novamente'));
    expect(find.text('João da Silva'), findsOneWidget);
  });
}
