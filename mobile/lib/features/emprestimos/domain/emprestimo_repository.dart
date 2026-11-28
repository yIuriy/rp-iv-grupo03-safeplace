// Contrato de operações de escrita do módulo de empréstimos de EPIs (RF11, UC12, US11, issue #320).
// Leituras ficam nos providers com cache offline de `data/emprestimos_api.dart`.

import '../data/emprestimo.dart';

/// Dados para registro de entrega de EPI a colaborador (`RegistrarEntregaRequest`).
class DadosEntrega {
  const DadosEntrega({
    required this.epiId,
    required this.colaboradorId,
    required this.dataDevolucaoPrevista,
    this.confirmarSubstituicao = false,
    this.justificativaSubstituicao,
  });

  final int epiId;
  final int colaboradorId;
  final DateTime dataDevolucaoPrevista;
  final bool confirmarSubstituicao;
  final String? justificativaSubstituicao;
}

/// Contrato de operações de entrega e devolução de EPIs.
abstract interface class EmprestimoRepository {
  /// Registra a entrega de um EPI ao colaborador (POST /api/emprestimos).
  Future<Emprestimo> registrarEntrega(DadosEntrega dados);

  /// Registra a devolução do EPI com seu estado de conservação (POST /api/emprestimos/{id}/devolucao).
  Future<Emprestimo> registrarDevolucao(
    int emprestimoId,
    EstadoDevolucao estado,
  );

  /// Consulta detalhes do empréstimo (GET /api/emprestimos/{id}).
  Future<Emprestimo> buscarPorId(int id);
}
