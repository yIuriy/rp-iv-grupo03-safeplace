// Classificação e reavaliação do grau de periculosidade (RF06, UC11), só do Gestor, como o
// `ClassificarTarefaDialog` do web. O Gestor logado fica registrado como responsável técnico.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';

Future<Tarefa?> abrirFormularioClassificacao(
  BuildContext context, {
  required Tarefa tarefa,
}) {
  return Navigator.of(context, rootNavigator: true).push<Tarefa>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioClassificacaoPage(tarefa: tarefa),
    ),
  );
}

final descreverFalhaDeClassificacao = criarDescritorDeFalha(
  porStatus: {
    403: usarMensagemDaApi(
      'Seu perfil não tem permissão para classificar. Apenas o Gestor de Segurança define o grau de periculosidade.',
    ),
  },
  padrao:
      'Não foi possível atualizar a classificação da tarefa. Tente novamente.',
);

String mensagemDeClassificacao(Tarefa salva) =>
    'Grau de periculosidade atualizado para ${salva.nivelPerigo?.rotulo ?? 'não informado'}.';

String? validarNivelPerigo(NivelPerigo? nivel) =>
    nivel == null ? 'Escolha o grau de periculosidade.' : null;

class FormularioClassificacaoPage extends ConsumerStatefulWidget {
  const FormularioClassificacaoPage({super.key, required this.tarefa});

  final Tarefa tarefa;

  @override
  ConsumerState<FormularioClassificacaoPage> createState() =>
      _FormularioClassificacaoPageState();
}

class _FormularioClassificacaoPageState
    extends ConsumerState<FormularioClassificacaoPage> {
  final _form = GlobalKey<FormState>();
  late NivelPerigo? _nivel = widget.tarefa.nivelPerigo;
  bool _enviando = false;
  String? _falha;

  bool get _alterado => _nivel != widget.tarefa.nivelPerigo;

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    final id = widget.tarefa.id!;
    setState(() => _enviando = true);
    try {
      final salva = await ref
          .read(tarefaRepositoryProvider)
          .classificar(id, _nivel!);
      if (!mounted) return;
      ref.invalidate(tarefasProvider);
      ref.invalidate(tarefaProvider(id));
      ref.invalidate(historicoTarefaProvider(id));
      Navigator.of(context).pop<Tarefa>(salva);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeClassificacao(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final tarefa = widget.tarefa;
    final textos = Theme.of(context).textTheme;
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            tarefa.classificada
                ? 'Reavaliar periculosidade'
                : 'Classificar tarefa',
          ),
        ),
        body: SafeArea(
          child: Form(
            key: _form,
            child: ListView(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              children: [
                Text(
                  'Atribua o grau de periculosidade da atividade operacional conforme a avaliação técnica de segurança.',
                  style: textos.bodyMedium?.copyWith(
                    color: context.cores.textoSecundario,
                  ),
                ),
                const SizedBox(height: TokensDimensoes.space16),
                MergeSemantics(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Descrição da atividade',
                        style: textos.labelMedium?.copyWith(
                          color: context.cores.textoSecundario,
                        ),
                      ),
                      Text(tarefa.descricao, style: textos.bodyLarge),
                    ],
                  ),
                ),
                const SizedBox(height: TokensDimensoes.space24),
                CampoSelecao<NivelPerigo>(
                  rotulo: 'Grau de periculosidade',
                  valor: _nivel,
                  ajuda: 'Mesma escala das áreas de risco. Você ficará registrado como responsável técnico.',
                  habilitado: !_enviando,
                  validador: validarNivelPerigo,
                  opcoes: [
                    for (final nivel in NivelPerigo.values)
                      (valor: nivel, rotulo: nivel.rotulo),
                  ],
                  aoMudar: (nivel) => setState(() => _nivel = nivel),
                ),
                if (_falha != null) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  Aviso(
                    tom: TomAviso.perigo,
                    anunciar: true,
                    titulo: 'Não foi possível classificar',
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
                  label: const Text('Salvar classificação'),
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
