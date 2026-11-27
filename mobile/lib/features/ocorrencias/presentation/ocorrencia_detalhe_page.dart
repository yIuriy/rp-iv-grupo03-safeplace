// Tela `/ocorrencias/{id}` (UC05, UC09, #318): dados do relato, com os campos próprios de acidente
// ou de incidente. Gestor e Supervisor consultam. Com o servidor fora do ar, mostra a cópia local
// com o aviso de modo offline (RNF08). A seta de voltar fica na barra da casca (#335).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/estados.dart';
import '../data/ocorrencia.dart';
import '../data/ocorrencias_api.dart';
import 'componentes_ocorrencia.dart';

String _ouNaoInformado(String? valor) => valor ?? 'Não informado';

/// Rótulo e valor de cada dado do relato, na ordem do detalhe do web.
List<(String, String)> dadosDaOcorrencia(Ocorrencia ocorrencia) => [
  ('Data do fato', formatarDataHora(ocorrencia.dataOcorrencia)),
  ('Registrada em', formatarDataHora(ocorrencia.dataRegistro)),
  ('Área de risco', ocorrencia.ondeOcorreu),
  ('Colaborador envolvido', _ouNaoInformado(ocorrencia.colaboradorNome)),
  ('Registrada por', ocorrencia.relator),
  ('Descrição', ocorrencia.descricao),
  if (ocorrencia.ehAcidente) ...[
    ('Protocolo CAT', _ouNaoInformado(ocorrencia.protocoloCat)),
    ('Causa', descreverCausaRaiz(ocorrencia.causaRaiz)),
    ('Gravidade', descreverGravidade(ocorrencia.gravidade)),
    ('Tipo de lesão', _ouNaoInformado(ocorrencia.tipoLesao)),
    ('Dano', _ouNaoInformado(ocorrencia.dano)),
    ('Destino do acidentado', _ouNaoInformado(ocorrencia.destino)),
  ] else ...[
    ('Situação de risco', _ouNaoInformado(ocorrencia.situacaoRisco)),
    ('Potencial de dano', _ouNaoInformado(ocorrencia.potencialDano)),
  ],
];

class OcorrenciaDetalhePage extends ConsumerWidget {
  const OcorrenciaDetalhePage({super.key, required this.id});

  /// Nulo quando o endereço não traz um número (`/ocorrencias/abc`).
  final int? id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = this.id;
    final lista = ListView(
      padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
      children: [
        if (id == null)
          const EstadoVazio(
            titulo: 'Ocorrência não encontrada',
            descricao: 'O endereço não indica uma ocorrência. Volte para a lista e escolha um relato.',
          )
        else
          ConteudoComCache<Ocorrencia>(
            valor: ref.watch(ocorrenciaProvider(id)),
            carregando: 'Carregando ocorrência...',
            falha: 'Não foi possível carregar a ocorrência.',
            descreverFalha: descreverFalhaDeOcorrencia,
            aoTentarDeNovo: () => ref.invalidate(ocorrenciaProvider(id)),
            construir: (ocorrencia, _) =>
                _DadosDaOcorrencia(ocorrencia: ocorrencia),
          ),
      ],
    );
    if (id == null) return lista;
    return RefreshIndicator(
      onRefresh: () => ref
          .refresh(ocorrenciaProvider(id).future)
          .then((_) {}, onError: (_) {}),
      child: lista,
    );
  }
}

class _DadosDaOcorrencia extends StatelessWidget {
  const _DadosDaOcorrencia({required this.ocorrencia});

  final Ocorrencia ocorrencia;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final rotulo = textos.labelMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Semantics(
          header: true,
          child: Text(ocorrencia.tipo.rotulo, style: textos.headlineSmall),
        ),
        const SizedBox(height: TokensDimensoes.space8),
        EtiquetaStatusOcorrencia(status: ocorrencia.status),
        for (final (nome, valor) in dadosDaOcorrencia(ocorrencia))
          Padding(
            padding: const EdgeInsets.only(top: TokensDimensoes.space8),
            child: MergeSemantics(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(nome, style: rotulo),
                  Text(valor, style: textos.bodyLarge),
                ],
              ),
            ),
          ),
      ],
    );
  }
}
