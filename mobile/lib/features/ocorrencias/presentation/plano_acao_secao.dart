// Plano de ação no detalhe da ocorrência (UC07; UC05, alternativo I, passo 9; #321): resumo das
// ações abertas, em atraso e com alerta, e um cartão por ação. Atraso e alerta são calculados pelo
// servidor. O Gestor cadastra ações e, numa ação aberta, conclui, prorroga, reatribui e configura o
// alerta; o Supervisor só acompanha. Sem conexão, o plano aparece da cópia local e nada se altera.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/dialogo_confirmacao.dart';
import '../../../shared/widgets/estados.dart';
import '../data/acao_plano.dart';
import '../data/ocorrencia.dart';
import '../data/planos_api.dart';
import '../domain/estado_ocorrencia.dart';
import 'formularios_plano.dart';

/// Abre um formulário e, se ele devolver uma mensagem de sucesso, mostra o aviso.
Future<void> _abrirEAvisar(
  BuildContext context,
  Future<String?> Function() abrir,
) async {
  final mensagem = await abrir();
  if (!context.mounted) return;
  _avisar(context, mensagem);
}

void _avisar(BuildContext context, String? mensagem) {
  if (mensagem == null || !context.mounted) return;
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(content: Text(mensagem)));
}

class PlanoDeAcaoSecao extends ConsumerWidget {
  const PlanoDeAcaoSecao({
    super.key,
    required this.ocorrencia,
    required this.gestor,
    required this.somenteLeitura,
  });

  final Ocorrencia ocorrencia;
  final bool gestor;

  /// A ocorrência veio da cópia local: nada se altera sem conexão.
  final bool somenteLeitura;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = ocorrencia.id;
    if (id == null) return const SizedBox.shrink();
    final textos = Theme.of(context).textTheme;
    final consulta = ref.watch(acoesDoPlanoProvider(id));
    final estado = EstadoOcorrencia.de(ocorrencia.status);
    final offline = somenteLeitura || (consulta.value?.somenteLeitura ?? false);
    // O Gestor escreve com conexão e com a ocorrência em acompanhamento (arquivada não muda).
    final podeAlterar = gestor && !offline && estado.permiteNovasAcoes;

    Future<void> novaAcao() =>
        _abrirEAvisar(context, () => abrirNovaAcao(context, ocorrencia));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Semantics(
          header: true,
          child: Text('Plano de ação', style: textos.titleLarge),
        ),
        const SizedBox(height: TokensDimensoes.space4),
        Text(
          gestor
              ? 'Medidas corretivas e preventivas, com prazo e responsável.'
              : 'Acompanhe as medidas definidas pelo Gestor de Segurança.',
          style: textos.bodyMedium?.copyWith(
            color: context.cores.textoSecundario,
          ),
        ),
        const SizedBox(height: TokensDimensoes.space12),
        consulta.when(
          skipLoadingOnReload: true,
          skipLoadingOnRefresh: true,
          loading: () => const Padding(
            padding: EdgeInsets.symmetric(vertical: TokensDimensoes.space16),
            child: EstadoCarregando(texto: 'Carregando plano de ação...'),
          ),
          error: (erro, _) => Aviso(
            tom: TomAviso.perigo,
            titulo: 'Não foi possível carregar o plano de ação.',
            mensagem: descreverFalhaDoPlano(erro),
            rotuloAcao: 'Tentar novamente',
            aoAgir: () => ref.invalidate(acoesDoPlanoProvider(id)),
          ),
          data: (consultado) {
            final acoes = consultado.dados;
            return Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (acoes.isEmpty)
                  EstadoVazio(
                    icone: Icons.playlist_add_check,
                    titulo: 'Nenhuma ação no plano',
                    descricao: gestor
                        ? 'Cadastre a primeira medida corretiva ou preventiva desta ocorrência.'
                        : 'O Gestor de Segurança ainda não definiu ações para esta ocorrência.',
                  )
                else ...[
                  _Resumo(acoes: acoes),
                  const SizedBox(height: TokensDimensoes.space12),
                  for (final acao in acoes) ...[
                    CartaoAcaoPlano(acao: acao, podeAlterar: podeAlterar),
                    const SizedBox(height: TokensDimensoes.space12),
                  ],
                ],
                if (gestor && estado.permiteNovasAcoes) ...[
                  const SizedBox(height: TokensDimensoes.space8),
                  FilledButton.icon(
                    onPressed: podeAlterar ? novaAcao : null,
                    icon: const Icon(Icons.add),
                    label: const Text('Nova ação'),
                  ),
                ],
                if (gestor && offline)
                  Padding(
                    padding: const EdgeInsets.only(top: TokensDimensoes.space8),
                    child: Text(
                      'Alterações no plano indisponíveis sem conexão.',
                      style: textos.bodyMedium?.copyWith(
                        color: context.cores.textoSecundario,
                      ),
                    ),
                  ),
              ],
            );
          },
        ),
      ],
    );
  }
}

/// "3 ações: 2 abertas, 1 em atraso, 1 com alerta de prazo".
class _Resumo extends StatelessWidget {
  const _Resumo({required this.acoes});

  final List<AcaoPlano> acoes;

  @override
  Widget build(BuildContext context) {
    final abertas = acoes.where((a) => a.aberta).length;
    final atraso = acoes.where((a) => a.emAtraso).length;
    final alerta = acoes.where((a) => a.alertaDevido).length;
    String plural(int n, String um, String varios) =>
        n == 1 ? '1 $um' : '$n $varios';
    return Semantics(
      liveRegion: true,
      child: Text(
        '${plural(acoes.length, 'ação', 'ações')}: ${plural(abertas, 'aberta', 'abertas')}, '
        '$atraso em atraso, $alerta com alerta de prazo.',
        style: Theme.of(context).textTheme.bodyMedium,
      ),
    );
  }
}

/// Uma ação do plano: situação, medidas, prazo, responsável e alerta; para o Gestor, as operações.
class CartaoAcaoPlano extends ConsumerWidget {
  const CartaoAcaoPlano({
    super.key,
    required this.acao,
    required this.podeAlterar,
  });

  final AcaoPlano acao;
  final bool podeAlterar;

  Future<void> _concluir(BuildContext context, WidgetRef ref) async {
    final concluiu = await confirmar(
      context,
      titulo: 'Concluir a ação #${acao.id}?',
      descricao: 'Confirme que a medida foi executada. Uma ação concluída não volta a ficar aberta.',
      rotuloConfirmar: 'Confirmar conclusão',
      rotuloCancelar: 'Cancelar',
      perigosa: false,
      aoConfirmar: () => ref.read(planoRepositoryProvider).concluir(acao.id),
      descreverFalha: descreverFalhaDoPlano,
    );
    if (!concluiu || !context.mounted) return;
    recarregarPlano(ref, acao);
    _avisar(context, 'Ação #${acao.id} concluída.');
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textos = Theme.of(context).textTheme;
    final cores = context.cores;
    final rotulo = textos.labelMedium?.copyWith(color: cores.textoSecundario);

    Widget dado(String nome, String valor) => Padding(
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
    );

    final situacao = acao.situacao;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(TokensDimensoes.space16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Ação #${acao.id}', style: textos.titleMedium),
            const SizedBox(height: TokensDimensoes.space8),
            Wrap(
              spacing: TokensDimensoes.space8,
              runSpacing: TokensDimensoes.space4,
              children: [
                situacao == null
                    ? _Selo(
                        'Situação não reconhecida: ${acao.statusGravado.isEmpty ? 'vazia' : acao.statusGravado}',
                        cores.alerta,
                        cores.alertaFundo,
                      )
                    : situacao == SituacaoAcao.aberta
                    ? _Selo(situacao.rotulo, cores.info, cores.infoFundo)
                    : _Selo(situacao.rotulo, cores.sucesso, cores.sucessoFundo),
                if (acao.emAtraso)
                  _Selo('Em atraso', cores.perigo, cores.perigoFundo),
                if (acao.alertaDevido)
                  _Selo('Alerta de prazo', cores.alerta, cores.alertaFundo),
              ],
            ),
            if (acao.medidasCorretivas != null)
              dado('Medida corretiva', acao.medidasCorretivas!),
            if (acao.medidasPreventivas != null)
              dado('Medida preventiva', acao.medidasPreventivas!),
            dado('Prazo', formatarData(acao.prazo)),
            dado('Responsável', descreverResponsavel(acao)),
            dado(
              'Alerta',
              acao.alertaAtivo
                  ? '${acao.antecedenciaAlertaDias ?? 0} dia(s) antes'
                        '${acao.dataAlerta == null ? '' : ', a partir de ${formatarData(acao.dataAlerta!)}'}'
                  : 'Desativado',
            ),
            if (acao.aberta && (acao.emAtraso || acao.alertaDevido)) ...[
              const SizedBox(height: TokensDimensoes.space12),
              Aviso(
                tom: acao.emAtraso ? TomAviso.perigo : TomAviso.alerta,
                titulo: acao.emAtraso ? 'Prazo vencido' : 'Prazo próximo',
                mensagem: acao.emAtraso
                    ? 'A ação venceu em ${formatarData(acao.prazo)} e continua aberta. Avise o responsável ou prorrogue com justificativa.'
                    : 'O prazo vence em ${formatarData(acao.prazo)}. Avise o responsável${acao.responsavelNome == null ? '' : ' (${acao.responsavelNome})'}.',
              ),
            ],
            if (podeAlterar && acao.aberta) ...[
              const SizedBox(height: TokensDimensoes.space12),
              Wrap(
                spacing: TokensDimensoes.space8,
                runSpacing: TokensDimensoes.space8,
                children: [
                  FilledButton(
                    onPressed: () => _concluir(context, ref),
                    child: const Text('Concluir'),
                  ),
                  OutlinedButton(
                    onPressed: () => _abrirEAvisar(
                      context,
                      () => abrirProrrogacao(context, acao),
                    ),
                    child: const Text('Prorrogar prazo'),
                  ),
                  OutlinedButton(
                    onPressed: () => _abrirEAvisar(
                      context,
                      () => abrirReatribuicao(context, acao),
                    ),
                    child: const Text('Reatribuir'),
                  ),
                  OutlinedButton(
                    onPressed: () => _abrirEAvisar(
                      context,
                      () => abrirAlerta(context, acao),
                    ),
                    child: const Text('Alerta'),
                  ),
                ],
              ),
            ],
            _HistoricoDeProrrogacoes(acao: acao),
          ],
        ),
      ),
    );
  }
}

/// Histórico de prorrogações, carregado só quando a pessoa abre a seção.
class _HistoricoDeProrrogacoes extends ConsumerWidget {
  const _HistoricoDeProrrogacoes({required this.acao});

  final AcaoPlano acao;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Theme(
      data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
      child: ExpansionTile(
        tilePadding: EdgeInsets.zero,
        childrenPadding: EdgeInsets.zero,
        title: Text(
          'Histórico de prorrogações',
          style: Theme.of(context).textTheme.labelLarge,
        ),
        children: [_ListaDeProrrogacoes(acao: acao)],
      ),
    );
  }
}

class _ListaDeProrrogacoes extends ConsumerWidget {
  const _ListaDeProrrogacoes({required this.acao});

  final AcaoPlano acao;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textos = Theme.of(context).textTheme;
    return ref
        .watch(prorrogacoesProvider(acao.id))
        .when(
          loading: () => const Padding(
            padding: EdgeInsets.all(TokensDimensoes.space8),
            child: EstadoCarregando(texto: 'Carregando prorrogações...'),
          ),
          error: (erro, _) => Aviso(
            tom: TomAviso.perigo,
            titulo: 'Não foi possível carregar as prorrogações.',
            rotuloAcao: 'Tentar novamente',
            aoAgir: () => ref.invalidate(prorrogacoesProvider(acao.id)),
          ),
          data: (consultado) => consultado.dados.isEmpty
              ? Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'O prazo nunca foi prorrogado.',
                    style: textos.bodyMedium,
                  ),
                )
              : Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    for (final p in consultado.dados)
                      ListTile(
                        contentPadding: EdgeInsets.zero,
                        leading: const Icon(Icons.event_repeat),
                        title: Text(
                          '${p.prazoAnterior == null ? '-' : formatarData(p.prazoAnterior!)} → '
                          '${p.novoPrazo == null ? '-' : formatarData(p.novoPrazo!)}',
                        ),
                        subtitle: Text(p.justificativa),
                      ),
                  ],
                ),
        );
  }
}

/// Selo de situação, com texto (a cor sozinha nunca carrega a informação).
class _Selo extends StatelessWidget {
  const _Selo(this.texto, this.frente, this.fundo);

  final String texto;
  final Color frente;
  final Color fundo;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: fundo,
        borderRadius: BorderRadius.circular(TokensDimensoes.radius4),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: TokensDimensoes.space8,
          vertical: 2,
        ),
        child: Text(
          texto,
          style: Theme.of(context).textTheme.labelMedium
              ?.copyWith(color: frente),
        ),
      ),
    );
  }
}
