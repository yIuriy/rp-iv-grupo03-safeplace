// Contrato de escrita do módulo de EPIs (RF03, RF04, UC01, UC06): cadastro, movimentação de estoque e
// manutenção. As leituras ficam nos providers com cache offline de `data/epis_api.dart`, que as
// demais issues (empréstimos, projeções, áreas de risco, tarefas e capacitações) reutilizam.

import '../data/epi.dart';
import '../data/manutencao.dart';
import '../data/movimentacao.dart';

/// Formulário de cadastro, já validado (`CriarEpiRequest`). Opcionais nulos não vão no corpo.
class DadosCadastroEpi {
  const DadosCadastroEpi({
    required this.nome,
    required this.numeroCa,
    required this.quantidade,
    required this.estoqueMinimo,
    required this.dataValidadeCa,
    required this.classificacao,
    this.vidaUtilDias,
    this.descricao,
    this.localizacao,
    this.numeroLote,
    this.notaFiscal,
    this.dataFabricacao,
    this.validadeLote,
    this.marca,
  });

  final String nome;
  final String numeroCa;
  final int quantidade;
  final int estoqueMinimo;
  final DateTime dataValidadeCa;
  final ClassificacaoEpi classificacao;
  final int? vidaUtilDias;
  final String? descricao;
  final String? localizacao;
  final String? numeroLote;
  final String? notaFiscal;
  final DateTime? dataFabricacao;
  final DateTime? validadeLote;
  final String? marca;
}

/// Formulário de movimentação, já validado (`MovimentacaoEstoqueRequest`).
class DadosMovimentacao {
  const DadosMovimentacao({
    required this.tipo,
    required this.quantidade,
    required this.motivo,
  });

  final TipoMovimentacao tipo;
  final int quantidade;
  final String motivo;
}

/// Formulário de manutenção, já validado (`RegistrarManutencaoRequest`).
class DadosManutencao {
  const DadosManutencao({
    required this.dataManutencao,
    required this.descricao,
    required this.resultado,
    this.tipo,
  });

  /// `aaaa-mm-ddThh:mm`, montado a partir do texto digitado, sem passar por `DateTime`.
  final String dataManutencao;
  final String descricao;
  final ResultadoManutencao resultado;
  final TipoManutencao? tipo;
}

/// Escritas de `/api/epis`, todas exclusivas do Gestor de Segurança. Lança a falha da API para a
/// tela mostrar a mensagem do backend (por exemplo, saída maior que o saldo).
abstract interface class EpiRepository {
  Future<Epi> cadastrar(DadosCadastroEpi dados);

  /// Devolve a movimentação com o saldo atualizado.
  Future<Movimentacao> movimentar(int epiId, DadosMovimentacao dados);

  /// Devolve a manutenção com a nova situação do EPI.
  Future<Manutencao> registrarManutencao(int epiId, DadosManutencao dados);
}
