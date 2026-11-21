// Peças visuais do módulo de EPIs, usadas na lista e no detalhe: etiqueta de status, selo de
// estoque crítico e o cartão de um EPI. A situação aparece em texto e cor juntos, porque a cor
// sozinha não carrega a informação (RNF11), como o `SituacaoEstoque` do web.

import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../data/epi.dart';

/// Mensagem das consultas do módulo quando o servidor responde com erro.
final descreverFalhaDeConsultaEpi = criarDescritorDeFalha(
  padrao: 'Verifique sua conexão ou tente novamente em alguns instantes.',
);

/// Número opcional como texto, com "não informado" no lugar do nulo.
String numeroOuNaoInformado(int? numero) =>
    numero?.toString() ?? 'não informado';

class _Etiqueta extends StatelessWidget {
  const _Etiqueta({
    required this.texto,
    required this.frente,
    required this.fundo,
    this.icone,
    this.rotuloLeitor,
  });

  final String texto;
  final Color frente;
  final Color fundo;
  final IconData? icone;
  final String? rotuloLeitor;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: rotuloLeitor,
      excludeSemantics: rotuloLeitor != null,
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: fundo,
          borderRadius: BorderRadius.circular(TokensDimensoes.radius16),
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: TokensDimensoes.space8,
            vertical: TokensDimensoes.space4,
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (icone != null) ...[
                Icon(icone, size: 16, color: frente),
                const SizedBox(width: TokensDimensoes.space4),
              ],
              Text(
                texto,
                style: Theme.of(context).textTheme.labelMedium
                    ?.copyWith(color: frente),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Status cadastral do EPI, com o mesmo tom de cada status do web.
class EtiquetaStatusEpi extends StatelessWidget {
  const EtiquetaStatusEpi({super.key, required this.status});

  final StatusEpi? status;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final (frente, fundo) = switch (status) {
      StatusEpi.disponivel => (cores.sucesso, cores.sucessoFundo),
      StatusEpi.emManutencao => (cores.alerta, cores.alertaFundo),
      StatusEpi.esgotado => (cores.perigo, cores.perigoFundo),
      _ => (cores.info, cores.infoFundo),
    };
    return _Etiqueta(
      texto: status?.rotulo ?? 'Status não informado',
      frente: frente,
      fundo: fundo,
    );
  }
}

/// Selo "Crítico" quando o backend marca saldo igual ou abaixo do mínimo (US03, regra 3). O leitor
/// de tela ouve também os dois números.
class EtiquetaEstoqueCritico extends StatelessWidget {
  const EtiquetaEstoqueCritico({super.key, required this.epi});

  final Epi epi;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    return _Etiqueta(
      texto: 'Crítico',
      icone: Icons.warning_amber_rounded,
      frente: cores.perigo,
      fundo: cores.perigoFundo,
      rotuloLeitor:
          'Estoque crítico: saldo ${numeroOuNaoInformado(epi.quantidade)} '
          'igual ou abaixo do mínimo ${numeroOuNaoInformado(epi.estoqueMinimo)}',
    );
  }
}

/// Um EPI da lista: nome, CA, localização, saldo, mínimo e situação. O EPI com estoque crítico
/// ganha borda vermelha e o selo "Crítico". [aoTocar] nulo deixa o cartão só para leitura.
class CartaoEpi extends StatelessWidget {
  const CartaoEpi({super.key, required this.epi, this.aoTocar});

  final Epi epi;
  final VoidCallback? aoTocar;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final textos = Theme.of(context).textTheme;
    final secundario = textos.bodyMedium?.copyWith(
      color: cores.textoSecundario,
    );
    return Card(
      margin: EdgeInsets.zero,
      clipBehavior: Clip.antiAlias,
      shape: epi.estoqueCritico
          ? RoundedRectangleBorder(
              side: BorderSide(color: cores.perigo, width: 2),
              borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
            )
          : null,
      child: InkWell(
        onTap: aoTocar,
        child: Padding(
          padding: const EdgeInsets.all(TokensDimensoes.space16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Semantics(
                header: true,
                child: Text(epi.nome, style: textos.titleMedium),
              ),
              Text('CA ${epi.numeroCa ?? 'não informado'}', style: secundario),
              Text(
                epi.localizacao ?? 'Localização não informada',
                style: secundario,
              ),
              const SizedBox(height: TokensDimensoes.space8),
              Text(
                'Saldo ${numeroOuNaoInformado(epi.quantidade)} · '
                'mínimo ${numeroOuNaoInformado(epi.estoqueMinimo)}',
                style: textos.bodyLarge,
              ),
              const SizedBox(height: TokensDimensoes.space8),
              Wrap(
                spacing: TokensDimensoes.space8,
                runSpacing: TokensDimensoes.space4,
                children: [
                  EtiquetaStatusEpi(status: epi.status),
                  if (epi.estoqueCritico) EtiquetaEstoqueCritico(epi: epi),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
