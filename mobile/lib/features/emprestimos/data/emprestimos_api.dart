// Chamadas de `/api/emprestimos`: escritas pelo `EmprestimoRepository` e leituras por providers
// com cache offline somente leitura por conta (RNF08). Gestor e Supervisor operam o módulo.

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_client_provider.dart';
import '../../../core/api/erros.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/offline/cache_offline.dart';
import '../../epis/data/epi.dart';
import '../domain/emprestimo_repository.dart';
import 'emprestimo.dart';

/// Implementação das operações da API de empréstimos.
class EmprestimosApi implements EmprestimoRepository {
  const EmprestimosApi(this._client);

  final ApiClient _client;

  @override
  Future<Emprestimo> registrarEntrega(DadosEntrega dados) async {
    final corpo = <String, Object?>{
      'epiId': dados.epiId,
      'colaboradorId': dados.colaboradorId,
      'dataDevolucaoPrevista': dataIso(dados.dataDevolucaoPrevista),
      if (dados.confirmarSubstituicao) 'confirmarSubstituicao': true,
      'justificativaSubstituicao': ?dados.justificativaSubstituicao,
    };
    final resposta = await _client.post('/emprestimos', corpo: corpo);
    return Emprestimo.ler(resposta);
  }

  @override
  Future<Emprestimo> registrarDevolucao(
    int emprestimoId,
    EstadoDevolucao estado,
  ) async {
    final resposta = await _client.post(
      '/emprestimos/$emprestimoId/devolucao',
      corpo: {'estado': estado.codigo},
    );
    return Emprestimo.lerDevolucao(resposta);
  }

  @override
  Future<Emprestimo> buscarPorId(int id) async {
    final resposta = await _client.get('/emprestimos/$id');
    return Emprestimo.ler(resposta);
  }
}

final emprestimoRepositoryProvider = Provider<EmprestimoRepository>(
  (ref) => EmprestimosApi(ref.read(apiClientProvider)),
);

/// 409 da entrega: colaborador já possui EPI do mesmo tipo em posse (UC12, exceção III).
String? duplicidadeDaFalha(Object erro) {
  if (statusDoErro(erro) != 409) return null;
  return mensagemDaApi(erro) ??
      'O colaborador já possui um EPI do mesmo tipo em posse. Confirme a substituição e informe a justificativa.';
}

final _falhaEntrega = usarMensagemDaApi(
  'A entrega foi recusada pelo servidor. Confira o colaborador, o EPI e a data.',
);

/// Mensagens de erro para cadastro de entrega (POST /api/emprestimos).
final descreverFalhaDeEntrega = criarDescritorDeFalha(
  porStatus: {
    400: _falhaEntrega,
    403: fixa('Seu perfil não tem permissão para registrar entregas de EPI.'),
    404: _falhaEntrega,
    422: _falhaEntrega,
  },
  padrao:
      'Não foi possível registrar a entrega agora. Os dados preenchidos foram mantidos; tente novamente.',
);

final _emprestimoInativo = usarMensagemDaApi(
  'Este empréstimo não está mais ativo. Feche o diálogo e recarregue a lista.',
);

final _devolucaoRecusada = usarMensagemDaApi(
  'A devolução foi recusada pelo servidor. Confira a condição informada.',
);

/// Mensagens de erro para registro de devolução (POST /api/emprestimos/{id}/devolucao).
final descreverFalhaDeDevolucao = criarDescritorDeFalha(
  porStatus: {
    400: _devolucaoRecusada,
    403: fixa('Seu perfil não tem permissão para registrar devoluções.'),
    404: _emprestimoInativo,
    409: _emprestimoInativo,
  },
  padrao:
      'Não foi possível registrar a devolução agora. A condição escolhida foi mantida; tente novamente.',
);

/// Mensagem de falha para consulta da listagem de empréstimos.
final descreverFalhaDeConsultaEmprestimos = criarDescritorDeFalha(
  padrao: 'Não foi possível carregar os empréstimos agora. Tente novamente.',
);

/// Filtro da consulta `GET /api/emprestimos?situacao=...&colaboradorId=...`.
class FiltroEmprestimos {
  const FiltroEmprestimos({
    this.situacao = SituacaoConsulta.pendentes,
    this.colaboradorId,
  });

  final SituacaoConsulta situacao;
  final int? colaboradorId;

  Map<String, String> get consulta => {
    'situacao': situacao.codigo,
    if (colaboradorId != null) 'colaboradorId': '$colaboradorId',
  };

  String get chaveCache {
    final query = Uri(queryParameters: consulta).query;
    return 'emprestimos?$query';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FiltroEmprestimos &&
          other.situacao == situacao &&
          other.colaboradorId == colaboradorId;

  @override
  int get hashCode => Object.hash(situacao, colaboradorId);
}

// Leituras com cópia local por conta (RNF08)

/// Lista de empréstimos filtrada por situação e colaborador.
final emprestimosProvider = FutureProvider.autoDispose
    .family<Consultado<List<Emprestimo>>, FiltroEmprestimos>(
      (ref, filtro) => consultarComCache(
        ref,
        chave: filtro.chaveCache,
        buscar: (api) => api.get('/emprestimos', consulta: filtro.consulta),
        ler: lerEmprestimos,
      ),
      retry: (_, _) => null,
    );

/// EPIs disponíveis para entrega (saldo > 0 e CA válido), passo 4 do UC12.
final episDisponiveisProvider =
    FutureProvider.autoDispose<Consultado<List<Epi>>>(
      (ref) => consultarComCache(
        ref,
        chave: 'emprestimos/epis-disponiveis',
        buscar: (api) => api.get('/emprestimos/epis-disponiveis'),
        ler: lerEpis,
      ),
      retry: (_, _) => null,
    );

/// Detalhes de um empréstimo específico (`GET /api/emprestimos/{id}`).
final emprestimoProvider =
    FutureProvider.autoDispose.family<Consultado<Emprestimo>, int>(
      (ref, id) => consultarComCache(
        ref,
        chave: 'emprestimos/$id',
        buscar: (api) => api.get('/emprestimos/$id'),
        ler: Emprestimo.ler,
      ),
      retry: (_, _) => null,
    );
