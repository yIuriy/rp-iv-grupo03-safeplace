// Modelo de tarefa da matriz de periculosidade (`TarefaResponse` de `/api/tarefas`), portado de
// `frontend/src/features/tarefas/api.ts`. Lista, detalhe e as issues de capacitações (#325) e
// relatórios (#326) leem o mesmo registro pelo mesmo `Tarefa.ler`.

import '../../../core/api/leitura.dart';
import '../../areas_risco/data/area_risco.dart';

export '../../areas_risco/data/area_risco.dart' show EpiResumo, NivelPerigo;

/// Atividade operacional e a última classificação de periculosidade. Leitura defensiva: sem [id], a
/// tarefa aparece na lista mas não abre o detalhe.
class Tarefa {
  const Tarefa({
    this.id,
    required this.descricao,
    this.nivelPerigo,
    this.classificada = false,
    this.dataClassificacao,
    this.responsavelClassificacaoNome,
    this.areaRiscoId,
    this.setoresIds = const [],
    this.episIds = const [],
  });

  final int? id;
  final String descricao;
  final NivelPerigo? nivelPerigo;

  /// Tarefa sem classificação não aceita alocação (409).
  final bool classificada;
  final DateTime? dataClassificacao;

  /// Gestor responsável técnico pela última classificação; nulo nos registros anteriores a #112.
  final String? responsavelClassificacaoNome;
  final int? areaRiscoId;
  final List<int> setoresIds;
  final List<int> episIds;

  factory Tarefa.ler(Object? dado) {
    final b = objeto(dado, 'Tarefa inválida');
    final nivel = NivelPerigo.ler(b['nivelPerigo']);
    return Tarefa(
      id: numeroOuNulo(b['id'])?.toInt(),
      descricao: texto(b['descricao'], 'Tarefa sem descrição'),
      nivelPerigo: nivel,
      // Sem o campo, vale o grau: uma tarefa com grau já foi classificada.
      classificada: b['classificada'] is bool
          ? b['classificada']! as bool
          : nivel != null,
      dataClassificacao: dataOuNula(b['dataClassificacao']),
      responsavelClassificacaoNome: textoOuNulo(
        b['responsavelClassificacaoNome'],
      ),
      areaRiscoId: numeroOuNulo(b['areaRiscoId'])?.toInt(),
      setoresIds: _ids(b['setoresIds']),
      episIds: _ids(b['episIds']),
    );
  }
}

List<int> _ids(Object? dado) => [
  for (final id in listaOuVazia(dado, numeroOuNulo)) ?id?.toInt(),
];

List<Tarefa> lerTarefas(Object? dado) =>
    lista(dado, Tarefa.ler, 'Esperava uma lista de tarefas');

/// Entrada do histórico da tarefa (`RegistroHistoricoResponse`): cadastro e cada classificação, da
/// mais recente para a mais antiga.
class RegistroHistorico {
  const RegistroHistorico({
    this.id,
    this.tipoOperacao,
    this.dataHora,
    this.responsavel,
    this.descricao,
  });

  final int? id;
  final String? tipoOperacao;
  final DateTime? dataHora;

  /// E-mail de quem fez a operação.
  final String? responsavel;
  final String? descricao;

  factory RegistroHistorico.ler(Object? dado) {
    final b = objeto(dado, 'Registro de histórico inválido');
    return RegistroHistorico(
      id: numeroOuNulo(b['id'])?.toInt(),
      tipoOperacao: textoOuNulo(b['tipoOperacao']),
      dataHora: dataOuNula(b['dataHora']),
      responsavel: textoOuNulo(b['responsavel']),
      descricao: textoOuNulo(b['descricao']),
    );
  }
}

List<RegistroHistorico> lerHistorico(Object? dado) =>
    lista(dado, RegistroHistorico.ler, 'Esperava uma lista de registros');

enum TipoCapacitacao {
  treinamento('TREINAMENTO', 'Treinamento'),
  certificacao('CERTIFICACAO', 'Certificação');

  const TipoCapacitacao(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  /// Como o web: o que não for certificação conta como treinamento.
  static TipoCapacitacao ler(Object? valor) =>
      valor == certificacao.codigo ? certificacao : treinamento;
}

/// Capacitação normativa exigida pela tarefa.
class ExigenciaCapacitacao {
  const ExigenciaCapacitacao({this.id, required this.nome, required this.tipo});

  final int? id;
  final String nome;
  final TipoCapacitacao tipo;

  factory ExigenciaCapacitacao.ler(Object? dado) {
    final b = objeto(dado, 'Exigência de capacitação inválida');
    return ExigenciaCapacitacao(
      id: numeroOuNulo(b['id'])?.toInt(),
      nome: texto(b['nomeCapacitacao'], 'Capacitação sem nome'),
      tipo: TipoCapacitacao.ler(b['tipoCapacitacao']),
    );
  }
}

/// O que a tarefa exige (`ExigenciasTarefaResponse`, US06): grau, EPIs e capacitações.
class ExigenciasTarefa {
  const ExigenciasTarefa({
    this.nivelPerigo,
    this.classificada = false,
    this.capacitacoes = const [],
    this.episObrigatorios = const [],
  });

  final NivelPerigo? nivelPerigo;
  final bool classificada;
  final List<ExigenciaCapacitacao> capacitacoes;
  final List<EpiResumo> episObrigatorios;

  factory ExigenciasTarefa.ler(Object? dado) {
    final b = objeto(dado, 'Exigências da tarefa inválidas');
    final nivel = NivelPerigo.ler(b['nivelPerigo']);
    return ExigenciasTarefa(
      nivelPerigo: nivel,
      classificada: b['classificada'] is bool
          ? b['classificada']! as bool
          : nivel != null,
      capacitacoes: listaOuVazia(b['capacitacoes'], ExigenciaCapacitacao.ler),
      episObrigatorios: listaOuVazia(b['episObrigatorios'], EpiResumo.ler),
    );
  }
}

List<EpiResumo> lerEpisDaTarefa(Object? dado) =>
    lista(dado, EpiResumo.ler, 'Esperava uma lista de EPIs');
