import 'package:material_ui/material_ui.dart';

import '../tema/app_theme.dart';
import '../tema/tokens.g.dart';
import 'aviso.dart';

/// Pede confirmação antes de uma operação (#316), como o `ConfirmDialog` do web: título que
/// nomeia a operação, explicação, "Continuar editando" para desistir e o botão da operação.
///
/// Sem [aoConfirmar], devolve `true` se a pessoa confirmou e `false` se desistiu. Com
/// [aoConfirmar], o diálogo executa a operação: o botão mostra "carregando", o diálogo não fecha
/// no meio e, se der erro, a mensagem de [descreverFalha] aparece dentro dele para tentar de novo.
/// Devolve `true` só quando a operação terminou bem.
///
/// [perigosa] (padrão) usa o botão vermelho do web, para descartar, remover ou arquivar.
Future<bool> confirmar(
  BuildContext context, {
  required String titulo,
  required String descricao,
  required String rotuloConfirmar,
  String rotuloCancelar = 'Continuar editando',
  bool perigosa = true,
  Future<void> Function()? aoConfirmar,
  String Function(Object erro)? descreverFalha,
}) async {
  final confirmou = await showDialog<bool>(
    context: context,
    barrierDismissible: aoConfirmar == null,
    builder: (_) => DialogoConfirmacao(
      titulo: titulo,
      descricao: descricao,
      rotuloConfirmar: rotuloConfirmar,
      rotuloCancelar: rotuloCancelar,
      perigosa: perigosa,
      aoConfirmar: aoConfirmar,
      descreverFalha: descreverFalha,
    ),
  );
  return confirmou ?? false;
}

class DialogoConfirmacao extends StatefulWidget {
  const DialogoConfirmacao({
    super.key,
    required this.titulo,
    required this.descricao,
    required this.rotuloConfirmar,
    this.rotuloCancelar = 'Continuar editando',
    this.perigosa = true,
    this.aoConfirmar,
    this.descreverFalha,
  });

  final String titulo;
  final String descricao;
  final String rotuloConfirmar;
  final String rotuloCancelar;
  final bool perigosa;
  final Future<void> Function()? aoConfirmar;
  final String Function(Object erro)? descreverFalha;

  @override
  State<DialogoConfirmacao> createState() => _DialogoConfirmacaoState();
}

class _DialogoConfirmacaoState extends State<DialogoConfirmacao> {
  bool _executando = false;
  String? _falha;

  Future<void> _confirmar() async {
    final operacao = widget.aoConfirmar;
    if (operacao == null) {
      Navigator.of(context).pop(true);
      return;
    }
    setState(() {
      _executando = true;
      _falha = null;
    });
    try {
      await operacao();
      if (mounted) Navigator.of(context).pop(true);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _executando = false;
        _falha =
            widget.descreverFalha?.call(erro) ??
            'Não foi possível concluir. Tente novamente.';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: !_executando,
      child: AlertDialog(
        title: Text(widget.titulo),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(widget.descricao),
            if (_falha != null) ...[
              const SizedBox(height: TokensDimensoes.space12),
              Aviso(tom: TomAviso.perigo, anunciar: true, titulo: _falha!),
            ],
          ],
        ),
        actions: [
          OutlinedButton(
            onPressed: _executando
                ? null
                : () => Navigator.of(context).pop(false),
            child: Text(widget.rotuloCancelar),
          ),
          FilledButton(
            style: widget.perigosa ? AppTheme.botaoPerigo : null,
            onPressed: _executando ? null : _confirmar,
            child: _executando
                ? Semantics(
                    label: widget.rotuloConfirmar,
                    child: const SizedBox.square(
                      dimension: TokensDimensoes.sizeIcon,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: TokensCores.textSecondary,
                      ),
                    ),
                  )
                : Text(widget.rotuloConfirmar),
          ),
        ],
      ),
    );
  }
}
