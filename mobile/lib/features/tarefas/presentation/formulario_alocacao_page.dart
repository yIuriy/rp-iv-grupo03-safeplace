// Alocação de colaborador em tarefa (RF10, UC02), para Gestor e Supervisor. Ao escolher o
// colaborador, a tela consulta a aptidão (só informativa) e, ao confirmar, o servidor valida tudo
// de novo: 409 quando a tarefa não tem grau e 422 com cada motivo do bloqueio, listados um a um.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/guarda_de_descarte.dart';
import '../../usuarios/presentation/colaborador_picker.dart';
import '../data/alocacao.dart';
import '../data/tarefa.dart';
import '../data/tarefas_api.dart';

Future<AlocacaoTarefa?> abrirFormularioAlocacao(
  BuildContext context, {
  required Tarefa tarefa,
}) {
  return Navigator.of(context, rootNavigator: true).push<AlocacaoTarefa>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioAlocacaoPage(tarefa: tarefa),
    ),
  );
}

const mensagemTarefaSemClassificacao =
    'Esta tarefa ainda não tem grau de periculosidade. O Gestor de Segurança precisa classificá-la antes de qualquer alocação.';

final descreverFalhaDeAlocacao = criarDescritorDeFalha(
  porStatus: {
    404: usarMensagemDaApi(
      'Tarefa ou colaborador não encontrado. Volte e recarregue a lista.',
    ),
    409: usarMensagemDaApi(mensagemTarefaSemClassificacao),
    422: usarMensagemDaApi(
      'O colaborador não tem as capacitações exigidas pela tarefa.',
    ),
  },
  padrao: 'Não foi possível confirmar a alocação agora. Os dados preenchidos foram mantidos. Tente novamente.',
);

String tituloDaFalhaDeAlocacao(Object erro) => switch (statusDoErro(erro)) {
  409 => 'Tarefa sem classificação',
  422 => 'Alocação bloqueada',
  _ => 'Alocação não confirmada',
};

String mensagemDeAlocacao(AlocacaoTarefa alocacao) =>
    '${alocacao.colaboradorNome} alocado(a) na tarefa.';

class FormularioAlocacaoPage extends ConsumerStatefulWidget {
  const FormularioAlocacaoPage({super.key, required this.tarefa});

  final Tarefa tarefa;

  @override
  ConsumerState<FormularioAlocacaoPage> createState() =>
      _FormularioAlocacaoPageState();
}

class _FormularioAlocacaoPageState
    extends ConsumerState<FormularioAlocacaoPage> {
  final _form = GlobalKey<FormState>();
  final _observacao = TextEditingController();
  int? _colaboradorId;
  bool _enviando = false;
  bool _alterado = false;
  Object? _falha;

  /// Consulta de aptidão do colaborador escolhido; nula antes da escolha.
  Future<ConsultaAptidao>? _aptidao;

  int get _tarefaId => widget.tarefa.id!;

  @override
  void dispose() {
    _observacao.dispose();
    super.dispose();
  }

  void _escolher(int? colaboradorId) {
    setState(() {
      _alterado = true;
      _falha = null;
      _colaboradorId = colaboradorId;
      _aptidao = colaboradorId == null
          ? null
          : ref
                .read(tarefaRepositoryProvider)
                .consultarAptidao(_tarefaId, colaboradorId);
    });
  }

  Future<void> _enviar() async {
    setState(() => _falha = null);
    if (!_form.currentState!.validate()) return;
    setState(() => _enviando = true);
    final observacao = _observacao.text.trim();
    try {
      final alocacao = await ref
          .read(tarefaRepositoryProvider)
          .alocar(
            _tarefaId,
            colaboradorId: _colaboradorId!,
            observacao: observacao.isEmpty ? null : observacao,
          );
      if (!mounted) return;
      ref.invalidate(alocacoesProvider(_tarefaId));
      Navigator.of(context).pop<AlocacaoTarefa>(alocacao);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = erro;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final tarefa = widget.tarefa;
    final textos = Theme.of(context).textTheme;
    final falha = _falha;
    return GuardaDeDescarte(
      alterado: _alterado,
      enviando: _enviando,
      child: Scaffold(
        appBar: AppBar(title: const Text('Alocar colaborador')),
        body: SafeArea(
          child: Form(
            key: _form,
            onChanged: () {
              if (!_alterado) setState(() => _alterado = true);
            },
            child: ListView(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              children: [
                Text(
                  '${tarefa.descricao}. Data, hora e responsável são registrados automaticamente.',
                  style: textos.bodyMedium?.copyWith(
                    color: context.cores.textoSecundario,
                  ),
                ),
                if (!tarefa.classificada) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  const Aviso(
                    tom: TomAviso.alerta,
                    titulo: 'Tarefa sem classificação',
                    mensagem: mensagemTarefaSemClassificacao,
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space24),
                ColaboradorPicker(
                  colaboradorId: _colaboradorId,
                  obrigatorio: true,
                  habilitado: !_enviando,
                  aoMudar: (colaborador) => _escolher(colaborador?.id),
                ),
                if (_aptidao case final aptidao?) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  _ResultadoDaAptidao(
                    // Recria o FutureBuilder a cada colaborador escolhido.
                    key: ValueKey(_colaboradorId),
                    consulta: aptidao,
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space16),
                CampoTexto(
                  rotulo: 'Observação (opcional)',
                  controlador: _observacao,
                  linhas: 3,
                  habilitado: !_enviando,
                ),
                if (falha != null) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  Aviso(
                    tom: TomAviso.perigo,
                    anunciar: true,
                    titulo: tituloDaFalhaDeAlocacao(falha),
                    mensagem: descreverFalhaDeAlocacao(falha),
                  ),
                  ..._listaDeMotivos(context, impedimentosDaFalha(falha)),
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
                  label: const Text('Confirmar alocação'),
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

/// Cada motivo numa linha própria, para o leitor de tela ler um de cada vez.
List<Widget> _listaDeMotivos(BuildContext context, List<String> motivos) => [
  for (final motivo in motivos)
    Padding(
      padding: const EdgeInsets.only(top: TokensDimensoes.space8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.block,
            size: TokensDimensoes.sizeIcon,
            color: context.cores.perigo,
          ),
          const SizedBox(width: TokensDimensoes.space8),
          Expanded(child: Text(motivo)),
        ],
      ),
    ),
];

class _ResultadoDaAptidao extends StatelessWidget {
  const _ResultadoDaAptidao({super.key, required this.consulta});

  final Future<ConsultaAptidao> consulta;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<ConsultaAptidao>(
      future: consulta,
      builder: (context, estado) {
        if (estado.connectionState != ConnectionState.done) {
          return const Text('Consultando aptidão do colaborador...');
        }
        final aptidao = estado.data;
        if (aptidao == null) {
          return Aviso(
            tom: TomAviso.info,
            titulo: 'Não foi possível consultar a aptidão',
            mensagem: descreverFalhaDeAlocacao(estado.error ?? 'erro'),
          );
        }
        if (aptidao.apto) {
          return const Aviso(
            tom: TomAviso.sucesso,
            anunciar: true,
            titulo: 'Colaborador apto',
            mensagem: 'Capacitações em dia para esta tarefa. A confirmação valida de novo.',
          );
        }
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Aviso(
              tom: TomAviso.alerta,
              anunciar: true,
              titulo: 'Colaborador com pendências',
              mensagem: 'A alocação será bloqueada pelos motivos abaixo.',
            ),
            ..._listaDeMotivos(context, aptidao.impedimentos),
          ],
        );
      },
    );
  }
}
