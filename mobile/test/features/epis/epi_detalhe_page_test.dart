// Testes de widget do detalhe de EPI (`/epis/{id}`): abertura pela lista, dados e históricos,
// históricos vazios, endereço inválido e modo offline com um único aviso.

import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import '../usuarios/apoio_usuarios.dart' show abrirApp, sessaoSupervisor, tocar;
import 'apoio_epis.dart';
import 'epi_test.dart' show capaceteJson;
import 'epis_page_test.dart' show luvaCriticaJson;

const entradaJson = <String, Object?>{
  'id': 7,
  'epiId': 1,
  'tipo': 'ENTRADA',
  'quantidade': 10,
  'dataHora': '2026-11-18T14:30:00',
  'motivo': 'Compra do lote L-2026-01',
  'saldoAtual': 25,
};

const manutencaoJson = <String, Object?>{
  'id': 3,
  'epiId': 1,
  'dataManutencao': '2026-11-19T09:05:00',
  'tipoManutencao': 'PREVENTIVA',
  'descricao': 'Inspeção da carneira',
  'resultado': 'APROVADO',
  'statusAtualEpi': 'DISPONIVEL',
};

Object? responderDetalhe(Requisicao r) => switch (r.caminho) {
  '/epis' => [capaceteJson, luvaCriticaJson],
  '/epis/1' => capaceteJson,
  '/epis/1/movimentacoes' => [entradaJson],
  '/epis/1/manutencoes' => [manutencaoJson],
  _ => throw UnimplementedError('Chamada inesperada: $r'),
};

void main() {
  late ApiEpisFalsa api;

  setUp(() => api = ApiEpisFalsa(responderDetalhe));

  testWidgets('toque no cartão abre dados e os dois históricos', (
    tester,
  ) async {
    await abrirApp(tester, sessao: sessaoSupervisor, api: api, rota: '/epis');
    await tocar(tester, find.text('Capacete de Segurança com Aba Frontal'));

    expect(find.text('25 (mínimo 5)'), findsOneWidget);
    expect(find.text('Proteção de cabeça'), findsOneWidget);
    expect(find.text('12345, válido até 31/12/2027'), findsOneWidget);
    expect(find.text('365 dias'), findsOneWidget);
    expect(find.text('Entrada de 10 unidades'), findsOneWidget);
    expect(
      find.text('Motivo: Compra do lote L-2026-01. Saldo após: 25.'),
      findsOneWidget,
    );
    await tester.scrollUntilVisible(find.text('Inspeção da carneira'), 200);
    expect(find.text('19/11/2026 09:05'), findsOneWidget);
    expect(find.text('Preventiva, aprovado'), findsOneWidget);
    expect(find.text('Status do EPI após: Disponível.'), findsOneWidget);

    await tocar(tester, find.text('Voltar para a lista'));
    expect(find.text('Luva de Vaqueta'), findsOneWidget);
  });

  testWidgets('históricos vazios explicam a ausência', (tester) async {
    api.responder = (r) => r.caminho == '/epis/1' ? capaceteJson : <Object?>[];
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis/1');

    expect(find.text('Sem movimentações registradas'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Sem manutenções registradas'),
      200,
    );
    expect(find.text('Sem manutenções registradas'), findsOneWidget);
  });

  testWidgets('endereço sem número mostra EPI não encontrado', (tester) async {
    await abrirApp(tester, sessao: sessaoGestor, api: api, rota: '/epis/abc');

    expect(find.text('EPI não encontrado'), findsOneWidget);
    expect(api.recebidas.where((r) => r.caminho.startsWith('/epis/')), isEmpty);
  });

  testWidgets('modo offline mostra a cópia com um único aviso', (tester) async {
    final cache = ArmazenamentoCacheEmMemoria();
    final copia = CacheOffline(cache);
    await copia.guardar(sessaoGestor.email, 'epis/1', capaceteJson);
    await copia.guardar(sessaoGestor.email, 'epis/1/movimentacoes', [
      entradaJson,
    ]);
    await copia.guardar(sessaoGestor.email, 'epis/1/manutencoes', [
      manutencaoJson,
    ]);
    api.responder = (_) => throw const FalhaApi();

    await abrirApp(
      tester,
      sessao: sessaoGestor,
      api: api,
      rota: '/epis/1',
      cache: cache,
    );

    expect(find.text('Modo offline: exibindo dados locais'), findsOneWidget);
    expect(find.text('Entrada de 10 unidades'), findsOneWidget);
  });
}
