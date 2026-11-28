// Peças visuais do módulo de tarefas, usadas na lista e no detalhe: etiqueta do grau de perigo e o
// cartão de uma tarefa. O grau aparece em texto e cor juntos, porque a cor sozinha não carrega a
// informação (RNF11).

import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../data/tarefa.dart';

/// Mensagem das consultas do módulo quando o servidor responde com erro.
final descreverFalhaDeConsultaTarefa = criarDescritorDeFalha(
  porStatus: {
    403: fixa(
      'Seu perfil não tem permissão para esta consulta. Ela é exclusiva do Gestor de Segurança.',
    ),
    404: fixa(
      'Tarefa não encontrada. Ela pode ter sido removida. Volte e recarregue a lista.',
    ),
  },
  padrao: 'Verifique sua conexão ou tente novamente em alguns instantes.',
);

/// "Última avaliação" da tarefa, como a coluna do web.
String descreverAvaliacao(Tarefa tarefa) {
  final data = tarefa.dataClassificacao;
  if (!tarefa.classificada || data == null) return 'Pendente de avaliação';
  final responsavel =
      tarefa.responsavelClassificacaoNome ?? 'responsável não registrado';
  return '${formatarDataHora(data)} por $responsavel';
}

/// Grau de periculosidade, ou "Não classificada" quando a tarefa ainda não foi avaliada.
class EtiquetaNivelPerigo extends StatelessWidget {
  const EtiquetaNivelPerigo({super.key, required this.nivel});

  final NivelPerigo? nivel;

  @override
  Widget build(BuildContext context) {
    final cores = context.cores;
    final (frente, fundo) = switch (nivel) {
      NivelPerigo.baixo => (cores.sucesso, cores.sucessoFundo),
      NivelPerigo.medio => (cores.info, cores.infoFundo),
      NivelPerigo.alto => (cores.alerta, cores.alertaFundo),
      NivelPerigo.critico => (cores.perigo, cores.perigoFundo),
      null => (cores.textoSecundario, cores.fundoDesabilitado),
    };
    return Semantics(
      label: nivel == null
          ? 'Grau de perigo: não classificada'
          : 'Grau de perigo: ${nivel!.rotulo}',
      excludeSemantics: true,
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
          child: Text(
            nivel?.rotulo ?? 'Não classificada',
            style: Theme.of(context).textTheme.labelMedium
                ?.copyWith(color: frente),
          ),
        ),
      ),
    );
  }
}

/// Uma tarefa da lista: descrição, grau, última avaliação e quantos EPIs exige. [aoTocar] nulo deixa
/// o cartão só para leitura.
class CartaoTarefa extends StatelessWidget {
  const CartaoTarefa({super.key, required this.tarefa, this.aoTocar});

  final Tarefa tarefa;
  final VoidCallback? aoTocar;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final secundario = textos.bodyMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    final epis = tarefa.episIds.length;
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
                child: Text(tarefa.descricao, style: textos.titleMedium),
              ),
              const SizedBox(height: TokensDimensoes.space8),
              EtiquetaNivelPerigo(nivel: tarefa.nivelPerigo),
              const SizedBox(height: TokensDimensoes.space8),
              Text(descreverAvaliacao(tarefa), style: secundario),
              if (epis > 0)
                Text(
                  epis == 1 ? '1 EPI obrigatório' : '$epis EPIs obrigatórios',
                  style: secundario,
                ),
            ],
          ),
        ),
      ),
    );
  }
}
