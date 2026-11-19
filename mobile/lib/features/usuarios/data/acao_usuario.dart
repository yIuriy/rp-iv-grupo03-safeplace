import '../../../core/api/leitura.dart';

/// Operações gravadas na trilha de auditoria (RNF05). Valor desconhecido aparece como veio.
enum TipoOperacao {
  cadastro('CADASTRO', 'Cadastro'),
  alteracao('ALTERACAO', 'Alteração'),
  remocao('REMOCAO', 'Remoção');

  const TipoOperacao(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  static TipoOperacao? ler(Object? valor) {
    for (final tipo in values) {
      if (tipo.codigo == valor) return tipo;
    }
    return null;
  }
}

/// Uma ação da pessoa logada (`GET /api/usuarios/me/historico`), da mais recente para a mais antiga.
/// Leitura defensiva, como o `lerAcao` do web, mas campo ausente não derruba a lista.
class AcaoUsuario {
  const AcaoUsuario({
    this.id,
    required this.operacao,
    this.dataHora,
    this.entidade,
    this.entidadeId,
    this.descricao,
  });

  final int? id;

  /// Código como veio do backend (`CADASTRO`, `ALTERACAO`, `REMOCAO`).
  final String operacao;
  final DateTime? dataHora;

  /// Tipo do registro afetado, como gravado no log (`Incidente`, `Epi`...).
  final String? entidade;
  final int? entidadeId;
  final String? descricao;

  TipoOperacao? get tipo => TipoOperacao.ler(operacao);

  /// Rótulo da operação; código desconhecido aparece como veio.
  String get rotuloOperacao => tipo?.rotulo ?? operacao;

  /// "Supervisor 12", "Empréstimo de EPI 4", com os nomes do log em português.
  String get registro {
    final nome = entidade == null
        ? 'Registro'
        : (_rotuloEntidade[entidade] ?? entidade!);
    return entidadeId == null ? nome : '$nome $entidadeId';
  }

  factory AcaoUsuario.ler(Object? dado) {
    final b = objeto(dado, 'Ação inválida na resposta');
    return AcaoUsuario(
      id: numeroOuNulo(b['id'])?.toInt(),
      operacao: texto(b['tipoOperacao'], 'Operação não informada'),
      dataHora: dataOuNula(b['dataHora']),
      entidade: textoOuNulo(b['entidade']),
      entidadeId: numeroOuNulo(b['entidadeId'])?.toInt(),
      descricao: textoOuNulo(b['descricao']),
    );
  }
}

List<AcaoUsuario> lerAcoes(Object? dado) =>
    lista(dado, AcaoUsuario.ler, 'Resposta inválida do histórico');

/// Mesmos nomes do web (`apiHistorico.ts`).
const _rotuloEntidade = {
  'Acidente': 'Acidente',
  'AlocacaoTarefa': 'Alocação em tarefa',
  'AreaRisco': 'Área de risco',
  'Certificacao': 'Certificação',
  'Colaborador': 'Colaborador',
  'Emprestimo': 'Empréstimo de EPI',
  'Epi': 'EPI',
  'Incidente': 'Incidente',
  'Inspecao': 'Inspeção',
  'ManutencaoEpi': 'Manutenção de EPI',
  'PlanoDeAcao': 'Plano de ação',
  'Supervisor': 'Supervisor',
  'Tarefa': 'Tarefa',
  'Treinamento': 'Treinamento',
  'Usuario': 'Usuário',
};
