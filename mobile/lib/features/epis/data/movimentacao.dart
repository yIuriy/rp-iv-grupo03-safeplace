// Movimentação de estoque de um EPI (`MovimentacaoEstoqueResponse` de `/api/epis/{id}/movimentacoes`),
// portada de `frontend/src/features/estoque/api.ts`: entrada ou saída, com motivo e o saldo depois dela.

import '../../../core/api/leitura.dart';

/// `TipoMovimentacao` do backend.
enum TipoMovimentacao {
  entrada('ENTRADA', 'Entrada'),
  saida('SAIDA', 'Saída');

  const TipoMovimentacao(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  static TipoMovimentacao? ler(Object? valor) {
    for (final tipo in values) {
      if (tipo.codigo == valor) return tipo;
    }
    return null;
  }
}

/// Uma linha do histórico de estoque. A API devolve só o id de quem registrou ([responsavelId]);
/// a tela resolve o nome. Campo ausente ou de outro tipo vira `null`, e a tela mostra "Não informado".
class Movimentacao {
  const Movimentacao({
    this.id,
    this.epiId,
    this.responsavelId,
    this.tipo,
    this.quantidade,
    this.dataHora,
    this.motivo,
    this.saldoAtual,
  });

  final int? id;
  final int? epiId;
  final int? responsavelId;
  final TipoMovimentacao? tipo;
  final int? quantidade;
  final DateTime? dataHora;
  final String? motivo;

  /// Saldo do EPI logo depois desta movimentação.
  final int? saldoAtual;

  factory Movimentacao.ler(Object? dado) {
    final b = objeto(dado, 'Movimentação inválida');
    return Movimentacao(
      id: numeroOuNulo(b['id'])?.toInt(),
      epiId: numeroOuNulo(b['epiId'])?.toInt(),
      responsavelId: numeroOuNulo(b['responsavelId'])?.toInt(),
      tipo: TipoMovimentacao.ler(b['tipo']),
      quantidade: numeroOuNulo(b['quantidade'])?.toInt(),
      dataHora: dataOuNula(b['dataHora']),
      motivo: textoOuNulo(b['motivo']),
      saldoAtual: numeroOuNulo(b['saldoAtual'])?.toInt(),
    );
  }
}

/// Lista de `GET /api/epis/{id}/movimentacoes`.
List<Movimentacao> lerMovimentacoes(Object? dado) =>
    lista(dado, Movimentacao.ler, 'Lista de movimentações inválida');
