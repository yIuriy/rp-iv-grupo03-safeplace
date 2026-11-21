// Manutenção de um EPI (`ManutencaoEpiResponse` de `/api/epis/{id}/manutencoes`), portada de
// `frontend/src/features/epis/api.ts`: preventiva ou corretiva, com resultado APROVADO ou REPROVADO.
// A data fica como o texto da API e é formatada sem passar por `DateTime` (mesmo cuidado da #250).

import '../../../core/api/leitura.dart';
import 'epi.dart';

/// `TipoManutencao` do backend.
enum TipoManutencao {
  preventiva('PREVENTIVA', 'Preventiva'),
  corretiva('CORRETIVA', 'Corretiva');

  const TipoManutencao(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  static TipoManutencao? ler(Object? valor) {
    for (final tipo in values) {
      if (tipo.codigo == valor) return tipo;
    }
    return null;
  }
}

/// `ResultadoManutencao` do backend. Reprovado leva o EPI a descarte.
enum ResultadoManutencao {
  aprovado('APROVADO', 'Aprovado'),
  reprovado('REPROVADO', 'Reprovado');

  const ResultadoManutencao(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  static ResultadoManutencao? ler(Object? valor) {
    for (final resultado in values) {
      if (resultado.codigo == valor) return resultado;
    }
    return null;
  }
}

/// Uma linha do histórico de manutenção. Campo ausente ou de outro tipo vira `null`.
class Manutencao {
  const Manutencao({
    this.id,
    this.epiId,
    this.dataManutencao,
    this.tipo,
    this.descricao,
    this.resultado,
    this.responsavelId,
    this.statusAtualEpi,
  });

  final int? id;
  final int? epiId;

  /// `aaaa-mm-ddThh:mm[:ss]`, como a API devolve (`LocalDateTime`).
  final String? dataManutencao;
  final TipoManutencao? tipo;
  final String? descricao;
  final ResultadoManutencao? resultado;
  final int? responsavelId;

  /// Situação do EPI depois da manutenção.
  final StatusEpi? statusAtualEpi;

  factory Manutencao.ler(Object? dado) {
    final b = objeto(dado, 'Manutenção inválida');
    return Manutencao(
      id: numeroOuNulo(b['id'])?.toInt(),
      epiId: numeroOuNulo(b['epiId'])?.toInt(),
      dataManutencao: textoOuNulo(b['dataManutencao']),
      tipo: TipoManutencao.ler(b['tipoManutencao']),
      descricao: textoOuNulo(b['descricao']),
      resultado: ResultadoManutencao.ler(b['resultado']),
      responsavelId: numeroOuNulo(b['responsavelId'])?.toInt(),
      statusAtualEpi: StatusEpi.ler(b['statusAtualEpi']),
    );
  }
}

/// Lista de `GET /api/epis/{id}/manutencoes`.
List<Manutencao> lerManutencoes(Object? dado) =>
    lista(dado, Manutencao.ler, 'Lista de manutenções inválida');

final _dataHoraIso = RegExp(r'^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}))?');

/// `aaaa-mm-ddThh:mm[:ss]` para `dd/mm/aaaa hh:mm`, só recortando o texto, como o
/// `formatarDataHora` do web. Sem hora, só a data. Fora do formato, devolve `null`.
String? formatarDataHoraIso(String? iso) {
  final partes = _dataHoraIso.firstMatch(iso?.trim() ?? '');
  if (partes == null) return null;
  final data = '${partes[3]}/${partes[2]}/${partes[1]}';
  return partes[4] == null ? data : '$data ${partes[4]}:${partes[5]}';
}
