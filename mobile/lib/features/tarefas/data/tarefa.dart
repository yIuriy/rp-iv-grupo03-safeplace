// Modelo de tarefa da matriz de periculosidade (`TarefaResponse` de `/api/tarefas`), portado de
// `frontend/src/features/tarefas/api.ts`. Lista, detalhe e as issues de capacitações (#325) e
// relatórios (#326) leem o mesmo registro pelo mesmo `Tarefa.ler`.

import '../../../core/api/leitura.dart';
import '../../areas_risco/data/area_risco.dart';

export '../../areas_risco/data/area_risco.dart' show NivelPerigo;

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
