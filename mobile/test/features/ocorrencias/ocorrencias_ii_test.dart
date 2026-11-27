// Testes de widget da #321 no detalhe `/ocorrencias/{id}`: acompanhamento do Gestor (editar,
// triagem, arquivamento com confirmação, CAT, recusa do servidor), plano de ação (selos, concluir,
// nova ação, prorrogação), visão do Supervisor e modo offline.

import 'package:flutter_test/flutter_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/features/ocorrencias/data/acao_plano.dart';
import 'package:safeplace/shared/formularios/data.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart';
import 'apoio_ocorrencias.dart';
import 'apoio_plano.dart';

void main() {
  late ApiUsuariosFalsa api;
  late Map<String, Object?> acidente;
  late Map<String, Object?> incidente;
  late List<Object?> planoDoAcidente;

  /// Servidor com estado: as operações mudam a ocorrência, e a próxima leitura já vem atualizada.
  Object? servidor(Requisicao r) => switch ((r.metodo, r.caminho)) {
    ('GET', '/ocorrencias/7') => acidente,
    ('GET', '/ocorrencias/8') => incidente,
    ('GET', '/ocorrencias/7/planos-acao') => planoDoAcidente,
    ('GET', '/ocorrencias/8/planos-acao') => <Object?>[],
    ('GET', '/planos-acao/21/prorrogacoes') => [prorrogacaoJson],
    ('PATCH', '/ocorrencias/7/triagem') => acidente = {
      ...acidente,
      'statusOcorrencia': 'EM_TRIAGEM',
    },
    ('PATCH', '/ocorrencias/8/arquivar') => incidente = {
      ...incidente,
      'statusOcorrencia': 'ARQUIVADA',
    },
    ('PATCH', '/ocorrencias/7/consolidar-cat') => acidente = {
      ...acidente,
      'numeroProtocoloCAT': 'CAT-2026-11-0042',
    },
    ('PUT', '/ocorrencias/7') => acidente = {
      ...acidente,
      'descricao': (r.corpo! as Map)['descricao'],
    },
    ('PATCH', '/planos-acao/21/status') => {
      ...acaoAtrasadaJson,
      'situacao': 'CONCLUIDA',
      'status': 'CONCLUIDA',
    },
    ('POST', '/ocorrencias/7/planos-acao') => {...acaoAtrasadaJson, 'id': 23},
    ('POST', '/planos-acao/21/prorrogacoes') => prorrogacaoJson,
    _ => responderOcorrencias(r),
  };

  setUp(() {
    acidente = {...acidenteJson};
    incidente = {...incidenteJson};
    planoDoAcidente = [acaoAtrasadaJson, acaoConcluidaJson];
    api = ApiUsuariosFalsa(servidor);
  });

  Iterable<Requisicao> chamadas(String metodo, String caminho) =>
      api.chamadas(metodo, caminho);

  Finder noDialogo(String texto) =>
      find.descendant(of: find.byType(AlertDialog), matching: find.text(texto));

  String daquiA(int dias) => formatarData(hoje().add(Duration(days: dias)));

  /// Formulário em tela cheia: rola a lista dele até o [alvo], que pode ainda não ter sido montado.
  Future<void> tocarNoFormulario(WidgetTester tester, Finder alvo) async {
    await tester.scrollUntilVisible(
      alvo,
      200,
      scrollable: find
          .descendant(of: find.byType(Form), matching: find.byType(Scrollable))
          .first,
    );
    await tocar(tester, alvo);
  }

  group('Acompanhamento do Gestor (State + Command)', () {
    testWidgets('aberta: edita e envia para triagem; depois oferece arquivar', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );

      expect(find.text('Acompanhamento'), findsOneWidget);
      expect(find.text('Próxima etapa: em triagem.'), findsOneWidget);
      expect(find.text('Editar dados'), findsOneWidget);
      expect(find.text('Arquivar'), findsNothing);

      await tocar(
        tester,
        find.widgetWithText(FilledButton, 'Enviar para triagem'),
      );
      expect(find.text('Enviar para triagem?'), findsOneWidget);
      await tocar(tester, noDialogo('Enviar para triagem'));

      expect(chamadas('PATCH', '/ocorrencias/7/triagem'), hasLength(1));
      expect(find.text('Ocorrência enviada para triagem.'), findsOneWidget);
      expect(find.widgetWithText(FilledButton, 'Arquivar'), findsOneWidget);
      expect(find.text('Enviar para triagem'), findsNothing);
    });

    testWidgets(
      'arquivar pede confirmação e mostra a recusa do servidor dentro do diálogo',
      (tester) async {
        api.responder = (r) => r.metodo == 'PATCH'
            ? throw falhaComMensagem(
                400,
                'A ocorrência tem ações abertas no plano.',
              )
            : servidor(r);
        await abrirApp(
          tester,
          sessao: sessaoGestor,
          api: api,
          rota: '/ocorrencias/8',
        );

        await tocar(tester, find.widgetWithText(FilledButton, 'Arquivar'));
        expect(find.text('Arquivar ocorrência?'), findsOneWidget);
        await tocar(tester, noDialogo('Confirmar arquivamento'));

        expect(
          noDialogo('A ocorrência tem ações abertas no plano.'),
          findsOneWidget,
        );
        expect(find.text('Arquivar ocorrência?'), findsOneWidget);

        api.responder = servidor;
        await tocar(tester, noDialogo('Confirmar arquivamento'));

        expect(find.text('Ocorrência arquivada.'), findsOneWidget);
        expect(find.text('Ocorrência arquivada'), findsOneWidget);
        expect(find.text('Editar dados'), findsNothing);
        expect(find.text('Nova ação'), findsNothing);
      },
    );

    testWidgets('acidente sem protocolo: emite a CAT e mostra o número', (
      tester,
    ) async {
      acidente = {...acidenteJson, 'numeroProtocoloCAT': null};
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );

      await tocar(
        tester,
        find.widgetWithText(FilledButton, 'Emitir protocolo CAT'),
      );
      await tocar(tester, noDialogo('Emitir protocolo'));

      expect(chamadas('PATCH', '/ocorrencias/7/consolidar-cat'), hasLength(1));
      expect(
        find.text('Protocolo CAT CAT-2026-11-0042 emitido.'),
        findsOneWidget,
      );
      expect(find.text('Emitir protocolo CAT'), findsNothing);
    });

    testWidgets('editar abre o relato preenchido e envia o PUT sem o tipo', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );

      await tocar(tester, find.text('Editar dados'));
      expect(find.text('Editar ocorrência'), findsOneWidget);
      expect(find.text('Corte na mão ao operar a prensa'), findsOneWidget);

      await tester.enterText(
        campo('Descrição dos fatos'),
        'Corte na mão direita ao operar a prensa',
      );
      await tocarNoFormulario(tester, find.text('Salvar alterações'));

      final corpo = chamadas('PUT', '/ocorrencias/7').single.corpo! as Map;
      expect(corpo['descricao'], 'Corte na mão direita ao operar a prensa');
      expect(corpo.containsKey('tipoOcorrencia'), isFalse);
      expect(corpo['dataOcorrencia'], '2026-11-10T08:30:00');
      expect(find.text('Alterações salvas.'), findsOneWidget);
    });
  });

  group('Plano de ação', () {
    testWidgets('mostra resumo, selos, aviso de atraso e prorrogações', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );
      await tester.scrollUntilVisible(
        find.text('Ação #21'),
        300,
        scrollable: find.byType(Scrollable).first,
      );

      expect(
        find.text('2 ações: 1 aberta, 1 em atraso, 1 com alerta de prazo.'),
        findsOneWidget,
      );
      expect(find.text('Em atraso'), findsOneWidget);
      expect(find.text('Alerta de prazo'), findsOneWidget);
      expect(find.text('Prazo vencido'), findsOneWidget);
      expect(find.text('Colaborador: João da Silva'), findsOneWidget);

      await tocar(tester, find.text('Histórico de prorrogações').first);
      expect(find.text('10/11/2026 → 20/11/2026'), findsOneWidget);
      expect(
        find.text('Peça de reposição em falta no fornecedor'),
        findsOneWidget,
      );
    });

    testWidgets('concluir pede confirmação e envia CONCLUIDA', (tester) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );

      // Só a ação aberta tem operações.
      expect(find.widgetWithText(FilledButton, 'Concluir'), findsOneWidget);
      await tocar(tester, find.widgetWithText(FilledButton, 'Concluir'));
      await tocar(tester, noDialogo('Confirmar conclusão'));

      expect(chamadas('PATCH', '/planos-acao/21/status').single.corpo, {
        'status': 'CONCLUIDA',
      });
      expect(find.text('Ação #21 concluída.'), findsOneWidget);
    });

    testWidgets('nova ação valida localmente e envia o responsável escolhido', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );

      await tocar(tester, find.widgetWithText(FilledButton, 'Nova ação'));
      expect(find.text('Nova ação do plano'), findsOneWidget);
      expect(find.text('Acidente grave'), findsOneWidget);

      await tocarNoFormulario(tester, find.text('Cadastrar ação'));
      expect(
        find.text('Informe a medida corretiva, a preventiva ou ambas.'),
        findsOneWidget,
      );
      expect(find.text('Informe o prazo da ação.'), findsOneWidget);
      expect(
        find.text('Indique se o responsável é um colaborador ou um setor.'),
        findsOneWidget,
      );
      expect(chamadas('POST', '/ocorrencias/7/planos-acao'), isEmpty);

      await tester.enterText(
        campo('Medida preventiva'),
        'Treinar a equipe no uso da prensa',
      );
      await tester.enterText(campo('Prazo'), daquiA(10));
      await tocarNoFormulario(
        tester,
        find
            .widgetWithText(
              DropdownButtonFormField<TipoResponsavel>,
              'Tipo de responsável',
            )
            .first,
      );
      await tocar(tester, find.text('Colaborador').last);
      await tocarNoFormulario(tester, find.text('Colaborador responsável'));
      await tocar(tester, find.text('Ana Souza').last);
      await tocarNoFormulario(tester, find.text('Cadastrar ação'));

      final corpo =
          chamadas('POST', '/ocorrencias/7/planos-acao').single.corpo! as Map;
      expect(corpo['medidasPreventivas'], 'Treinar a equipe no uso da prensa');
      expect(corpo['responsavelColaboradorId'], 11);
      expect(corpo.containsKey('responsavelSetorId'), isFalse);
      expect(corpo['prazo'], dataIso(hoje().add(const Duration(days: 10))));
      expect(find.text('Ação #23 cadastrada no plano.'), findsOneWidget);
    });

    testWidgets('prorrogar exige prazo posterior ao atual e justificativa', (
      tester,
    ) async {
      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
      );

      await tocar(
        tester,
        find.widgetWithText(OutlinedButton, 'Prorrogar prazo'),
      );
      expect(find.text('Prazo atual: 20/11/2026.'), findsOneWidget);

      await tester.enterText(campo('Novo prazo'), '20/11/2026');
      await tocar(tester, find.widgetWithText(FilledButton, 'Prorrogar'));
      expect(
        find.text('O novo prazo deve ser posterior ao prazo atual.'),
        findsOneWidget,
      );
      expect(
        find.text('A justificativa técnica é obrigatória para prorrogar.'),
        findsOneWidget,
      );

      await tester.enterText(campo('Novo prazo'), daquiA(15));
      await tester.enterText(
        campo('Justificativa técnica'),
        'Fornecedor atrasou a peça',
      );
      await tocar(tester, find.widgetWithText(FilledButton, 'Prorrogar'));

      final corpo =
          chamadas('POST', '/planos-acao/21/prorrogacoes').single.corpo! as Map;
      expect(corpo, {
        'novoPrazo': dataIso(hoje().add(const Duration(days: 15))),
        'justificativa': 'Fornecedor atrasou a peça',
      });
      expect(
        find.textContaining('Prazo da ação #21 prorrogado para'),
        findsOneWidget,
      );
    });
  });

  testWidgets('Supervisor acompanha o plano, sem operações', (tester) async {
    await abrirApp(
      tester,
      sessao: sessaoSupervisor,
      api: api,
      rota: '/ocorrencias/7',
    );

    expect(find.text('Acompanhamento'), findsNothing);
    expect(find.text('Editar dados'), findsNothing);
    expect(
      find.text('Acompanhe as medidas definidas pelo Gestor de Segurança.'),
      findsOneWidget,
    );
    expect(find.text('Ação #21', skipOffstage: false), findsOneWidget);
    expect(find.text('Concluir', skipOffstage: false), findsNothing);
    expect(find.text('Nova ação', skipOffstage: false), findsNothing);
  });

  testWidgets(
    'sem conexão: dados e plano da cópia local, alterações desabilitadas',
    (tester) async {
      final cache = ArmazenamentoCacheEmMemoria();
      final copia = CacheOffline(cache);
      await copia.guardar(sessaoGestor.email, 'ocorrencias/7', acidenteJson);
      await copia.guardar(sessaoGestor.email, 'ocorrencias/7/planos-acao', [
        acaoAtrasadaJson,
      ]);
      api.responder = servidorFora;

      await abrirApp(
        tester,
        sessao: sessaoGestor,
        api: api,
        rota: '/ocorrencias/7',
        cache: cache,
      );

      expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
      expect(botao<OutlinedButton>(tester, 'Editar dados').onPressed, isNull);
      expect(
        botao<FilledButton>(tester, 'Enviar para triagem').onPressed,
        isNull,
      );
      expect(
        find.text('Alterações indisponíveis sem conexão.'),
        findsOneWidget,
      );
      expect(find.text('Ação #21', skipOffstage: false), findsOneWidget);
      expect(find.text('Concluir', skipOffstage: false), findsNothing);
      expect(
        find.text(
          'Alterações no plano indisponíveis sem conexão.',
          skipOffstage: false,
        ),
        findsOneWidget,
      );
    },
  );
}
