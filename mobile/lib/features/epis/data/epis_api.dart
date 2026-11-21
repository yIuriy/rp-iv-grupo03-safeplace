// Chamadas de `/api/epis`: escritas pelo `EpiRepository` e leituras por providers com cache offline
// somente leitura por conta (RNF08). Lista, detalhe e históricos valem para Gestor e Supervisor.

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_client_provider.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/offline/cache_offline.dart';
import '../domain/epi_repository.dart';
import 'epi.dart';
import 'manutencao.dart';
import 'movimentacao.dart';

/// Escritas de `/api/epis`. Os corpos seguem os `Request` do backend.
class EpisApi implements EpiRepository {
  const EpisApi(this._client);

  final ApiClient _client;

  @override
  Future<Epi> cadastrar(DadosCadastroEpi dados) async =>
      Epi.ler(await _client.post('/epis', corpo: _cadastro(dados)));

  @override
  Future<Movimentacao> movimentar(int epiId, DadosMovimentacao dados) async =>
      Movimentacao.ler(
        await _client.post(
          '/epis/$epiId/movimentacoes',
          corpo: {
            'tipo': dados.tipo.codigo,
            'quantidade': dados.quantidade,
            'motivo': dados.motivo,
          },
        ),
      );

  @override
  Future<Manutencao> registrarManutencao(
    int epiId,
    DadosManutencao dados,
  ) async => Manutencao.ler(
    await _client.post(
      '/epis/$epiId/manutencoes',
      corpo: {
        'dataManutencao': dados.dataManutencao,
        'descricao': dados.descricao,
        'resultado': dados.resultado.codigo,
        'tipoManutencao': ?dados.tipo?.codigo,
      },
    ),
  );

  static Map<String, Object?> _cadastro(DadosCadastroEpi dados) => {
    'nome': dados.nome,
    'numeroCa': dados.numeroCa,
    'quantidade': dados.quantidade,
    'estoqueMinimo': dados.estoqueMinimo,
    'dataValidadeCa': dataIso(dados.dataValidadeCa),
    'classificacao': dados.classificacao.codigo,
    'vidaUtilDias': ?dados.vidaUtilDias,
    'descricao': ?dados.descricao,
    'localizacao': ?dados.localizacao,
    'numeroLote': ?dados.numeroLote,
    'notaFiscal': ?dados.notaFiscal,
    'dataFabricacao': ?_dataOuNula(dados.dataFabricacao),
    'validadeLote': ?_dataOuNula(dados.validadeLote),
    'marca': ?dados.marca,
  };

  static String? _dataOuNula(DateTime? data) =>
      data == null ? null : dataIso(data);
}

final epiRepositoryProvider = Provider<EpiRepository>(
  (ref) => EpisApi(ref.read(apiClientProvider)),
);

// Leituras com cópia local por conta (RNF08). `autoDispose`: ao sair da tela (ou da conta), a
// consulta é descartada e a próxima abertura busca de novo. Sem tentativas automáticas: as telas
// oferecem "Tentar novamente" e o puxar para atualizar.

/// Catálogo com saldo e estoque crítico (`GET /api/epis`). Também alimenta o `EpiPicker`.
final episProvider = FutureProvider.autoDispose<Consultado<List<Epi>>>(
  (ref) => consultarComCache(
    ref,
    chave: 'epis',
    buscar: (api) => api.get('/epis'),
    ler: lerEpis,
  ),
  retry: (_, _) => null,
);

/// Detalhe de um EPI (`GET /api/epis/{id}`).
final epiProvider = FutureProvider.autoDispose.family<Consultado<Epi>, int>(
  (ref, id) => consultarComCache(
    ref,
    chave: 'epis/$id',
    buscar: (api) => api.get('/epis/$id'),
    ler: Epi.ler,
  ),
  retry: (_, _) => null,
);

/// Histórico de estoque de um EPI (`GET /api/epis/{id}/movimentacoes`).
final movimentacoesProvider = FutureProvider.autoDispose
    .family<Consultado<List<Movimentacao>>, int>(
      (ref, id) => consultarComCache(
        ref,
        chave: 'epis/$id/movimentacoes',
        buscar: (api) => api.get('/epis/$id/movimentacoes'),
        ler: lerMovimentacoes,
      ),
      retry: (_, _) => null,
    );

/// Histórico de manutenção de um EPI (`GET /api/epis/{id}/manutencoes`).
final manutencoesProvider = FutureProvider.autoDispose
    .family<Consultado<List<Manutencao>>, int>(
      (ref, id) => consultarComCache(
        ref,
        chave: 'epis/$id/manutencoes',
        buscar: (api) => api.get('/epis/$id/manutencoes'),
        ler: lerManutencoes,
      ),
      retry: (_, _) => null,
    );
