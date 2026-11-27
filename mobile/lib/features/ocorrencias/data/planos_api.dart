// Chamadas do plano de ação (`/api/ocorrencias/{id}/planos-acao` e `/api/planos-acao`, UC07, #321).
// Escritas pelo `PlanoRepository`; leituras por providers com cache offline somente leitura por
// conta (RNF08). Gestor e Supervisor leem; só o Gestor escreve (o backend responde 403 ao
// Supervisor).

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_client_provider.dart';
import '../../../core/api/erros.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/offline/cache_offline.dart';
import '../domain/plano_repository.dart';
import 'acao_plano.dart';

class PlanosApi implements PlanoRepository {
  const PlanosApi(this._client);

  final ApiClient _client;

  @override
  Future<AcaoPlano> cadastrar(int idOcorrencia, NovaAcao acao) async =>
      AcaoPlano.ler(
        await _client.post(
          '/ocorrencias/$idOcorrencia/planos-acao',
          corpo: corpoDaNovaAcao(acao),
        ),
      );

  @override
  Future<AcaoPlano> concluir(int idAcao) async => AcaoPlano.ler(
    await _client.patch(
      '/planos-acao/$idAcao/status',
      corpo: {'status': SituacaoAcao.concluida.codigo},
    ),
  );

  @override
  Future<Prorrogacao> prorrogar(
    int idAcao,
    DateTime novoPrazo,
    String justificativa,
  ) async => Prorrogacao.ler(
    await _client.post(
      '/planos-acao/$idAcao/prorrogacoes',
      corpo: {
        'novoPrazo': dataIso(novoPrazo),
        'justificativa': justificativa.trim(),
      },
    ),
  );

  @override
  Future<AcaoPlano> reatribuir(
    int idAcao,
    TipoResponsavel tipo,
    int responsavelId,
  ) async => AcaoPlano.ler(
    await _client.patch(
      '/planos-acao/$idAcao/responsavel',
      corpo: {
        if (tipo == TipoResponsavel.colaborador) 'colaboradorId': responsavelId,
        if (tipo == TipoResponsavel.setor) 'setorId': responsavelId,
      },
    ),
  );

  @override
  Future<AcaoPlano> configurarAlerta(
    int idAcao, {
    required bool ativo,
    int? antecedenciaDias,
  }) async => AcaoPlano.ler(
    await _client.patch(
      '/planos-acao/$idAcao/alerta',
      corpo: {'ativo': ativo, if (ativo) 'antecedenciaDias': antecedenciaDias},
    ),
  );
}

/// Corpo de `CriarPlanoDeAcaoRequest`: medidas vazias e o responsável que não é o escolhido não vão.
Map<String, Object?> corpoDaNovaAcao(NovaAcao acao) => {
  'medidasCorretivas': ?acao.medidasCorretivas,
  'medidasPreventivas': ?acao.medidasPreventivas,
  'prazo': dataIso(acao.prazo),
  if (acao.responsavelTipo == TipoResponsavel.colaborador)
    'responsavelColaboradorId': acao.responsavelId,
  if (acao.responsavelTipo == TipoResponsavel.setor)
    'responsavelSetorId': acao.responsavelId,
  'alertaAtivo': acao.alertaAtivo,
  if (acao.alertaAtivo) 'antecedenciaAlertaDias': acao.antecedenciaAlertaDias,
};

final planoRepositoryProvider = Provider<PlanoRepository>(
  (ref) => PlanosApi(ref.read(apiClientProvider)),
);

/// Ações do plano de uma ocorrência, com cópia offline por conta.
final acoesDoPlanoProvider = FutureProvider.autoDispose
    .family<Consultado<List<AcaoPlano>>, int>(
      (ref, idOcorrencia) => consultarComCache(
        ref,
        chave: 'ocorrencias/$idOcorrencia/planos-acao',
        buscar: (api) => api.get('/ocorrencias/$idOcorrencia/planos-acao'),
        ler: lerAcoesDoPlano,
      ),
      retry: (_, _) => null,
    );

/// Histórico de prorrogações de uma ação, com cópia offline por conta.
final prorrogacoesProvider = FutureProvider.autoDispose
    .family<Consultado<List<Prorrogacao>>, int>(
      (ref, idAcao) => consultarComCache(
        ref,
        chave: 'planos-acao/$idAcao/prorrogacoes',
        buscar: (api) => api.get('/planos-acao/$idAcao/prorrogacoes'),
        ler: lerProrrogacoes,
      ),
      retry: (_, _) => null,
    );

/// Mensagens das falhas do plano. As recusas de regra ("O novo prazo deve ser posterior ao prazo
/// atual", "O colaborador informado já é o responsável") chegam no `message` do backend.
final descreverFalhaDoPlano = criarDescritorDeFalha(
  porStatus: {
    400: usarMensagemDaApi(
      'Os dados da ação foram recusados pelo servidor. Revise e tente novamente.',
    ),
    403: fixa('Só o Gestor de Segurança altera o plano de ação.'),
    404: usarMensagemDaApi(
      'A ação não existe mais. Recarregue o plano e tente novamente.',
    ),
  },
  padrao: 'Não foi possível salvar a alteração agora. Verifique sua conexão e tente novamente.',
);
