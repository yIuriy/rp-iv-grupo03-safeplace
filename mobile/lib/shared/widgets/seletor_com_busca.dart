import 'package:material_ui/material_ui.dart';

import '../tema/tokens.g.dart';
import 'estados.dart';

/// Escolha de um item numa lista longa (#316): colaborador, EPI, área de risco. O campo se parece
/// com os outros do formulário; ao tocar, abre uma lista com busca que ignora maiúsculas e acentos
/// ("joao" encontra "João").
///
/// Participa do `Form` como qualquer campo: [validador] roda no envio e o erro aparece embaixo.
/// [carregando] e [falha] cobrem as opções que ainda vêm da API.
class SeletorComBusca<T> extends FormField<T> {
  SeletorComBusca({
    super.key,
    required String rotulo,
    required List<T> opcoes,
    required String Function(T) descrever,
    required ValueChanged<T?> aoMudar,
    String Function(T)? detalhar,
    T? valor,
    String? ajuda,
    String dica = 'Toque para escolher',
    String rotuloBusca = 'Buscar',
    super.validator,
    bool habilitado = true,
    bool carregando = false,
    String? falha,
  }) : super(
         initialValue: valor,
         enabled: habilitado && !carregando && falha == null,
         builder: (estado) {
           final contexto = estado.context;
           final selecionado = estado.value;
           final ativo = estado.widget.enabled;

           Future<void> abrir() async {
             final escolhido = await showModalBottomSheet<T>(
               context: contexto,
               isScrollControlled: true,
               useSafeArea: true,
               showDragHandle: true,
               builder: (_) => _ListaComBusca<T>(
                 titulo: rotulo,
                 rotuloBusca: rotuloBusca,
                 opcoes: opcoes,
                 descrever: descrever,
                 detalhar: detalhar,
                 selecionado: selecionado,
               ),
             );
             if (escolhido != null) {
               estado.didChange(escolhido);
               aoMudar(escolhido);
             }
           }

           final texto = carregando
               ? 'Carregando...'
               : selecionado == null
               ? dica
               : descrever(selecionado);
           return Semantics(
             button: true,
             enabled: ativo,
             label: rotulo,
             value: selecionado == null ? null : descrever(selecionado),
             excludeSemantics: true,
             child: InkWell(
               onTap: ativo ? abrir : null,
               borderRadius: BorderRadius.circular(TokensDimensoes.radius8),
               child: InputDecorator(
                 isEmpty: false,
                 decoration: InputDecoration(
                   labelText: rotulo,
                   helperText: ajuda,
                   errorText: falha ?? estado.errorText,
                   enabled: ativo,
                   fillColor: ativo ? null : TokensCores.controlDisabledBg,
                   suffixIcon: const Icon(Icons.arrow_drop_down),
                 ),
                 child: Text(
                   texto,
                   overflow: TextOverflow.ellipsis,
                   style: TextStyle(
                     color: selecionado == null
                         ? TokensCores.textSecondary
                         : null,
                   ),
                 ),
               ),
             ),
           );
         },
       );
}

class _ListaComBusca<T> extends StatefulWidget {
  const _ListaComBusca({
    required this.titulo,
    required this.rotuloBusca,
    required this.opcoes,
    required this.descrever,
    required this.detalhar,
    required this.selecionado,
  });

  final String titulo;
  final String rotuloBusca;
  final List<T> opcoes;
  final String Function(T) descrever;
  final String Function(T)? detalhar;
  final T? selecionado;

  @override
  State<_ListaComBusca<T>> createState() => _ListaComBuscaState<T>();
}

class _ListaComBuscaState<T> extends State<_ListaComBusca<T>> {
  String _termo = '';

  @override
  Widget build(BuildContext context) {
    final termo = normalizarBusca(_termo);
    final filtradas = widget.opcoes.where((opcao) {
      if (termo.isEmpty) return true;
      final texto =
          '${widget.descrever(opcao)} ${widget.detalhar?.call(opcao) ?? ''}';
      return normalizarBusca(texto).contains(termo);
    }).toList();

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.viewInsetsOf(context).bottom),
      child: SizedBox(
        height: MediaQuery.sizeOf(context).height * 0.75,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: TokensDimensoes.layoutMobilePadding,
              ),
              child: Text(
                widget.titulo,
                style: Theme.of(context).textTheme.titleLarge,
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              child: TextField(
                autofocus: true,
                onChanged: (valor) => setState(() => _termo = valor),
                decoration: InputDecoration(
                  labelText: widget.rotuloBusca,
                  prefixIcon: const Icon(Icons.search),
                ),
              ),
            ),
            Semantics(
              liveRegion: true,
              child: Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: TokensDimensoes.layoutMobilePadding,
                ),
                child: Text(
                  filtradas.length == 1
                      ? '1 resultado'
                      : '${filtradas.length} resultados',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ),
            ),
            Expanded(
              child: filtradas.isEmpty
                  ? const EstadoVazio(
                      titulo: 'Nada encontrado',
                      descricao: 'Confira o que foi digitado ou busque por outro termo.',
                    )
                  : ListView.builder(
                      itemCount: filtradas.length,
                      itemBuilder: (_, indice) {
                        final opcao = filtradas[indice];
                        final escolhida = opcao == widget.selecionado;
                        return ListTile(
                          title: Text(widget.descrever(opcao)),
                          subtitle: widget.detalhar == null
                              ? null
                              : Text(widget.detalhar!(opcao)),
                          selected: escolhida,
                          trailing: escolhida ? const Icon(Icons.check) : null,
                          onTap: () => Navigator.of(context).pop(opcao),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Minúsculas e sem acentos, para a busca achar "João" digitando "joao".
String normalizarBusca(String texto) {
  const comAcento = 'áàâãäéèêëíìîïóòôõöúùûüçñ';
  const semAcento = 'aaaaaeeeeiiiiooooouuuucn';
  final minusculo = texto.toLowerCase().trim();
  final saida = StringBuffer();
  for (final letra in minusculo.split('')) {
    final posicao = comAcento.indexOf(letra);
    saida.write(posicao < 0 ? letra : semAcento[posicao]);
  }
  return saida.toString();
}
