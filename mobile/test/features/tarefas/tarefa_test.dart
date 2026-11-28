// Testes da camada de lista de tarefas: leitura defensiva do `TarefaResponse` e filtro por grau
// enviado ao servidor, com cópia offline por filtro.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/core/sessao/controlador_sessao.dart';
import 'package:safeplace/features/tarefas/data/tarefa.dart';
import 'package:safeplace/features/tarefas/data/tarefas_api.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';
import 'apoio_tarefas.dart';

void main() {
  group('Tarefa.ler', () {
    test('lê a tarefa classificada com vínculos', () {
      final tarefa = Tarefa.ler(soldaJson);

      expect(tarefa.id, 1);
      expect(tarefa.descricao, 'Solda em altura na estrutura metálica');
      expect(tarefa.nivelPerigo, NivelPerigo.alto);
      expect(tarefa.classificada, isTrue);
      expect(tarefa.dataClassificacao, DateTime(2026, 11, 20, 10, 15, 30));
      expect(tarefa.responsavelClassificacaoNome, 'Gestor Demo');
      expect(tarefa.areaRiscoId, 3);
      expect(tarefa.setoresIds, [1]);
      expect(tarefa.episIds, [5]);
    });

    test('campos ausentes ou de outro tipo não derrubam a leitura', () {
      final tarefa = Tarefa.ler({
        'descricao': '  ',
        'nivelPerigo': 'EXTREMO',
        'setoresIds': [1, 'x', null, 2.0],
        'episIds': 'nenhum',
      });

      expect(tarefa.id, isNull);
      expect(tarefa.descricao, 'Tarefa sem descrição');
      expect(tarefa.nivelPerigo, isNull);
      expect(tarefa.classificada, isFalse);
      expect(tarefa.setoresIds, [1, 2]);
      expect(tarefa.episIds, isEmpty);
    });

    test('sem o campo classificada, o grau decide', () {
      expect(Tarefa.ler({'nivelPerigo': 'BAIXO'}).classificada, isTrue);
    });

    test('lista fora do contrato lança FormatException', () {
      expect(() => lerTarefas({'id': 1}), throwsFormatException);
    });
  });

  group('tarefasProvider', () {
    late ApiUsuariosFalsa api;
    late ArmazenamentoCacheEmMemoria cache;

    Future<ProviderContainer> conteiner() async {
      final c = ProviderContainer(
        overrides: [
          apiClientProvider.overrideWithValue(api),
          armazenamentoCacheProvider.overrideWithValue(cache),
          armazenamentoSessaoProvider.overrideWithValue(
            ArmazenamentoSessaoEmMemoria(sessaoGestor),
          ),
        ],
      );
      addTearDown(c.dispose);
      await c.read(controladorSessaoProvider.future);
      return c;
    }

    setUp(() {
      api = ApiUsuariosFalsa(responderTarefas);
      cache = ArmazenamentoCacheEmMemoria();
    });

    test('envia o grau como filtro e sem grau traz todas', () async {
      final c = await conteiner();

      final altas = await c.read(tarefasProvider(NivelPerigo.alto).future);
      final todas = await c.read(tarefasProvider(null).future);

      expect(altas.dados.map((t) => t.id), [1]);
      expect(todas.dados.map((t) => t.id), [1, 2]);
      expect(api.recebidas.map((r) => r.consulta), [
        {'nivelPerigo': 'ALTO'},
        null,
      ]);
    });

    test('sem conexão devolve a cópia do mesmo filtro', () async {
      final c = await conteiner();
      await c.read(tarefasProvider(NivelPerigo.alto).future);
      api.responder = servidorFora;

      final copia = await c.refresh(tarefasProvider(NivelPerigo.alto).future);

      expect(copia.somenteLeitura, isTrue);
      expect(copia.dados.single.id, 1);
      await expectLater(
        c.refresh(tarefasProvider(null).future),
        throwsA(isA<FalhaApi>()),
      );
    });
  });
}
