import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../tema/cores_safeplace.dart';
import '../tema/tokens.g.dart';
import 'aviso.dart';

/// Lista sem resultados (#316), o `EmptyState` do web: explica a ausência e, quando faz sentido,
/// oferece a ação para sair dela ("Cadastrar colaborador", "Limpar filtros").
class EstadoVazio extends StatelessWidget {
  const EstadoVazio({
    super.key,
    required this.titulo,
    required this.descricao,
    this.icone = Icons.search_off,
    this.rotuloAcao,
    this.aoAgir,
  }) : assert(
         (rotuloAcao == null) == (aoAgir == null),
         'Ação precisa de rótulo e callback.',
       );

  final String titulo;
  final String descricao;
  final IconData icone;
  final String? rotuloAcao;
  final VoidCallback? aoAgir;

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(TokensDimensoes.space24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ExcludeSemantics(
              child: Icon(
                icone,
                size: 40,
                color: context.cores.textoSecundario,
              ),
            ),
            const SizedBox(height: TokensDimensoes.space12),
            Text(titulo, style: textos.titleLarge, textAlign: TextAlign.center),
            const SizedBox(height: TokensDimensoes.space4),
            Text(
              descricao,
              style: textos.bodyMedium?.copyWith(
                color: context.cores.textoSecundario,
              ),
              textAlign: TextAlign.center,
            ),
            if (rotuloAcao != null) ...[
              const SizedBox(height: TokensDimensoes.space16),
              OutlinedButton(onPressed: aoAgir, child: Text(rotuloAcao!)),
            ],
          ],
        ),
      ),
    );
  }
}

/// Carregamento com texto (#316): o leitor de tela anuncia o que está sendo carregado.
class EstadoCarregando extends StatelessWidget {
  const EstadoCarregando({super.key, this.texto = 'Carregando...'});

  final String texto;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Semantics(
        liveRegion: true,
        label: texto,
        excludeSemantics: true,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const CircularProgressIndicator(),
            const SizedBox(height: TokensDimensoes.space12),
            Text(texto, style: Theme.of(context).textTheme.bodyMedium),
          ],
        ),
      ),
    );
  }
}

/// Os quatro estados de uma consulta à API (#316), como o `ResultadoConsulta` do web:
/// carregando, falha com "Tentar novamente", lista vazia e lista com dados.
///
/// ```dart
/// ConteudoAssincrono(
///   valor: ref.watch(colaboradoresProvider),
///   carregando: 'Carregando colaboradores...',
///   falha: 'Não foi possível carregar os colaboradores.',
///   vazio: const EstadoVazio(titulo: 'Nenhum colaborador', descricao: '...'),
///   aoTentarDeNovo: () => ref.invalidate(colaboradoresProvider),
///   construir: (lista) => ListView(...),
/// )
/// ```
///
/// Numa recarga, os dados anteriores continuam na tela até os novos chegarem.
class ConteudoAssincrono<T> extends StatelessWidget {
  const ConteudoAssincrono({
    super.key,
    required this.valor,
    required this.construir,
    required this.vazio,
    required this.falha,
    required this.aoTentarDeNovo,
    this.carregando = 'Carregando...',
    this.descreverFalha,
  });

  final AsyncValue<List<T>> valor;
  final Widget Function(List<T> itens) construir;
  final Widget vazio;

  /// Título do aviso de falha, dizendo o que não carregou.
  final String falha;
  final VoidCallback aoTentarDeNovo;
  final String carregando;

  /// Mensagem do aviso de falha a partir do erro (por exemplo, o descritor da tela); sem ela,
  /// sugere conferir a conexão.
  final String Function(Object erro)? descreverFalha;

  @override
  Widget build(BuildContext context) {
    return valor.when(
      skipLoadingOnReload: true,
      skipLoadingOnRefresh: true,
      loading: () => EstadoCarregando(texto: carregando),
      error: (erro, _) => Padding(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        child: Aviso(
          tom: TomAviso.perigo,
          anunciar: true,
          titulo: falha,
          mensagem:
              descreverFalha?.call(erro) ??
              'Verifique sua conexão ou tente novamente em alguns instantes.',
          rotuloAcao: 'Tentar novamente',
          aoAgir: aoTentarDeNovo,
        ),
      ),
      data: (itens) => itens.isEmpty ? vazio : construir(itens),
    );
  }
}
