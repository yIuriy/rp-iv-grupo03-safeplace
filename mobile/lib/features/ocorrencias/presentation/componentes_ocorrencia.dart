// Peças visuais do módulo de ocorrências, usadas na lista e no detalhe: etiqueta da situação, data e
// hora no formato da tela e o cartão de uma ocorrência. A situação aparece em texto e cor juntos,
// porque a cor sozinha não carrega a informação (RNF11).

import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../data/ocorrencia.dart';

String _dois(int valor) => valor.toString().padLeft(2, '0');

/// `dd/mm/aaaa às hh:mm`, ou "Não informada".
String formatarDataHora(DateTime? momento) {
  if (momento == null) return 'Não informada';
  return '${formatarData(momento)} às ${_dois(momento.hour)}:${_dois(momento.minute)}';
}

/// Situação do relato: aberta aguarda triagem, em triagem está em análise, arquivada encerrou.
class EtiquetaStatusOcorrencia extends StatelessWidget {
  const EtiquetaStatusOcorrencia({super.key, required this.status});

  final StatusOcorrencia status;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final (frente, fundo) = switch (status) {
      StatusOcorrencia.emTriagem => (cores.alerta, cores.alertaFundo),
      StatusOcorrencia.arquivada => (cores.sucesso, cores.sucessoFundo),
      StatusOcorrencia.aberta => (cores.info, cores.infoFundo),
    };
    return DecoratedBox(
      decoration: BoxDecoration(
        color: fundo,
        borderRadius: BorderRadius.circular(TokensDimensoes.radius16),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: TokensDimensoes.space8,
          vertical: TokensDimensoes.space4,
        ),
        child: Text(
          status.rotulo,
          style: Theme.of(context).textTheme.labelMedium
              ?.copyWith(color: frente),
        ),
      ),
    );
  }
}

/// Uma ocorrência da lista: tipo, data do fato, área, colaborador, protocolo e situação. [aoTocar]
/// nulo deixa o cartão só para leitura.
class CartaoOcorrencia extends StatelessWidget {
  const CartaoOcorrencia({super.key, required this.ocorrencia, this.aoTocar});

  final Ocorrencia ocorrencia;
  final VoidCallback? aoTocar;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final secundario = textos.bodyMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    return Card(
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: aoTocar,
        child: Padding(
          padding: const EdgeInsets.all(TokensDimensoes.space16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Semantics(
                header: true,
                child: Text(
                  '${ocorrencia.tipo.rotulo} em ${formatarDataHora(ocorrencia.dataOcorrencia)}',
                  style: textos.titleMedium,
                ),
              ),
              Text(ocorrencia.ondeOcorreu, style: secundario),
              Text(
                'Envolvido: ${ocorrencia.colaboradorNome ?? 'não informado'}',
                style: secundario,
              ),
              if (ocorrencia.protocoloCat case final protocolo?)
                Text('Protocolo CAT $protocolo', style: secundario),
              const SizedBox(height: TokensDimensoes.space8),
              Text(
                ocorrencia.descricao,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: textos.bodyLarge,
              ),
              const SizedBox(height: TokensDimensoes.space8),
              EtiquetaStatusOcorrencia(status: ocorrencia.status),
            ],
          ),
        ),
      ),
    );
  }
}
