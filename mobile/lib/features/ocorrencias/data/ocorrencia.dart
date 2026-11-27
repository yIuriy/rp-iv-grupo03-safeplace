// Modelo de ocorrência no app (`OcorrenciaResponse` de `/api/ocorrencias`), portado de
// `frontend/src/features/ocorrencias/api.ts` e `formatos.ts` (#318). Lista, detalhe e busca por
// protocolo leem o mesmo registro pelo mesmo `Ocorrencia.ler`.

import '../../../core/api/leitura.dart';
import '../../../core/sessao/sessao.dart';
import '../../areas_risco/data/area_risco.dart';

enum TipoOcorrencia {
  acidente('ACIDENTE', 'Acidente'),
  incidente('INCIDENTE', 'Incidente');

  const TipoOcorrencia(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  /// Como no web, só "ACIDENTE" vira acidente. Qualquer outro valor é lido como incidente.
  static TipoOcorrencia ler(Object? valor) =>
      '$valor'.toUpperCase() == 'ACIDENTE' ? acidente : incidente;
}

/// Situação do relato (decisão DS09, seção 2.6): Aberta, Em triagem e Arquivada.
enum StatusOcorrencia {
  aberta('ABERTA', 'Aberta'),
  emTriagem('EM_TRIAGEM', 'Em triagem'),
  arquivada('ARQUIVADA', 'Arquivada');

  const StatusOcorrencia(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  /// Aceita "em triagem" com espaço. Valor desconhecido vira Aberta, como no web.
  static StatusOcorrencia ler(Object? valor) {
    if (valor is String) {
      final codigo = valor.toUpperCase().replaceAll(RegExp(r'\s+'), '_');
      for (final status in values) {
        if (status.codigo == codigo) return status;
      }
    }
    return aberta;
  }
}

/// Causa do acidente (`CausaRaiz` do backend).
enum CausaRaiz {
  fatorHumano('FATOR_HUMANO', 'Fator humano'),
  falhaEpi('FALHA_EPI', 'Falha de EPI'),
  outro('OUTRO', 'Outra causa');

  const CausaRaiz(this.codigo, this.rotulo);

  final String codigo;
  final String rotulo;

  /// Causa pelo código da API, ou `null` se desconhecida (#321, edição do relato).
  static CausaRaiz? ler(Object? valor) {
    for (final causa in values) {
      if (causa.codigo == valor) return causa;
    }
    return null;
  }
}

/// Rótulo da causa. Valor desconhecido aparece como veio.
String descreverCausaRaiz(String? valor) {
  if (valor == null) return 'Não informada';
  for (final causa in CausaRaiz.values) {
    if (causa.codigo == valor) return causa.rotulo;
  }
  return valor;
}

/// Rótulo da gravidade, que usa o vocabulário `NivelPerigo`. Valor desconhecido aparece como veio.
String descreverGravidade(String? valor) {
  if (valor == null) return 'Não informada';
  return NivelPerigo.ler(valor)?.rotulo ?? valor;
}

/// Ocorrência registrada. Leitura defensiva: campo ausente ou de outro tipo vira `null`, e a tela
/// mostra "Não informado". Sem [id], a ocorrência aparece na lista mas não abre o detalhe.
class Ocorrencia {
  const Ocorrencia({
    this.id,
    required this.tipo,
    this.dataOcorrencia,
    this.dataRegistro,
    required this.status,
    required this.descricao,
    this.local,
    this.protocoloCat,
    this.gravidade,
    this.causaRaiz,
    this.tipoLesao,
    this.dano,
    this.destino,
    this.situacaoRisco,
    this.potencialDano,
    this.areaId,
    this.areaNome,
    this.colaboradorId,
    this.colaboradorNome,
    this.relatorId,
    this.relatorNome,
    this.relatorPerfil,
  });

  final int? id;
  final TipoOcorrencia tipo;

  /// Data e hora do fato.
  final DateTime? dataOcorrencia;

  /// Quando o relato entrou no sistema, definido pelo servidor.
  final DateTime? dataRegistro;
  final StatusOcorrencia status;
  final String descricao;

  /// Nome da área ou o texto de registros antigos.
  final String? local;

  /// Número da CAT (Comunicação de Acidente de Trabalho). Só acidentes têm.
  final String? protocoloCat;
  final String? gravidade;
  final String? causaRaiz;

  /// Campo `tipo` da API: o tipo de lesão do acidente.
  final String? tipoLesao;
  final String? dano;
  final String? destino;
  final String? situacaoRisco;
  final String? potencialDano;
  final int? areaId;
  final String? areaNome;
  final int? colaboradorId;
  final String? colaboradorNome;
  final int? relatorId;
  final String? relatorNome;
  final Perfil? relatorPerfil;

  bool get ehAcidente => tipo == TipoOcorrencia.acidente;

  factory Ocorrencia.ler(Object? dado) {
    final b = objeto(dado, 'Ocorrência inválida na resposta');
    return Ocorrencia(
      id: numeroOuNulo(b['idOcorrencia'])?.toInt(),
      tipo: TipoOcorrencia.ler(b['tipoOcorrencia']),
      dataOcorrencia: dataOuNula(b['dataOcorrencia']),
      dataRegistro: dataOuNula(b['dataRegistro']),
      status: StatusOcorrencia.ler(b['statusOcorrencia']),
      descricao: texto(b['descricao'], 'Sem descrição'),
      local: textoOuNulo(b['local']),
      protocoloCat: textoOuNulo(b['numeroProtocoloCAT']),
      gravidade: textoOuNulo(b['gravidade']),
      causaRaiz: textoOuNulo(b['causaRaiz']),
      tipoLesao: textoOuNulo(b['tipo']),
      dano: textoOuNulo(b['dano']),
      destino: textoOuNulo(b['destino']),
      situacaoRisco: textoOuNulo(b['situacaoRisco']),
      potencialDano: textoOuNulo(b['potencialDano']),
      areaId: numeroOuNulo(b['areaId'])?.toInt(),
      areaNome: textoOuNulo(b['areaNome']),
      colaboradorId: numeroOuNulo(b['colaboradorId'])?.toInt(),
      colaboradorNome: textoOuNulo(b['colaboradorNome']),
      relatorId: numeroOuNulo(b['relatorId'])?.toInt(),
      relatorNome: textoOuNulo(b['relatorNome']),
      relatorPerfil: Perfil.ler(b['relatorPerfil']),
    );
  }

  /// Área do fato: o nome da área ou, em registros antigos, o texto do local.
  String get ondeOcorreu => areaNome ?? local ?? 'Não informado';

  /// Quem registrou, com o perfil entre parênteses quando conhecido.
  String get relator {
    final nome = relatorNome ?? 'Não informado';
    final perfil = relatorPerfil;
    return perfil == null ? nome : '$nome (${perfil.rotulo})';
  }
}

List<Ocorrencia> lerOcorrencias(Object? dado) =>
    lista(dado, Ocorrencia.ler, 'Esperava uma lista de ocorrências');
