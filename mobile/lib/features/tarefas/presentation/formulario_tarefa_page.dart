// Formulário de cadastro e edição de tarefa (UC11), só do Gestor: descrição, área de risco opcional,
// setores físicos e, no cadastro, o grau de periculosidade opcional. Quando o servidor recusa, a
// mensagem dele aparece e os dados continuam preenchidos, como o `TarefaFormDialog` do web. Fechar com
// dados digitados pede confirmação (#335).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../../areas_risco/data/area_risco.dart';
import '../../areas_risco/data/areas_risco_api.dart';
import '../../areas_risco/presentation/area_risco_picker.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';
import '../domain/tarefa_repository.dart';
import '../domain/validacao_tarefa.dart';

/// Abre o formulário em tela cheia. Sem [tarefa], cadastra; com ela, edita. Devolve a tarefa salva,
/// ou `null` se a pessoa desistiu.
Future<Tarefa?> abrirFormularioTarefa(BuildContext context, {Tarefa? tarefa}) {
  return Navigator.of(context, rootNavigator: true).push<Tarefa>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioTarefaPage(tarefa: tarefa),
    ),
  );
}

final descreverFalhaDeTarefa = criarDescritorDeFalha(
  porStatus: {
    403: fixa(
      'Seu perfil não tem permissão para salvar tarefas. Apenas o Gestor de Segurança cadastra e edita.',
    ),
    404: usarMensagemDaApi(
      'Tarefa, área ou setor não encontrado. Volte e recarregue a lista.',
    ),
  },
  padrao: 'Não foi possível salvar a tarefa agora. Os dados preenchidos foram mantidos. Tente novamente.',
);

/// Aviso da tela de origem depois de salvar, o mesmo texto do web.
String mensagemAoSalvarTarefa(Tarefa salva, {required bool editando}) =>
    editando
    ? 'Tarefa atualizada com sucesso: "${salva.descricao}".'
    : 'Tarefa cadastrada com sucesso: "${salva.descricao}".';

class FormularioTarefaPage extends ConsumerStatefulWidget {
  const FormularioTarefaPage({super.key, this.tarefa});

  final Tarefa? tarefa;

  @override
  ConsumerState<FormularioTarefaPage> createState() =>
      _FormularioTarefaPageState();
}

class _FormularioTarefaPageState extends ConsumerState<FormularioTarefaPage> {
  final _form = GlobalKey<FormState>();
  late final _descricao = TextEditingController(text: widget.tarefa?.descricao);
  late int? _areaId = widget.tarefa?.areaRiscoId;
  late final Set<int> _setores = {...?widget.tarefa?.setoresIds};
  NivelPerigo? _nivel;
  bool _enviando = false;
  bool _alterado = false;
  String? _falha;

  bool get _editando => widget.tarefa != null;

  @override
  void dispose() {
    _descricao.dispose();
    super.dispose();
  }

  void _marcarAlterado() {
    if (!_alterado) setState(() => _alterado = true);
  }

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    final dados = DadosTarefa(
      descricao: _descricao.text.trim(),
      areaRiscoId: _areaId,
      setoresIds: _setores.toList()..sort(),
      nivelPerigo: _editando ? null : _nivel,
    );
    final repositorio = ref.read(tarefaRepositoryProvider);
    try {
      final id = widget.tarefa?.id;
      final salva = id == null
          ? await repositorio.cadastrar(dados)
          : await repositorio.atualizar(id, dados);
      if (!mounted) return;
      ref.invalidate(tarefasProvider);
      if (id != null) ref.invalidate(tarefaProvider(id));
      Navigator.of(context).pop<Tarefa>(salva);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeTarefa(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final tarefa = widget.tarefa;
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            tarefa == null
                ? 'Cadastrar tarefa'
                : 'Editar tarefa nº ${tarefa.id}',
          ),
        ),
        body: SafeArea(
          child: Form(
            key: _form,
            onChanged: _marcarAlterado,
            child: ListView(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              children: [
                Text(
                  _editando
                      ? 'Atualize a descrição, área de risco e setores físicos vinculados à tarefa operacional.'
                      : 'Informe a descrição da atividade operacional, área de risco e setores físicos.',
                  style: Theme.of(context).textTheme.bodyMedium
                      ?.copyWith(color: context.cores.textoSecundario),
                ),
                const SizedBox(height: TokensDimensoes.space24),
                CampoTexto(
                  rotulo: 'Descrição da atividade operacional',
                  controlador: _descricao,
                  linhas: 3,
                  dica: 'Ex.: solda em altura na estrutura metálica do galpão',
                  validador: validarDescricaoTarefa,
                ),
                const SizedBox(height: TokensDimensoes.space16),
                AreaRiscoPicker(
                  areaId: _areaId,
                  rotulo: 'Área de risco (opcional)',
                  ajuda: 'Vincule a tarefa a uma área de risco delimitada, se aplicável.',
                  habilitado: !_enviando,
                  aoMudar: (area) {
                    _marcarAlterado();
                    setState(() => _areaId = area?.id);
                  },
                ),
                const SizedBox(height: TokensDimensoes.space16),
                _SeletorDeSetores(
                  selecionados: _setores,
                  habilitado: !_enviando,
                  aoAlternar: (id) {
                    _marcarAlterado();
                    setState(
                      () => _setores.contains(id)
                          ? _setores.remove(id)
                          : _setores.add(id),
                    );
                  },
                ),
                if (!_editando) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  CampoSelecao<NivelPerigo?>(
                    rotulo: 'Grau de periculosidade (opcional)',
                    valor: _nivel,
                    ajuda: 'Escolha só se a atividade já foi classificada tecnicamente. Você ficará registrado como responsável técnico.',
                    habilitado: !_enviando,
                    opcoes: [
                      (valor: null, rotulo: 'Não classificada no momento'),
                      for (final nivel in NivelPerigo.values)
                        (valor: nivel, rotulo: nivel.rotulo),
                    ],
                    aoMudar: (nivel) => setState(() => _nivel = nivel),
                  ),
                ],
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
                  label: Text(
                    _editando ? 'Salvar alterações' : 'Cadastrar tarefa',
                  ),
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
      ),
    );
  }
}

/// Setores físicos como chips de múltipla escolha. Os setores já vinculados que não vierem no
/// catálogo continuam marcados, para a edição não perder o vínculo sem a pessoa ver.
class _SeletorDeSetores extends ConsumerWidget {
  const _SeletorDeSetores({
    required this.selecionados,
    required this.habilitado,
    required this.aoAlternar,
  });

  final Set<int> selecionados;
  final bool habilitado;
  final ValueChanged<int> aoAlternar;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final valor = ref.watch(setoresProvider);
    final textos = Theme.of(context).textTheme;
    final secundario = textos.bodyMedium?.copyWith(
      color: context.cores.textoSecundario,
    );
    final setores = [
      for (final setor in valor.value?.dados ?? const <Setor>[])
        if (setor.id != null) setor,
    ];
    final conhecidos = {for (final s in setores) s.id};
    final Widget conteudo;
    if (valor.isLoading && !valor.hasValue) {
      conteudo = Text('Carregando setores físicos...', style: secundario);
    } else if (valor.hasError && !valor.hasValue) {
      conteudo = Row(
        children: [
          Expanded(
            child: Text(
              'Não foi possível carregar os setores.',
              style: secundario,
            ),
          ),
          TextButton(
            onPressed: () => ref.invalidate(setoresProvider),
            child: const Text('Tentar novamente'),
          ),
        ],
      );
    } else if (setores.isEmpty && selecionados.isEmpty) {
      conteudo = Text('Nenhum setor físico cadastrado.', style: secundario);
    } else {
      conteudo = Wrap(
        spacing: TokensDimensoes.space8,
        runSpacing: TokensDimensoes.space4,
        children: [
          for (final setor in setores)
            FilterChip(
              label: Text(setor.descricaoCurta),
              selected: selecionados.contains(setor.id),
              onSelected: habilitado ? (_) => aoAlternar(setor.id!) : null,
            ),
          for (final id in selecionados)
            if (!conhecidos.contains(id))
              FilterChip(
                label: Text('Setor nº $id'),
                selected: true,
                onSelected: habilitado ? (_) => aoAlternar(id) : null,
              ),
        ],
      );
    }
    return Semantics(
      container: true,
      label: 'Setores físicos (opcional)',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ExcludeSemantics(
            child: Text('Setores físicos (opcional)', style: textos.labelLarge),
          ),
          const SizedBox(height: TokensDimensoes.space8),
          conteudo,
        ],
      ),
    );
  }
}
