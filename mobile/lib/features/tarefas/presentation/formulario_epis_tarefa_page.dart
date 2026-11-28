// Vínculo dos EPIs obrigatórios da tarefa (RF12, UC08, US12), só do Gestor, como o
// `VincularEpisTarefaDialog` do web: catálogo com filtro por classificação, marcação múltipla e a
// lista inteira enviada de uma vez (substitui a anterior).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/estados.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../../epis/data/epi.dart';
import '../../epis/data/epis_api.dart';
import '../../epis/presentation/epi_picker.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';

Future<Tarefa?> abrirFormularioEpisTarefa(
  BuildContext context, {
  required Tarefa tarefa,
}) {
  return Navigator.of(context, rootNavigator: true).push<Tarefa>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioEpisTarefaPage(tarefa: tarefa),
    ),
  );
}

final descreverFalhaDeVinculoEpis = criarDescritorDeFalha(
  porStatus: {
    403: fixa(
      'Seu perfil não tem permissão para vincular EPIs. Apenas o Gestor de Segurança associa EPIs a tarefas.',
    ),
    404: usarMensagemDaApi(
      'Tarefa ou EPI não encontrado. Volte e recarregue a lista.',
    ),
  },
  padrao: 'Não foi possível associar os EPIs à tarefa. Tente novamente.',
);

String mensagemDeVinculoEpis(Tarefa salva) {
  final qtd = salva.episIds.length;
  return 'EPIs da tarefa atualizados com sucesso ($qtd vinculado${qtd == 1 ? '' : 's'}).';
}

/// Valor do filtro de classificação para os EPIs sem classificação.
const _semClassificacao = 'SEM_CLASSIFICACAO';

class FormularioEpisTarefaPage extends ConsumerStatefulWidget {
  const FormularioEpisTarefaPage({super.key, required this.tarefa});

  final Tarefa tarefa;

  @override
  ConsumerState<FormularioEpisTarefaPage> createState() =>
      _FormularioEpisTarefaPageState();
}

class _FormularioEpisTarefaPageState
    extends ConsumerState<FormularioEpisTarefaPage> {
  late final Set<int> _selecionados = {...widget.tarefa.episIds};
  String? _classificacao;
  bool _enviando = false;
  bool _alterado = false;
  String? _falha;

  @override
  void initState() {
    super.initState();
    // A tarefa da tela pode estar desatualizada: a lista vinculada atual vem do servidor e só
    // substitui a seleção se a pessoa ainda não mexeu nela.
    ref.listenManual(episDaTarefaProvider(widget.tarefa.id!), (_, valor) {
      final atuais = valor.value;
      if (atuais == null || _alterado || atuais.somenteLeitura) return;
      setState(
        () => _selecionados
          ..clear()
          ..addAll([for (final epi in atuais.dados) ?epi.id]),
      );
    }, fireImmediately: true);
  }

  bool _passaNoFiltro(Epi epi) => switch (_classificacao) {
    null => true,
    _semClassificacao => epi.classificacao == null,
    final codigo => epi.classificacao?.codigo == codigo,
  };

  void _alternar(int id) => setState(() {
    _alterado = true;
    _selecionados.contains(id)
        ? _selecionados.remove(id)
        : _selecionados.add(id);
  });

  Future<void> _enviar() async {
    final id = widget.tarefa.id!;
    setState(() {
      _enviando = true;
      _falha = null;
    });
    try {
      final salva = await ref
          .read(tarefaRepositoryProvider)
          .vincularEpis(id, _selecionados.toList()..sort());
      if (!mounted) return;
      ref.invalidate(tarefasProvider);
      ref.invalidate(tarefaProvider(id));
      ref.invalidate(episDaTarefaProvider(id));
      ref.invalidate(exigenciasProvider(id));
      Navigator.of(context).pop<Tarefa>(salva);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeVinculoEpis(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    final secundario = textos.bodyMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    final consulta = ref.watch(episProvider);
    final catalogo = [
      for (final epi in consulta.value?.dados ?? const <Epi>[])
        // Descartados saem do catálogo, mas os já vinculados continuam para poderem ser removidos.
        if (epi.id != null &&
            (EpiPicker.naoDescartado(epi) || _selecionados.contains(epi.id)))
          epi,
    ];
    final visiveis = catalogo.where(_passaNoFiltro).toList();
    final qtd = _selecionados.length;
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(title: const Text('Vincular EPIs obrigatórios')),
        body: SafeArea(
          child: ListView(
            padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
            children: [
              Text(
                'Selecione os EPIs necessários para realizar "${widget.tarefa.descricao}".',
                style: secundario,
              ),
              const SizedBox(height: TokensDimensoes.space16),
              CampoSelecao<String?>(
                rotulo: 'Filtrar por classificação do EPI',
                valor: _classificacao,
                habilitado: !_enviando,
                opcoes: [
                  (valor: null, rotulo: 'Todas as classificações'),
                  for (final c in ClassificacaoEpi.values)
                    (valor: c.codigo, rotulo: c.rotulo),
                  (
                    valor: _semClassificacao,
                    rotulo: 'Outros / sem classificação',
                  ),
                ],
                aoMudar: (valor) => setState(() => _classificacao = valor),
              ),
              const SizedBox(height: TokensDimensoes.space16),
              Semantics(
                liveRegion: true,
                child: Text(
                  qtd == 1
                      ? '1 EPI selecionado no total'
                      : '$qtd EPIs selecionados no total',
                  style: textos.titleSmall,
                ),
              ),
              const SizedBox(height: TokensDimensoes.space8),
              if (consulta.isLoading && !consulta.hasValue)
                const EstadoCarregando(texto: 'Carregando EPIs...')
              else if (consulta.hasError && !consulta.hasValue)
                Aviso(
                  tom: TomAviso.perigo,
                  titulo: 'Não foi possível carregar os EPIs.',
                  mensagem: descreverFalhaDeVinculoEpis(consulta.error!),
                  rotuloAcao: 'Tentar novamente',
                  aoAgir: () => ref.invalidate(episProvider),
                )
              else if (visiveis.isEmpty)
                EstadoVazio(
                  icone: Icons.inventory_2_outlined,
                  titulo: catalogo.isEmpty
                      ? 'Nenhum EPI disponível no catálogo.'
                      : 'Nenhum EPI encontrado para a classificação selecionada.',
                  descricao: catalogo.isEmpty
                      ? 'Cadastre EPIs no módulo de EPIs para vinculá-los.'
                      : 'Escolha outra classificação no filtro.',
                )
              else
                for (final epi in visiveis)
                  CheckboxListTile(
                    value: _selecionados.contains(epi.id),
                    onChanged: _enviando ? null : (_) => _alternar(epi.id!),
                    title: Text(epi.nome),
                    subtitle: Text(detalharEpi(epi)),
                    controlAffinity: ListTileControlAffinity.leading,
                    contentPadding: EdgeInsets.zero,
                  ),
              if (_falha != null) ...[
                const SizedBox(height: TokensDimensoes.space16),
                Aviso(
                  tom: TomAviso.perigo,
                  anunciar: true,
                  titulo: 'Não foi possível salvar',
                  mensagem: _falha,
                ),
              ],
              const SizedBox(height: TokensDimensoes.space24),
              FilledButton.icon(
                onPressed: _enviando ? null : _enviar,
                icon: _enviando
                    ? const SizedBox.square(
                        dimension: TokensDimensoes.sizeIcon,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.check),
                label: const Text('Salvar vínculos'),
              ),
              const SizedBox(height: TokensDimensoes.space8),
              TextButton(
                onPressed: _enviando
                    ? null
                    : () => Navigator.of(context).maybePop(),
                child: const Text('Cancelar'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
