// Contratos do módulo de ocorrências (UC05, UC09): critérios da consulta, dados do relato e as
// chamadas que não são leituras em cache (registro e busca por protocolo). As leituras ficam nos
// providers com cache offline de `data/ocorrencias_api.dart`.

import '../../../shared/formularios/data.dart';
import '../../areas_risco/data/area_risco.dart';
import '../data/ocorrencia.dart';

/// Critérios de `GET /api/ocorrencias` (issues #128 e #195). O período usa a data do fato e inclui
/// os dois dias. O setor é o da área de risco. A gravidade só existe em acidentes, então esse
/// critério deixa os incidentes de fora. O Supervisor recebe só os próprios relatos (o backend
/// filtra).
class FiltroOcorrencias {
  const FiltroOcorrencias({
    this.dataInicio,
    this.dataFim,
    this.setorId,
    this.colaboradorId,
    this.tipo,
    this.gravidade,
  });

  static const nenhum = FiltroOcorrencias();

  final DateTime? dataInicio;
  final DateTime? dataFim;
  final int? setorId;
  final int? colaboradorId;
  final TipoOcorrencia? tipo;
  final NivelPerigo? gravidade;

  Map<String, String> get consulta => {
    if (dataInicio case final inicio?) 'dataInicio': dataIso(inicio),
    if (dataFim case final fim?) 'dataFim': dataIso(fim),
    if (setorId case final setor?) 'setorId': '$setor',
    if (colaboradorId case final colaborador?) 'colaboradorId': '$colaborador',
    if (tipo case final tipo?) 'tipoOcorrencia': tipo.codigo,
    if (gravidade case final gravidade?) 'gravidade': gravidade.codigo,
  };

  bool get vazio => consulta.isEmpty;

  /// Chave do cache offline: cada combinação de critérios guarda a sua cópia.
  String get chaveCache => vazio
      ? 'ocorrencias'
      : 'ocorrencias?${Uri(queryParameters: consulta).query}';

  @override
  bool operator ==(Object other) =>
      other is FiltroOcorrencias && other.chaveCache == chaveCache;

  @override
  int get hashCode => chaveCache.hashCode;
}

/// Relato já validado, no formato de `CriarOcorrenciaRequest`. Data do registro, protocolo CAT e
/// relator são definidos pelo servidor. Os campos de acidente e os de incidente são exclusivos.
class DadosOcorrencia {
  const DadosOcorrencia({
    required this.tipo,
    required this.dataOcorrencia,
    required this.descricao,
    required this.areaId,
    required this.colaboradorId,
    this.causaRaiz,
    this.gravidade,
    this.tipoLesao,
    this.dano,
    this.destino,
    this.situacaoRisco,
    this.potencialDano,
  });

  final TipoOcorrencia tipo;

  /// `aaaa-mm-ddThh:mm:00`, montado a partir do texto digitado, sem passar por `DateTime`.
  final String dataOcorrencia;
  final String descricao;
  final int areaId;
  final int colaboradorId;
  final CausaRaiz? causaRaiz;
  final NivelPerigo? gravidade;
  final String? tipoLesao;
  final String? dano;
  final String? destino;
  final String? situacaoRisco;
  final String? potencialDano;
}

/// Chamadas fora do cache. Lança a falha da API para a tela mostrar a mensagem do backend.
abstract interface class OcorrenciaRepository {
  /// `POST /api/ocorrencias`, para Gestor e Supervisor.
  Future<Ocorrencia> registrar(DadosOcorrencia dados);

  /// `GET /api/ocorrencias/protocolo/{protocolo}`. Só acidentes têm protocolo (o número da CAT).
  Future<Ocorrencia> buscarPorProtocolo(String protocolo);
}
