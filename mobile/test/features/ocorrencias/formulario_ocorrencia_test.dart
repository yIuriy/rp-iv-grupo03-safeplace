// Testes de widget do registro de ocorrência: campos obrigatórios, data futura, seletor de área com
// o setor de cada área, corpo enviado para acidente e para incidente, aviso com o protocolo CAT e
// recusa do backend exibida com os dados mantidos.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/core/sessao/sessao.dart';
import 'package:safeplace/features/areas_risco/data/area_risco.dart';
import 'package:safeplace/features/areas_risco/presentation/area_risco_picker.dart';
import 'package:safeplace/features/ocorrencias/data/ocorrencia.dart';
import 'package:safeplace/features/usuarios/presentation/colaborador_picker.dart';

import '../../apoio.dart';
import '../usuarios/apoio_usuarios.dart';
import 'apoio_ocorrencias.dart';

void main() {
  late ApiUsuariosFalsa api;

  setUp(() => api = ApiUsuariosFalsa(responderOcorrencias));

  Iterable<Requisicao> envios() =>
      api.recebidas.where((r) => r.metodo == 'POST');

  final enviar = find.widgetWithText(FilledButton, 'Registrar ocorrência');

  Finder opcao(String nome) =>
      find.descendant(of: find.byType(ListTile), matching: find.text(nome));

  Future<void> abrirFormulario(WidgetTester tester, {Sessao? sessao}) async {
    await abrirApp(
      tester,
      sessao: sessao ?? sessaoGestor,
      api: api,
      rota: '/ocorrencias',
    );
    await tocar(tester, enviar);
  }

  Future<void> escolher<T>(
    WidgetTester tester,
    String rotulo,
    String valor,
  ) async {
    await tocar(
      tester,
      find.widgetWithText(DropdownButtonFormField<T>, rotulo),
    );
    await tocar(tester, find.text(valor).last);
  }

  Future<void> abrirLista(WidgetTester tester, String rotulo) async {
    await tester.ensureVisible(find.bySemanticsLabel(rotulo));
    await tester.pumpAndSettle();
    await tester.tap(find.bySemanticsLabel(rotulo));
    await tester.pumpAndSettle();
  }

  Future<void> escolherNaLista(
    WidgetTester tester,
    String rotulo,
    String nome,
  ) async {
    await abrirLista(tester, rotulo);
    await tester.tap(opcao(nome));
    await tester.pumpAndSettle();
  }

  Future<void> preencherComum(WidgetTester tester, TipoOcorrencia tipo) async {
    await escolher<TipoOcorrencia>(tester, 'Tipo de ocorrência', tipo.rotulo);
    await tester.enterText(campo('Data do fato'), '10112026');
    await tester.enterText(campo('Hora do fato'), '0830');
    await escolherNaLista(tester, 'Colaborador envolvido', 'João da Silva');
    await escolherNaLista(tester, 'Área de risco', 'Prensa hidráulica');
    await tester.ensureVisible(campo('Descrição dos fatos'));
    await tester.enterText(
      campo('Descrição dos fatos'),
      ' Corte na mão ao operar a prensa ',
    );
    await tester.pump();
  }

  Future<void> preencherAcidente(WidgetTester tester) async {
    await preencherComum(tester, TipoOcorrencia.acidente);
    await escolher<CausaRaiz>(tester, 'Causa', 'Falha de EPI');
    await escolher<NivelPerigo>(tester, 'Gravidade', 'Alto');
    await tester.ensureVisible(campo('Tipo de lesão (opcional)'));
    await tester.enterText(campo('Tipo de lesão (opcional)'), 'Corte');
    await tester.pump();
  }

  testWidgets('campos obrigatórios vazios não são enviados', (tester) async {
    await abrirFormulario(tester);
    await tocar(tester, enviar);

    for (final mensagem in [
      'Selecione se foi um acidente ou um incidente.',
      'Informe a data do fato.',
      'Informe a hora do fato.',
      ColaboradorPicker.mensagemObrigatorio,
      AreaRiscoPicker.mensagemObrigatorio,
      'Descreva o que aconteceu.',
    ]) {
      expect(find.text(mensagem), findsOneWidget, reason: mensagem);
    }
    expect(envios(), isEmpty);
  });

  testWidgets('data futura e descrição só com espaços são recusadas', (
    tester,
  ) async {
    await abrirFormulario(tester);
    await tester.enterText(campo('Data do fato'), '01012099');
    await tester.enterText(campo('Descrição dos fatos'), '   ');
    await tocar(tester, enviar);

    expect(
      find.text('A data do fato não pode estar no futuro.'),
      findsOneWidget,
    );
    expect(find.text('Descreva o que aconteceu.'), findsOneWidget);
    expect(envios(), isEmpty);
  });

  testWidgets('acidente exige causa e gravidade', (tester) async {
    await abrirFormulario(tester);
    await preencherComum(tester, TipoOcorrencia.acidente);
    await tocar(tester, enviar);

    expect(find.text('Indique a causa do acidente.'), findsOneWidget);
    expect(find.text('Indique a gravidade do acidente.'), findsOneWidget);
    expect(envios(), isEmpty);
  });

  testWidgets('seletor de área mostra o setor e busca por ele', (tester) async {
    await abrirFormulario(tester);
    await abrirLista(tester, 'Área de risco');

    expect(find.text('SET-01 · Galpão A · perigo alto'), findsOneWidget);
    await tester.enterText(
      find.widgetWithText(TextField, 'Buscar por área, código ou setor'),
      'deposito',
    );
    await tester.pump();

    expect(opcao('Almoxarifado'), findsOneWidget);
    expect(opcao('Prensa hidráulica'), findsNothing);
  });

  testWidgets('Supervisor registra acidente e vê o protocolo CAT', (
    tester,
  ) async {
    await abrirFormulario(tester, sessao: sessaoSupervisor);
    api.responder = (r) =>
        r.metodo == 'POST' ? acidenteJson : responderOcorrencias(r);
    await preencherAcidente(tester);
    await tocar(tester, enviar);

    final envio = envios().single;
    expect(envio.caminho, '/ocorrencias');
    expect(envio.corpo, {
      'tipoOcorrencia': 'ACIDENTE',
      'dataOcorrencia': '2026-11-10T08:30:00',
      'descricao': 'Corte na mão ao operar a prensa',
      'areaId': 3,
      'colaboradorId': 10,
      'causaRaiz': 'FALHA_EPI',
      'gravidade': 'ALTO',
      'tipo': 'Corte',
    });
    expect(
      find.text('Acidente registrado. Protocolo CAT CAT-2026-0007.'),
      findsOneWidget,
    );
    expect(api.chamadas('GET', '/ocorrencias'), hasLength(2));
  });

  testWidgets('incidente envia situação de risco e potencial de dano', (
    tester,
  ) async {
    await abrirFormulario(tester);
    api.responder = (r) =>
        r.metodo == 'POST' ? incidenteJson : responderOcorrencias(r);
    await preencherComum(tester, TipoOcorrencia.incidente);
    await tocar(tester, enviar);
    expect(
      find.text('Descreva a situação de risco observada.'),
      findsOneWidget,
    );

    await tester.enterText(campo('Situação de risco'), 'Piso escorregadio');
    await tester.enterText(campo('Potencial de dano'), 'Queda');
    await tocar(tester, enviar);

    expect(envios().single.corpo, {
      'tipoOcorrencia': 'INCIDENTE',
      'dataOcorrencia': '2026-11-10T08:30:00',
      'descricao': 'Corte na mão ao operar a prensa',
      'areaId': 3,
      'colaboradorId': 10,
      'situacaoRisco': 'Piso escorregadio',
      'potencialDano': 'Queda',
    });
    expect(find.text('Incidente registrado.'), findsOneWidget);
  });

  testWidgets('recusa do backend aparece e os dados ficam', (tester) async {
    await abrirFormulario(tester);
    api.responder = (r) => r.metodo == 'POST'
        ? throw falhaComMensagem(400, 'Área de risco inativa.')
        : responderOcorrencias(r);
    await preencherAcidente(tester);
    await tocar(tester, enviar);

    expect(find.text('Relato não registrado'), findsOneWidget);
    expect(find.text('Área de risco inativa.'), findsOneWidget);
    expect(find.text(' Corte na mão ao operar a prensa '), findsOneWidget);
  });
}
