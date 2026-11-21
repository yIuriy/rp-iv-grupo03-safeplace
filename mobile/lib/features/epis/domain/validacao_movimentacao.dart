// Validação local do formulário de movimentação de estoque (US03), portada de
// `frontend/src/features/estoque/validacaoMovimentacao.ts` com as mesmas mensagens: quantidade inteira
// maior que zero, saída limitada ao saldo conhecido e motivo obrigatório. A regra definitiva (saldo
// nunca negativo) é do backend. Aqui só se evita uma chamada que já se sabe inválida.

import '../data/movimentacao.dart';

const _quantidadeInvalida = 'Informe uma quantidade inteira maior que zero.';

/// Na saída, a quantidade também fica limitada ao [saldo] conhecido. Sem saldo conhecido, quem
/// decide é o backend.
String? validarQuantidadeMovimentacao(
  String? valor, {
  required TipoMovimentacao tipo,
  required int? saldo,
}) {
  final quantidade = int.tryParse((valor ?? '').trim());
  if (quantidade == null || quantidade <= 0) return _quantidadeInvalida;
  if (tipo == TipoMovimentacao.saida && saldo != null && quantidade > saldo) {
    return 'A saída não pode superar o saldo disponível ($saldo).';
  }
  return null;
}

String? validarMotivo(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Informe o motivo da movimentação.' : null;
