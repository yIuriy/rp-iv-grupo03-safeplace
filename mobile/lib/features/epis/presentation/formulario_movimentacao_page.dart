// Formulário de entrada ou saída de estoque (UC06, passos 5 a 8), só do Gestor: tipo, quantidade e
// motivo, validação local e envio à API. Quando o servidor recusa (por exemplo, saída acima do
// saldo), a mensagem dele aparece e os dados continuam preenchidos, como o `MovimentacaoDialog` do web.

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../data/epi.dart';
import '../data/epis_api.dart';
import '../data/movimentacao.dart';
import '../domain/epi_repository.dart';
import '../domain/validacao_movimentacao.dart';
import 'componentes_epi.dart';

/// Abre o formulário em tela cheia. Devolve a movimentação registrada, ou `null` se a pessoa
/// desistiu.
Future<Movimentacao?> abrirFormularioMovimentacao(
  BuildContext context, {
  required Epi epi,
}) {
  return Navigator.of(context, rootNavigator: true).push<Movimentacao>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioMovimentacaoPage(epi: epi),
    ),
  );
}

/// Mensagens de falha do envio, as mesmas do web. Saldo insuficiente chega como 400 com a regra no
/// `message`, então a mensagem do backend tem a preferência.
final descreverFalhaDeMovimentacao = criarDescritorDeFalha(
  porStatus: {
    400: usarMensagemDaApi(
      'A movimentação foi recusada pelo servidor. Confira a quantidade e o motivo.',
    ),
    403: fixa(
      'Seu perfil não tem permissão para movimentar o estoque. Apenas o Gestor de Segurança registra entradas e saídas.',
    ),
    404: fixa(
      'EPI não encontrado. Ele pode ter sido alterado por outra pessoa. Volte e recarregue a lista.',
    ),
  },
  padrao: 'Não foi possível registrar a movimentação agora. Os dados preenchidos foram mantidos. Tente novamente.',
);

/// Aviso da tela de origem depois de registrar, o mesmo texto do web.
String mensagemDeMovimentacao(Epi epi, Movimentacao mov) {
  final quantidade = mov.quantidade;
  final unidades = quantidade == 1 ? 'unidade' : 'unidades';
  return '${mov.tipo?.rotulo ?? 'Movimentação'} de ${numeroOuNaoInformado(quantidade)} $unidades '
      'registrada para ${epi.nome}. Saldo atual: ${numeroOuNaoInformado(mov.saldoAtual)}.';
}

class FormularioMovimentacaoPage extends ConsumerStatefulWidget {
  const FormularioMovimentacaoPage({super.key, required this.epi});

  final Epi epi;

  @override
  ConsumerState<FormularioMovimentacaoPage> createState() =>
      _FormularioMovimentacaoPageState();
}

class _FormularioMovimentacaoPageState
    extends ConsumerState<FormularioMovimentacaoPage> {
  final _form = GlobalKey<FormState>();
  final _quantidade = TextEditingController();
  final _motivo = TextEditingController();
  TipoMovimentacao _tipo = TipoMovimentacao.entrada;
  bool _tentouEnviar = false;
  bool _enviando = false;
  String? _falha;

  @override
  void dispose() {
    _quantidade.dispose();
    _motivo.dispose();
    super.dispose();
  }

  void _trocarTipo(TipoMovimentacao tipo) {
    setState(() => _tipo = tipo);
    // A regra do saldo muda com o tipo, então a quantidade é conferida de novo.
    if (_tentouEnviar) _form.currentState!.validate();
  }

  Future<void> _enviar() async {
    setState(() {
      _tentouEnviar = true;
      _falha = null;
    });
    if (!_form.currentState!.validate()) return;
    final epiId = widget.epi.id!;
    setState(() => _enviando = true);
    try {
      final mov = await ref
          .read(epiRepositoryProvider)
          .movimentar(
            epiId,
            DadosMovimentacao(
              tipo: _tipo,
              quantidade: int.parse(_quantidade.text.trim()),
              motivo: _motivo.text.trim(),
            ),
          );
      if (!mounted) return;
      // Saldo, status e histórico mudaram: as telas buscam de novo.
      ref.invalidate(episProvider);
      ref.invalidate(epiProvider(epiId));
      ref.invalidate(movimentacoesProvider(epiId));
      Navigator.of(context).pop<Movimentacao>(mov);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _enviando = false;
        _falha = descreverFalhaDeMovimentacao(erro);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final epi = widget.epi;
    return Scaffold(
      appBar: AppBar(title: const Text('Registrar movimentação')),
      body: SafeArea(
        child: Form(
          key: _form,
          child: ListView(
            padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
            children: [
              Text(
                '${epi.nome}: saldo atual ${numeroOuNaoInformado(epi.quantidade)}, '
                'mínimo ${numeroOuNaoInformado(epi.estoqueMinimo)}. Data, hora e operador são registrados automaticamente.',
                style: Theme.of(context).textTheme.bodyMedium
                    ?.copyWith(color: context.cores.textoSecundario),
              ),
              const SizedBox(height: TokensDimensoes.space24),
              Semantics(
                container: true,
                label: 'Tipo de movimentação',
                child: SegmentedButton<TipoMovimentacao>(
                  segments: const [
                    ButtonSegment(
                      value: TipoMovimentacao.entrada,
                      label: Text('Entrada (reposição)'),
                    ),
                    ButtonSegment(
                      value: TipoMovimentacao.saida,
                      label: Text('Saída (baixa)'),
                    ),
                  ],
                  selected: {_tipo},
                  onSelectionChanged: _enviando
                      ? null
                      : (escolha) => _trocarTipo(escolha.single),
                ),
              ),
              const SizedBox(height: TokensDimensoes.space16),
              CampoTexto(
                rotulo: 'Quantidade',
                controlador: _quantidade,
                ajuda: 'Unidades a movimentar. Uma saída não pode deixar o saldo negativo.',
                teclado: TextInputType.number,
                formatadores: [FilteringTextInputFormatter.digitsOnly],
                validador: (valor) => validarQuantidadeMovimentacao(
                  valor,
                  tipo: _tipo,
                  saldo: epi.quantidade,
                ),
              ),
              const SizedBox(height: TokensDimensoes.space16),
              CampoTexto(
                rotulo: 'Motivo',
                controlador: _motivo,
                linhas: 3,
                ajuda: 'Ex.: recebimento de lote, entrega à equipe, ajuste de inventário.',
                validador: validarMotivo,
              ),
              if (_falha != null) ...[
                const SizedBox(height: TokensDimensoes.space16),
                Aviso(
                  tom: TomAviso.perigo,
                  anunciar: true,
                  titulo: 'Movimentação não registrada',
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
                label: const Text('Registrar'),
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
