// Testes de widget do registro de manutenção: botão só do Gestor, campos obrigatórios, data e hora
// montadas pelo texto digitado, recusa do backend exibida e registro com aviso.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/epis/data/manutencao.dart';

import '../../apoio.dart';
import '../usuarios/apoio_usuarios.dart'
    show abrirApp, campo, falhaComMensagem, sessaoSupervisor, tocar;
import 'apoio_epis.dart';
import 'epi_detalhe_page_test.dart' show manutencaoJson, responderDetalhe;

void main() {
  late ApiEpisFalsa api;

  setUp(() => api = ApiEpisFalsa(responderDetalhe));

  Iterable<Requisicao> envios() =>
      api.recebidas.where((r) => r.metodo == 'POST');

  Future<void> abrirFormulario(WidgetTester tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis/1');
    await tocar(tester, find.text('Registrar manutenção'));
  }

  Future<void> escolher<T>(
    WidgetTester tester,
    String rotulo,
    String opcao,
  ) async {
    await tocar(
      tester,
      find.widgetWithText(DropdownButtonFormField<T>, rotulo),
    );
    await tocar(tester, find.text(opcao).last);
  }

  Future<void> preencher(WidgetTester tester) async {
    await tester.enterText(campo('Data da manutenção'), '20112026');
    await tester.enterText(campo('Hora da manutenção'), '0905');
    await escolher<TipoManutencao>(tester, 'Tipo (opcional)', 'Corretiva');
    await tester.enterText(
      campo('Descrição da manutenção'),
      ' Troca da jugular ',
    );
    await escolher<ResultadoManutencao>(tester, 'Resultado', 'Reprovado');
  }

  testWidgets('Supervisor não vê o botão de manutenção', (tester) async {
    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/epis/1');

    expect(find.text('Entrada de 10 unidades'), findsOneWidget);
    expect(find.text('Registrar manutenção'), findsNothing);
  });

  testWidgets('campos obrigatórios vazios não são enviados', (tester) async {
    await abrirFormulario(tester);
    await tocar(tester, find.text('Salvar manutenção'));

    expect(find.text('Informe a data da manutenção.'), findsOneWidget);
    expect(find.text('Informe a hora da manutenção.'), findsOneWidget);
    expect(find.text('Informe a descrição da manutenção.'), findsOneWidget);
    expect(find.text('Escolha o resultado da manutenção.'), findsOneWidget);
    expect(envios(), isEmpty);
  });

  testWidgets('registro envia data e hora digitadas e volta com aviso', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? {...manutencaoJson, 'id': 4, 'resultado': 'REPROVADO'}
        : responderDetalhe(r);
    await preencher(tester);
    expect(find.text('20/11/2026'), findsOneWidget);
    expect(find.text('09:05'), findsOneWidget);
    await tocar(tester, find.text('Salvar manutenção'));

    final envio = envios().single;
    expect(envio.caminho, '/epis/1/manutencoes');
    expect(envio.corpo, {
      'dataManutencao': '2026-11-20T09:05',
      'descricao': 'Troca da jugular',
      'resultado': 'REPROVADO',
      'tipoManutencao': 'CORRETIVA',
    });
    expect(
      find.text(
        'Manutenção registrada. Situação e histórico foram atualizados.',
      ),
      findsOneWidget,
    );
    expect(
      api.recebidas.where((r) => r.caminho == '/epis/1' && r.metodo == 'GET'),
      hasLength(2), // leitura inicial e nova leitura com o status atualizado
    );
  });

  testWidgets('recusa do backend aparece e os dados ficam preenchidos', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? throw falhaComMensagem(400, 'EPI descartado não recebe manutenção.')
        : responderDetalhe(r);
    await preencher(tester);
    await tocar(tester, find.text('Salvar manutenção'));

    expect(find.text('Manutenção não registrada'), findsOneWidget);
    expect(find.text('EPI descartado não recebe manutenção.'), findsOneWidget);
    expect(find.text('Troca da jugular'), findsNothing);
    expect(find.text(' Troca da jugular '), findsOneWidget);
  });
}
