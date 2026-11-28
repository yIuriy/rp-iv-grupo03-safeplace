// Testes do cadastro e da edição de tarefa: validação local, corpo enviado, falha do servidor com os
// dados mantidos e acesso só do Gestor.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/tarefas/data/tarefa.dart';
import 'package:safeplace/features/tarefas/data/tarefas_api.dart';
import 'package:safeplace/features/tarefas/domain/tarefa_repository.dart';
import 'package:safeplace/features/tarefas/domain/validacao_tarefa.dart';

import '../../apoio.dart';
import 'apoio_tarefas.dart';

void main() {
  test('descrição em branco é recusada', () {
    expect(
      validarDescricaoTarefa('   '),
      'A descrição da atividade é obrigatória.',
    );
    expect(validarDescricaoTarefa('Solda'), isNull);
  });

  group('TarefasApi', () {
    test('cadastro leva o grau e omite a área vazia', () async {
      final api = ApiUsuariosFalsa((_) => limpezaJson);

      await TarefasApi(api).cadastrar(
        const DadosTarefa(
          descricao: 'Pintura',
          setoresIds: [2],
          nivelPerigo: NivelPerigo.medio,
        ),
      );

      final r = api.recebidas.single;
      expect((r.metodo, r.caminho), ('POST', '/tarefas'));
      expect(r.corpo, {
        'descricao': 'Pintura',
        'setoresIds': [2],
        'nivelPerigo': 'MEDIO',
      });
    });

    test('edição usa PUT sem o grau', () async {
      final api = ApiUsuariosFalsa((_) => soldaJson);

      await TarefasApi(api).atualizar(
        1,
        const DadosTarefa(
          descricao: 'Solda',
          areaRiscoId: 3,
          nivelPerigo: NivelPerigo.alto,
        ),
      );

      final r = api.recebidas.single;
      expect((r.metodo, r.caminho), ('PUT', '/tarefas/1'));
      expect(r.corpo, {
        'descricao': 'Solda',
        'areaRiscoId': 3,
        'setoresIds': <int>[],
      });
    });
  });

  group('formulário', () {
    late ApiUsuariosFalsa api;

    setUp(() => api = ApiUsuariosFalsa(responderTarefas));

    testWidgets('Supervisor não vê cadastro nem edição', (tester) async {
      await abrirApp(
        tester,
        sessao: sessaoSupervisor,
        api: api,
        rota: '/tarefas',
      );
      expect(find.text('Cadastrar tarefa'), findsNothing);

      await tocar(tester, find.text('Limpeza do almoxarifado'));
      expect(find.text('Editar tarefa'), findsNothing);
    });

    testWidgets('Gestor cadastra com setor e grau', (tester) async {
      api.responder = (r) =>
          r.metodo == 'POST' ? limpezaJson : responderTarefas(r);
      await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas');

      await tocar(tester, find.text('Cadastrar tarefa'));
      await tocar(
        tester,
        find.widgetWithText(FilledButton, 'Cadastrar tarefa'),
      );
      expect(
        find.text('A descrição da atividade é obrigatória.'),
        findsOneWidget,
      );

      await tester.enterText(
        campo('Descrição da atividade operacional'),
        '  Limpeza do almoxarifado ',
      );
      await tocar(
        tester,
        find.widgetWithText(FilterChip, 'Estoque (Galpão B)'),
      );
      await tocar(tester, find.text('Não classificada no momento'));
      await tocar(tester, find.text('Baixo').last);
      await tocar(
        tester,
        find.widgetWithText(FilledButton, 'Cadastrar tarefa'),
      );

      final post = api.recebidas.singleWhere((r) => r.metodo == 'POST');
      expect(post.corpo, {
        'descricao': 'Limpeza do almoxarifado',
        'setoresIds': [2],
        'nivelPerigo': 'BAIXO',
      });
      expect(
        find.text('Tarefa cadastrada com sucesso: "Limpeza do almoxarifado".'),
        findsOneWidget,
      );
    });

    testWidgets('falha do servidor mantém os dados', (tester) async {
      api.responder = (r) => r.metodo == 'PUT'
          ? throw falhaComMensagem(404, 'Setor não encontrado: 9')
          : responderTarefas(r);
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/tarefas/1',
      );

      await tocar(tester, find.text('Editar tarefa'));
      expect(find.text('Editar tarefa nº 1'), findsOneWidget);
      expect(find.text('Grau de periculosidade (opcional)'), findsNothing);
      await tocar(tester, find.text('Salvar alterações'));

      expect(find.text('Não foi possível salvar'), findsOneWidget);
      expect(find.text('Setor não encontrado: 9'), findsOneWidget);
      expect(
        find.text('Solda em altura na estrutura metálica'),
        findsOneWidget,
      );
      final put = api.recebidas.singleWhere((r) => r.metodo == 'PUT');
      expect(put.corpo, {
        'descricao': 'Solda em altura na estrutura metálica',
        'areaRiscoId': 3,
        'setoresIds': [1],
      });
    });
  });
}
