// Contrato das escritas do plano de ação (UC07, #321). As leituras (ações e prorrogações) ficam nos
// providers com cache offline de `data/planos_api.dart`. Todas as escritas são do Gestor de
// Segurança; o Supervisor só acompanha.

import '../data/acao_plano.dart';

/// Nova ação já validada, no formato de `CriarPlanoDeAcaoRequest`: exatamente um responsável
/// (colaborador ou setor) e a antecedência só com o alerta ativo.
class NovaAcao {
  const NovaAcao({
    this.medidasCorretivas,
    this.medidasPreventivas,
    required this.prazo,
    required this.responsavelTipo,
    required this.responsavelId,
    this.alertaAtivo = false,
    this.antecedenciaAlertaDias,
  });

  final String? medidasCorretivas;
  final String? medidasPreventivas;
  final DateTime prazo;
  final TipoResponsavel responsavelTipo;
  final int responsavelId;
  final bool alertaAtivo;
  final int? antecedenciaAlertaDias;
}

abstract interface class PlanoRepository {
  /// `POST /api/ocorrencias/{idOcorrencia}/planos-acao`.
  Future<AcaoPlano> cadastrar(int idOcorrencia, NovaAcao acao);

  /// `PATCH /api/planos-acao/{id}/status` com `CONCLUIDA`. Não tem volta.
  Future<AcaoPlano> concluir(int idAcao);

  /// `POST /api/planos-acao/{id}/prorrogacoes`, com justificativa técnica obrigatória.
  Future<Prorrogacao> prorrogar(
    int idAcao,
    DateTime novoPrazo,
    String justificativa,
  );

  /// `PATCH /api/planos-acao/{id}/responsavel`: um colaborador ou um setor.
  Future<AcaoPlano> reatribuir(
    int idAcao,
    TipoResponsavel tipo,
    int responsavelId,
  );

  /// `PATCH /api/planos-acao/{id}/alerta`. Desativado, a antecedência não vai.
  Future<AcaoPlano> configurarAlerta(
    int idAcao, {
    required bool ativo,
    int? antecedenciaDias,
  });
}
