// Chamadas de `/api/ocorrencias` (#318): registro e busca por protocolo pelo `OcorrenciaRepository`,
// e lista e detalhe por providers com cache offline somente leitura por conta (RNF08). Gestor e
// Supervisor consultam e registram, e o Supervisor recebe só os próprios relatos.

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_client_provider.dart';
import '../../../core/api/erros.dart';
import '../../../shared/offline/cache_offline.dart';
import '../domain/ocorrencia_repository.dart';
import 'ocorrencia.dart';

class OcorrenciasApi implements OcorrenciaRepository {
  const OcorrenciasApi(this._client);

  final ApiClient _client;

  @override
  Future<Ocorrencia> registrar(DadosOcorrencia dados) async => Ocorrencia.ler(
    await _client.post('/ocorrencias', corpo: corpoDoRelato(dados)),
  );

  @override
  Future<Ocorrencia> buscarPorProtocolo(String protocolo) async =>
      Ocorrencia.ler(
        await _client.get(
          '/ocorrencias/protocolo/${Uri.encodeComponent(protocolo.trim())}',
        ),
      );
}

/// Corpo de `CriarOcorrenciaRequest`. Opcionais nulos não vão no corpo.
Map<String, Object?> corpoDoRelato(DadosOcorrencia dados) => {
  'tipoOcorrencia': dados.tipo.codigo,
  'dataOcorrencia': dados.dataOcorrencia,
  'descricao': dados.descricao,
  'areaId': dados.areaId,
  'colaboradorId': dados.colaboradorId,
  'causaRaiz': ?dados.causaRaiz?.codigo,
  'gravidade': ?dados.gravidade?.codigo,
  'tipo': ?dados.tipoLesao,
  'dano': ?dados.dano,
  'destino': ?dados.destino,
  'situacaoRisco': ?dados.situacaoRisco,
  'potencialDano': ?dados.potencialDano,
};

final ocorrenciaRepositoryProvider = Provider<OcorrenciaRepository>(
  (ref) => OcorrenciasApi(ref.read(apiClientProvider)),
);

/// Mensagens das falhas do módulo, as mesmas do web. Recusas de regra trazem o motivo no `message`.
final descreverFalhaDeOcorrencia = criarDescritorDeFalha(
  porStatus: {
    400: usarMensagemDaApi(
      'Os dados informados foram recusados pelo servidor. Revise os campos e tente novamente.',
    ),
    403: fixa('Seu perfil não tem permissão para esta operação.'),
    404: usarMensagemDaApi(
      'Ocorrência não encontrada. Recarregue a lista e tente novamente.',
    ),
  },
  padrao: 'Não foi possível concluir a operação agora. Os dados preenchidos foram mantidos. Verifique sua conexão e tente novamente.',
);

// Leituras com cópia local por conta (RNF08). `autoDispose`: ao sair da tela (ou da conta), a
// consulta é descartada e a próxima abertura busca de novo.

/// Lista com os critérios da consulta. Cada combinação de critérios tem a sua cópia offline.
final ocorrenciasProvider = FutureProvider.autoDispose
    .family<Consultado<List<Ocorrencia>>, FiltroOcorrencias>(
      (ref, filtro) => consultarComCache(
        ref,
        chave: filtro.chaveCache,
        buscar: (api) => api.get(
          '/ocorrencias',
          consulta: filtro.vazio ? null : filtro.consulta,
        ),
        ler: lerOcorrencias,
      ),
      retry: (_, _) => null,
    );

/// Detalhe de uma ocorrência (`GET /api/ocorrencias/{id}`).
final ocorrenciaProvider = FutureProvider.autoDispose
    .family<Consultado<Ocorrencia>, int>(
      (ref, id) => consultarComCache(
        ref,
        chave: 'ocorrencias/$id',
        buscar: (api) => api.get('/ocorrencias/$id'),
        ler: Ocorrencia.ler,
      ),
      retry: (_, _) => null,
    );

/// Mensagens da busca por protocolo. 404 quer dizer que nenhum acidente tem esse número.
final descreverFalhaDeBusca = criarDescritorDeFalha(
  porStatus: {
    404: fixa(
      'Nenhum acidente tem esse protocolo. Confira o número e tente novamente.',
    ),
  },
  padrao: 'Não foi possível buscar o protocolo agora. Tente novamente.',
);
