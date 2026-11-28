// Contrato de escrita do módulo de tarefas (RF06, UC11). As leituras ficam nos providers com cache
// offline de `data/tarefas_api.dart`.

import '../data/alocacao.dart';
import '../data/tarefa.dart';

/// Formulário de cadastro ou edição, já validado (`CriarTarefaRequest` e `AtualizarTarefaRequest`).
/// O grau só vai no cadastro: na edição, a classificação tem fluxo próprio.
class DadosTarefa {
  const DadosTarefa({
    required this.descricao,
    this.areaRiscoId,
    this.setoresIds = const [],
    this.nivelPerigo,
  });

  final String descricao;
  final int? areaRiscoId;
  final List<int> setoresIds;
  final NivelPerigo? nivelPerigo;
}

/// Escritas de `/api/tarefas`. Lança a falha da API para a tela mostrar a mensagem do backend.
abstract interface class TarefaRepository {
  /// Só o Gestor de Segurança.
  Future<Tarefa> cadastrar(DadosTarefa dados);

  /// Só o Gestor de Segurança.
  Future<Tarefa> atualizar(int id, DadosTarefa dados);

  /// Define ou reavalia o grau; o Gestor logado fica como responsável técnico.
  Future<Tarefa> classificar(int id, NivelPerigo nivel);

  /// Gestor e Supervisor. 409 quando a tarefa não tem grau; 422 com os `impedimentos` quando o
  /// colaborador não tem as capacitações exigidas.
  Future<AlocacaoTarefa> alocar(
    int tarefaId, {
    required int colaboradorId,
    String? observacao,
  });

  /// Consulta sem cache, porque o diagnóstico só vale com dados atuais.
  Future<ConsultaAptidao> consultarAptidao(int tarefaId, int colaboradorId);

  /// Substitui a lista de EPIs obrigatórios. Só o Gestor de Segurança.
  Future<Tarefa> vincularEpis(int id, List<int> epiIds);
}
