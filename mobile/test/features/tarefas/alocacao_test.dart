// Testes da alocação de colaborador: leitura dos modelos, consulta de aptidão ao escolher, 409 de
// tarefa sem classificação, 422 com cada motivo listado e confirmação pelo Supervisor.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/tarefas/data/alocacao.dart';
import 'package:safeplace/features/usuarios/presentation/colaborador_picker.dart';

import '../../apoio.dart';
import 'apoio_tarefas.dart';

const alocacaoJson = <String, Object?>{
  'id': 4,
  'tarefaId': 1,
  'tarefaDescricao': 'Solda em altura na estrutura metálica',
  'colaboradorId': 10,
  'colaboradorNome': 'João da Silva',
  'dataAlocacao': '2026-11-22T07:30:00',
  'responsavelAlocacaoId': 2,
  'responsavelNome': 'Joana Ribeiro',
  'observacao': 'Turno da manhã',
};

const motivos = [
  'Certificação NR-35 vencida em 01/10/2026.',
  'Treinamento de solda pendente.',
];

void main() {
  late ApiUsuariosFalsa api;

  Object? responder(Requisicao r) => switch ((r.metodo, r.caminho)) {
    ('GET', '/tarefas/1/alocacoes') => [alocacaoJson],
    ('GET', '/tarefas/2/alocacoes') => <Object?>[],
    ('GET', '/tarefas/1/aptidao/10') => {'apto': true, 'impedimentos': []},
    ('GET', '/tarefas/1/aptidao/11') => {
      'apto': false,
      'impedimentos': motivos,
    },
    ('GET', '/tarefas/1/historico') => <Object?>[],
    ('POST', '/tarefas/1/alocacoes') => alocacaoJson,
    _ => responderTarefas(r),
  };

  setUp(() => api = ApiUsuariosFalsa(responder));

  Future<void> escolher(WidgetTester tester, String nome) async {
    await tester.tap(find.bySemanticsLabel('Colaborador'));
    await tester.pumpAndSettle();
    await tester.tap(
      find.descendant(of: find.byType(ListTile), matching: find.text(nome)),
    );
    await tester.pumpAndSettle();
  }

  group('modelos', () {
    test('alocação e aptidão com campos ausentes', () {
      final alocacao = AlocacaoTarefa.ler({'id': 1});
      expect(alocacao.colaboradorNome, 'Colaborador sem nome');
      expect(alocacao.dataAlocacao, isNull);

      final aptidao = ConsultaAptidao.ler({
        'impedimentos': ['A', 3, null],
      });
      expect(aptidao.apto, isFalse);
      expect(aptidao.impedimentos, ['A']);
    });

    test('impedimentos só saem do 422', () {
      final corpo = {'message': 'Bloqueada', 'impedimentos': motivos};
      expect(impedimentosDaFalha(falhaHttp(422, corpo)), motivos);
      expect(impedimentosDaFalha(falhaHttp(409, corpo)), isEmpty);
      expect(impedimentosDaFalha(falhaHttp(422, 'texto')), isEmpty);
    });
  });

  testWidgets('detalhe lista as alocações registradas', (tester) async {
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/tarefas/1',
    );

    await tester.scrollUntilVisible(find.text('João da Silva'), 200);
    expect(find.text('Alocado por Joana Ribeiro'), findsOneWidget);
    expect(find.text('Turno da manhã'), findsOneWidget);
  });

  testWidgets('Supervisor confirma colaborador apto', (tester) async {
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/tarefas/1',
    );

    await tocar(tester, find.text('Alocar colaborador'));
    await tocar(tester, find.text('Confirmar alocação'));
    expect(find.text(ColaboradorPicker.mensagemObrigatorio), findsOneWidget);

    await escolher(tester, 'João da Silva');
    expect(find.text('Colaborador apto'), findsOneWidget);
    await tester.enterText(campo('Observação (opcional)'), ' Turno da manhã ');
    await tocar(tester, find.text('Confirmar alocação'));

    final post = api.recebidas.singleWhere((r) => r.metodo == 'POST');
    expect(post.corpo, {'colaboradorId': 10, 'observacao': 'Turno da manhã'});
    expect(find.text('João da Silva alocado(a) na tarefa.'), findsOneWidget);
  });

  testWidgets('aptidão com pendências lista cada motivo', (tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/1');

    await tocar(tester, find.text('Alocar colaborador'));
    await escolher(tester, 'Ana Souza');

    expect(find.text('Colaborador com pendências'), findsOneWidget);
    for (final motivo in motivos) {
      expect(find.text(motivo), findsOneWidget);
    }
  });

  testWidgets('422 lista os motivos do bloqueio um a um', (tester) async {
    api.responder = (r) => r.metodo == 'POST'
        ? throw falhaHttp(422, {
            'status': 422,
            'error': 'Alocação Bloqueada',
            'message': 'Colaborador não atende às exigências da tarefa.',
            'impedimentos': motivos,
          })
        : responder(r);
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/tarefas/1');

    await tocar(tester, find.text('Alocar colaborador'));
    await escolher(tester, 'João da Silva');
    await tocar(tester, find.text('Confirmar alocação'));

    expect(find.text('Alocação bloqueada'), findsOneWidget);
    expect(
      find.text('Colaborador não atende às exigências da tarefa.'),
      findsOneWidget,
    );
    for (final motivo in motivos) {
      expect(find.text(motivo), findsOneWidget);
    }
  });

  testWidgets('tarefa sem classificação avisa e mostra o 409', (tester) async {
    api.responder = (r) => switch ((r.metodo, r.caminho)) {
      ('GET', '/tarefas/2/aptidao/10') => {'apto': true},
      ('POST', '/tarefas/2/alocacoes') => throw falhaComMensagem(
        409,
        'A tarefa "Limpeza do almoxarifado" não possui grau de periculosidade classificado.',
      ),
      _ => responder(r),
    };
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/tarefas/2',
    );

    await tocar(tester, find.text('Alocar colaborador'));
    expect(find.text('Tarefa sem classificação'), findsOneWidget);

    await escolher(tester, 'João da Silva');
    await tocar(tester, find.text('Confirmar alocação'));

    expect(find.text('Tarefa sem classificação'), findsNWidgets(2));
    expect(
      find.text(
        'A tarefa "Limpeza do almoxarifado" não possui grau de periculosidade classificado.',
      ),
      findsOneWidget,
    );
  });
}
