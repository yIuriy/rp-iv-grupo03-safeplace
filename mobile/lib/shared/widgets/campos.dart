import 'package:flutter/services.dart';
import 'package:material_ui/material_ui.dart';

import '../tema/tokens.g.dart';

/// Campo de texto do formulário (#316), o `TextField`/`TextArea` do web: rótulo sempre visível,
/// ajuda embaixo e o erro no lugar da ajuda.
///
/// O erro vem de dois lugares:
/// - [validador], conferido pelo `Form` antes do envio (validação local);
/// - [erroDoServidor], para o que só o backend sabe, como "CPF já cadastrado" num 409. Ele some
///   assim que a pessoa altera o campo, já que o valor recusado mudou.
class CampoTexto extends StatefulWidget {
  const CampoTexto({
    super.key,
    required this.rotulo,
    this.controlador,
    this.valorInicial,
    this.ajuda,
    this.dica,
    this.validador,
    this.erroDoServidor,
    this.habilitado = true,
    this.somenteLeitura = false,
    this.linhas = 1,
    this.teclado,
    this.acaoTeclado,
    this.aoEnviar,
    this.aoMudar,
    this.formatadores,
    this.dicasAutopreenchimento,
    this.foco,
    this.textoOculto = false,
    this.sufixo,
    this.tamanhoMaximo,
  });

  final String rotulo;
  final TextEditingController? controlador;
  final String? valorInicial;
  final String? ajuda;
  final String? dica;
  final FormFieldValidator<String>? validador;
  final String? erroDoServidor;
  final bool habilitado;
  final bool somenteLeitura;

  /// Mais de 1 vira área de texto (TextArea do web), que cresce até esse número de linhas.
  final int linhas;
  final TextInputType? teclado;
  final TextInputAction? acaoTeclado;
  final ValueChanged<String>? aoEnviar;
  final ValueChanged<String>? aoMudar;
  final List<TextInputFormatter>? formatadores;
  final Iterable<String>? dicasAutopreenchimento;
  final FocusNode? foco;
  final bool textoOculto;
  final Widget? sufixo;
  final int? tamanhoMaximo;

  @override
  State<CampoTexto> createState() => _CampoTextoState();
}

class _CampoTextoState extends State<CampoTexto> {
  String? _erroDoServidor;

  @override
  void initState() {
    super.initState();
    _erroDoServidor = widget.erroDoServidor;
  }

  @override
  void didUpdateWidget(CampoTexto antigo) {
    super.didUpdateWidget(antigo);
    if (widget.erroDoServidor != antigo.erroDoServidor) {
      _erroDoServidor = widget.erroDoServidor;
    }
  }

  @override
  Widget build(BuildContext context) {
    final cores = Theme.of(context).colorScheme;
    return TextFormField(
      controller: widget.controlador,
      initialValue: widget.controlador == null ? widget.valorInicial : null,
      focusNode: widget.foco,
      enabled: widget.habilitado,
      readOnly: widget.somenteLeitura,
      obscureText: widget.textoOculto,
      minLines: widget.linhas > 1 ? 3 : 1,
      maxLines: widget.textoOculto ? 1 : widget.linhas,
      maxLength: widget.tamanhoMaximo,
      keyboardType: widget.linhas > 1
          ? TextInputType.multiline
          : widget.teclado,
      textInputAction: widget.acaoTeclado,
      inputFormatters: widget.formatadores,
      autofillHints: widget.dicasAutopreenchimento,
      onFieldSubmitted: widget.aoEnviar,
      onChanged: (valor) {
        if (_erroDoServidor != null) setState(() => _erroDoServidor = null);
        widget.aoMudar?.call(valor);
      },
      validator: widget.validador,
      decoration: InputDecoration(
        labelText: widget.rotulo,
        helperText: widget.ajuda,
        hintText: widget.dica,
        errorText: _erroDoServidor,
        suffixIcon: widget.sufixo,
        // Somente leitura: fundo da página, como o `[readonly]` do web; desabilitado: cinza.
        fillColor: !widget.habilitado
            ? TokensCores.controlDisabledBg
            : widget.somenteLeitura
            ? TokensCores.surfacePage
            : cores.surface,
      ),
    );
  }
}

/// Uma opção de [CampoSelecao].
typedef OpcaoSelecao<T> = ({T valor, String rotulo});

/// Seleção entre poucas opções fixas (#316), o `Select` do web. Para listas longas, como
/// colaboradores ou EPIs, use `SeletorComBusca`.
class CampoSelecao<T> extends StatelessWidget {
  const CampoSelecao({
    super.key,
    required this.rotulo,
    required this.opcoes,
    required this.aoMudar,
    this.valor,
    this.ajuda,
    this.dica,
    this.validador,
    this.erroDoServidor,
    this.habilitado = true,
  });

  final String rotulo;
  final List<OpcaoSelecao<T>> opcoes;
  final ValueChanged<T?> aoMudar;
  final T? valor;
  final String? ajuda;
  final String? dica;
  final FormFieldValidator<T>? validador;
  final String? erroDoServidor;
  final bool habilitado;

  @override
  Widget build(BuildContext context) {
    return DropdownButtonFormField<T>(
      initialValue: valor,
      isExpanded: true,
      items: [
        for (final opcao in opcoes)
          DropdownMenuItem(
            value: opcao.valor,
            child: Text(opcao.rotulo, overflow: TextOverflow.ellipsis),
          ),
      ],
      onChanged: habilitado ? aoMudar : null,
      validator: validador,
      decoration: InputDecoration(
        labelText: rotulo,
        helperText: ajuda,
        hintText: dica,
        errorText: erroDoServidor,
        fillColor: habilitado ? null : TokensCores.controlDisabledBg,
      ),
    );
  }
}

/// Validador de campo obrigatório: texto vazio ou só com espaços recebe [mensagem].
FormFieldValidator<String> obrigatorio(String mensagem) =>
    (valor) => (valor ?? '').trim().isEmpty ? mensagem : null;
