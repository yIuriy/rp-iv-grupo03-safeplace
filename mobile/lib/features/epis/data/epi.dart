// Modelo único de EPI no app (`EpiResponse` de `/api/epis`), portado de `frontend/src/features/epis/epi.ts`.
// Lista, detalhe, cadastro, empréstimos, áreas de risco, tarefas e capacitações leem o mesmo registro
// pelo mesmo `Epi.ler`, com as primitivas de `core/api/leitura.dart`.

import '../../../core/api/leitura.dart';

/// Situação do EPI (`StatusEpi` do backend).
enum StatusEpi {
  disponivel('DISPONIVEL', 'Disponível'),
  emUso('EM_USO', 'Em uso'),
  emManutencao('EM_MANUTENCAO', 'Em manutenção'),
  esgotado('ESGOTADO', 'Esgotado'),
  descartado('DESCARTADO', 'Descartado');

  const StatusEpi(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  /// Valor fora do enum vira `null`, e a tela mostra "Não informado".
  static StatusEpi? ler(Object? valor) {
    for (final status in values) {
      if (status.codigo == valor) return status;
    }
    return null;
  }
}

/// Os nove valores de `ClassificacaoEPI` do diagrama de classes.
enum ClassificacaoEpi {
  protecaoContraQuedas('PROTECAO_CONTRA_QUEDAS', 'Proteção contra quedas'),
  protecaoDeMembrosInferiores(
    'PROTECAO_DE_MEMBROS_INFERIORES',
    'Proteção de membros inferiores',
  ),
  protecaoDeMembrosSuperiores(
    'PROTECAO_DE_MEMBROS_SUPERIORES',
    'Proteção de membros superiores',
  ),
  protecaoDeTronco('PROTECAO_DE_TRONCO', 'Proteção de tronco'),
  protecaoRespiratoria('PROTECAO_RESPIRATORIA', 'Proteção respiratória'),
  protecaoAuditiva('PROTECAO_AUDITIVA', 'Proteção auditiva'),
  protecaoDeOlhos('PROTECAO_DE_OLHOS', 'Proteção de olhos'),
  protecaoDeFace('PROTECAO_DE_FACE', 'Proteção de face'),
  protecaoDeCabeca('PROTECAO_DE_CABECA', 'Proteção de cabeça');

  const ClassificacaoEpi(this.codigo, this.rotulo);

  /// Valor usado pela API.
  final String codigo;
  final String rotulo;

  /// Valor fora dos nove vira `null`, para a tela nunca mostrar um rótulo inexistente.
  static ClassificacaoEpi? ler(Object? valor) {
    for (final classificacao in values) {
      if (classificacao.codigo == valor) return classificacao;
    }
    return null;
  }
}

/// EPI cadastrado, com saldo, situação e os dados de lote e modelo que o detalhe mostra.
///
/// Leitura defensiva, como a de `Usuario`: campo ausente ou de outro tipo vira `null` (ou a reserva
/// do nome), e a tela mostra "Não informado". Sem [id] o EPI aparece na lista, mas não abre o
/// detalhe, não recebe movimentação e não pode ser escolhido no `EpiPicker`.
class Epi {
  const Epi({
    this.id,
    required this.nome,
    this.numeroCa,
    this.quantidade,
    this.estoqueMinimo,
    this.estoqueCritico = false,
    this.status,
    this.dataValidadeCa,
    this.vidaUtilDias,
    this.descricao,
    this.classificacao,
    this.localizacao,
    this.numeroLote,
    this.notaFiscal,
    this.dataFabricacao,
    this.validadeLote,
    this.marca,
  });

  final int? id;
  final String nome;

  /// Número do CA (Certificado de Aprovação).
  final String? numeroCa;

  /// Saldo atual em estoque.
  final int? quantidade;
  final int? estoqueMinimo;

  /// Regra do backend (saldo menor ou igual ao mínimo, US03 regra 3). A tela exibe e não recalcula.
  final bool estoqueCritico;
  final StatusEpi? status;
  final DateTime? dataValidadeCa;
  final int? vidaUtilDias;
  final String? descricao;
  final ClassificacaoEpi? classificacao;
  final String? localizacao;
  final String? numeroLote;
  final String? notaFiscal;
  final DateTime? dataFabricacao;
  final DateTime? validadeLote;
  final String? marca;

  factory Epi.ler(Object? dado) {
    final b = objeto(dado, 'Registro de EPI inválido');
    return Epi(
      id: numeroOuNulo(b['id'])?.toInt(),
      nome: texto(b['nome'], 'Nome não informado'),
      numeroCa: textoOuNulo(b['numeroCa']),
      quantidade: numeroOuNulo(b['quantidade'])?.toInt(),
      estoqueMinimo: numeroOuNulo(b['estoqueMinimo'])?.toInt(),
      // Como no web: só `true` explícito é crítico.
      estoqueCritico: b['estoqueCritico'] == true,
      status: StatusEpi.ler(b['status']),
      dataValidadeCa: dataOuNula(b['dataValidadeCa']),
      vidaUtilDias: numeroOuNulo(b['vidaUtilDias'])?.toInt(),
      descricao: textoOuNulo(b['descricao']),
      classificacao: ClassificacaoEpi.ler(b['classificacao']),
      localizacao: textoOuNulo(b['localizacao']),
      numeroLote: textoOuNulo(b['numeroLote']),
      notaFiscal: textoOuNulo(b['notaFiscal']),
      dataFabricacao: dataOuNula(b['dataFabricacao']),
      validadeLote: dataOuNula(b['validadeLote']),
      marca: textoOuNulo(b['marca']),
    );
  }

  /// O mesmo EPI: pelo [id] quando há, senão por nome e CA. O `SeletorComBusca` usa isto para
  /// marcar a opção escolhida mesmo depois de a lista ser recarregada.
  @override
  bool operator ==(Object other) =>
      other is Epi &&
      (id != null
          ? other.id == id
          : other.id == null &&
                other.nome == nome &&
                other.numeroCa == numeroCa);

  @override
  int get hashCode => id != null ? id.hashCode : Object.hash(nome, numeroCa);

  @override
  String toString() => 'Epi($id, $nome)';
}

/// Lista de `GET /api/epis` (e de `GET /api/emprestimos/epis-disponiveis`, mesmo contrato).
List<Epi> lerEpis(Object? dado) =>
    lista(dado, Epi.ler, 'Lista de EPIs inválida');
