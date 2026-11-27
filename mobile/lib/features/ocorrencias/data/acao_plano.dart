// Ação do plano de ação de uma ocorrência (`PlanoDeAcaoResponse`, UC07, #321), portada de
// `frontend/src/features/ocorrencias/apiPlanos.ts`. Cada ação tem medida corretiva e/ou preventiva,
// prazo, um responsável (colaborador ou setor), situação ABERTA → CONCLUIDA e alerta com
// antecedência. "Em atraso" e "alerta devido" não são gravados: o servidor calcula a partir do prazo
// e da data de hoje (#196).

import '../../../core/api/leitura.dart';

enum SituacaoAcao {
  aberta('ABERTA', 'Aberta'),
  concluida('CONCLUIDA', 'Concluída');

  const SituacaoAcao(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  /// `null` para texto gravado antes do contrato, que não é convertido por suposição (decisão 2.8).
  static SituacaoAcao? ler(Object? valor) {
    final codigo = '${valor ?? ''}'.toUpperCase();
    for (final situacao in values) {
      if (situacao.codigo == codigo) return situacao;
    }
    return null;
  }
}

enum TipoResponsavel {
  colaborador('COLABORADOR', 'Colaborador'),
  setor('SETOR', 'Setor');

  const TipoResponsavel(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  static TipoResponsavel? ler(Object? valor) {
    final codigo = '${valor ?? ''}'.toUpperCase();
    for (final tipo in values) {
      if (tipo.codigo == codigo) return tipo;
    }
    return null;
  }
}

class AcaoPlano {
  const AcaoPlano({
    required this.id,
    this.idOcorrencia,
    this.medidasCorretivas,
    this.medidasPreventivas,
    required this.prazo,
    this.situacao,
    this.statusGravado = '',
    this.responsavelTipo,
    this.responsavelId,
    this.responsavelNome,
    this.alertaAtivo = false,
    this.antecedenciaAlertaDias,
    this.dataAlerta,
    this.alertaDevido = false,
    this.emAtraso = false,
    this.gestorNome,
  });

  final int id;
  final int? idOcorrencia;
  final String? medidasCorretivas;
  final String? medidasPreventivas;
  final DateTime prazo;
  final SituacaoAcao? situacao;

  /// Texto do status como veio, para mostrar quando a situação não é reconhecida.
  final String statusGravado;
  final TipoResponsavel? responsavelTipo;
  final int? responsavelId;
  final String? responsavelNome;
  final bool alertaAtivo;
  final int? antecedenciaAlertaDias;
  final DateTime? dataAlerta;

  /// Ação aberta que já chegou à antecedência do alerta, inclusive depois do prazo.
  final bool alertaDevido;

  /// Ação aberta com prazo anterior a hoje.
  final bool emAtraso;
  final String? gestorNome;

  /// Só a ação aberta recebe conclusão, prorrogação, reatribuição e alerta.
  bool get aberta => situacao == SituacaoAcao.aberta;

  /// Sem `id` ou sem prazo a ação não tem como ser alterada, então o contrato é recusado.
  factory AcaoPlano.ler(Object? dado) {
    final b = objeto(dado, 'Ação do plano inválida na resposta');
    final id = numeroOuNulo(b['id'])?.toInt();
    final prazo = dataOuNula(b['prazo']);
    if (id == null || prazo == null) {
      throw const FormatException(
        'Ação do plano incompatível com o contrato da API',
      );
    }
    return AcaoPlano(
      id: id,
      idOcorrencia: numeroOuNulo(b['idOcorrencia'])?.toInt(),
      medidasCorretivas: textoOuNulo(b['medidasCorretivas']),
      medidasPreventivas: textoOuNulo(b['medidasPreventivas']),
      prazo: prazo,
      situacao: SituacaoAcao.ler(b['situacao'] ?? b['status']),
      statusGravado: '${b['status'] ?? b['situacao'] ?? ''}',
      responsavelTipo: TipoResponsavel.ler(b['responsavelTipo']),
      responsavelId: numeroOuNulo(b['responsavelId'])?.toInt(),
      responsavelNome: textoOuNulo(b['responsavelNome']),
      alertaAtivo: b['alertaAtivo'] == true,
      antecedenciaAlertaDias: numeroOuNulo(b['antecedenciaAlertaDias'])
          ?.toInt(),
      dataAlerta: dataOuNula(b['dataAlerta']),
      alertaDevido: b['alertaDevido'] == true,
      emAtraso: b['emAtraso'] == true,
      gestorNome: textoOuNulo(b['gestorNome']),
    );
  }
}

/// Uma prorrogação registrada (`ProrrogacaoPrazoResponse`).
class Prorrogacao {
  const Prorrogacao({
    required this.idAcao,
    this.prazoAnterior,
    this.novoPrazo,
    required this.justificativa,
  });

  final int idAcao;
  final DateTime? prazoAnterior;
  final DateTime? novoPrazo;
  final String justificativa;

  factory Prorrogacao.ler(Object? dado) {
    final b = objeto(dado, 'Prorrogação inválida na resposta');
    return Prorrogacao(
      idAcao: numeroOuNulo(b['idAcao'])?.toInt() ?? 0,
      prazoAnterior: dataOuNula(b['prazoAnterior']),
      novoPrazo: dataOuNula(b['novoPrazo']),
      justificativa: texto(b['justificativa'], 'Sem justificativa registrada'),
    );
  }
}

List<AcaoPlano> lerAcoesDoPlano(Object? dado) =>
    lista(dado, AcaoPlano.ler, 'Esperava a lista de ações do plano');

List<Prorrogacao> lerProrrogacoes(Object? dado) =>
    lista(dado, Prorrogacao.ler, 'Esperava o histórico de prorrogações');
