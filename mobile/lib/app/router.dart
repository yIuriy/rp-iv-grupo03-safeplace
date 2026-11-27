import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../core/sessao/controlador_sessao.dart';
import '../features/autenticacao/presentation/login_page.dart';
import '../features/emprestimos/presentation/emprestimos_page.dart';
import '../features/epis/presentation/epi_detalhe_page.dart';
import '../features/epis/presentation/epis_page.dart';
import '../features/inicio/presentation/inicio_page.dart';
import '../features/ocorrencias/presentation/ocorrencia_detalhe_page.dart';
import '../features/ocorrencias/presentation/ocorrencias_page.dart';
import '../features/usuarios/presentation/exemplo_colaborador_picker_page.dart';
import '../features/usuarios/presentation/perfil_page.dart';
import '../features/usuarios/presentation/usuarios_page.dart';
import '../shared/catalogo/catalogo_page.dart';
import 'casca.dart';
import 'navegacao.dart';

abstract final class Rotas {
  static const carregando = '/carregando';
  static const login = '/login';
  static const inicio = '/';
  static const ocorrencias = '/ocorrencias';
  static const epis = '/epis';
  static const emprestimos = '/emprestimos';
  static const usuarios = '/usuarios';
  static const perfil = '/perfil';

  /// Tela de exemplo do `ColaboradorPicker` (#317), só no modo debug.
  static const exemploSeletorColaborador = '/usuarios/exemplo-seletor';

  /// Catálogo de componentes (#316), só no modo debug: `flutter run --route=/catalogo`.
  static const catalogo = '/catalogo';
}

/// Decide a rota pela sessão e pelo perfil (#316):
/// - sem sessão, só o login abre; com sessão, o login volta ao início;
/// - rota de um módulo que o perfil não vê (Supervisor em `/alertas`) volta ao início, como se o
///   item não existisse. O backend continua recusando a operação com 403 (RNF03).
String? redirecionar(AsyncValue<EstadoSessao> estado, String local) {
  // O catálogo não mostra dado nenhum do backend, então abre sem sessão, como o /design-system do web.
  if (kDebugMode && local == Rotas.catalogo) return null;
  if (estado.isLoading && !estado.hasValue) {
    return local == Rotas.carregando ? null : Rotas.carregando;
  }
  final sessao = estado.value?.sessao;
  if (sessao == null) return local == Rotas.login ? null : Rotas.login;
  if (local == Rotas.login || local == Rotas.carregando) return Rotas.inicio;
  final item = itemDaRota(local);
  if (item != null && !item.permite(sessao.perfil)) return Rotas.inicio;
  return null;
}

/// Tela de cada item do menu. As issues de cada módulo trocam a página "em construção" pela tela
/// real aqui, que é o único lugar que junta as features.
Widget _telaDo(ItemMenu item, BuildContext context) {
  if (item.rota == Rotas.inicio) {
    return InicioPage(
      atalhos: [
        (
          rotulo: 'Registrar ocorrência',
          icone: Icons.assignment_add,
          aoTocar: () => context.go(Rotas.ocorrencias),
        ),
        (
          rotulo: 'Novo empréstimo de EPI',
          icone: Icons.handshake_outlined,
          aoTocar: () => context.go(Rotas.emprestimos),
        ),
      ],
    );
  }
  return switch (item.rota) {
    Rotas.epis => const EpisPage(),
    Rotas.emprestimos => const EmprestimosPage(),
    Rotas.ocorrencias => const OcorrenciasPage(),
    Rotas.usuarios => const UsuariosPage(),
    Rotas.perfil => const PerfilPage(),
    _ => PaginaEmConstrucao(item: item),
  };
}

/// Sub-rotas de cada item, que herdam a casca e a guarda de perfil do item.
List<RouteBase> _subrotasDo(ItemMenu item) => [
  if (item.rota == Rotas.epis)
    GoRoute(
      path: ':id', // /epis/{id}
      builder: (_, state) =>
          EpiDetalhePage(id: int.tryParse(state.pathParameters['id'] ?? '')),
    ),
  if (item.rota == Rotas.ocorrencias)
    GoRoute(
      path: ':id', // /ocorrencias/{id}
      builder: (_, state) => OcorrenciaDetalhePage(
        id: int.tryParse(state.pathParameters['id'] ?? ''),
      ),
    ),
  if (kDebugMode && item.rota == Rotas.usuarios)
    GoRoute(
      path: 'exemplo-seletor', // Rotas.exemploSeletorColaborador

      builder: (_, _) => const ExemploColaboradorPickerPage(),
    ),
];

final routerProvider = Provider<GoRouter>((ref) {
  final sessao = ValueNotifier(ref.read(controladorSessaoProvider));
  ref.listen(controladorSessaoProvider, (_, novo) => sessao.value = novo);
  ref.onDispose(sessao.dispose);

  final router = GoRouter(
    initialLocation: Rotas.carregando,
    refreshListenable: sessao,
    redirect: (_, state) => redirecionar(sessao.value, state.matchedLocation),
    errorBuilder: (_, _) => const PaginaNaoEncontrada(),
    routes: [
      GoRoute(
        path: Rotas.carregando,
        builder: (_, _) =>
            const Scaffold(body: Center(child: CircularProgressIndicator())),
      ),
      GoRoute(path: Rotas.login, builder: (_, _) => const LoginPage()),
      ShellRoute(
        builder: (_, state, filho) =>
            CascaApp(local: state.uri.path, filho: filho),
        routes: [
          for (final item in itensDoMenu)
            GoRoute(
              path: item.rota,
              builder: (context, _) => _telaDo(item, context),
              routes: _subrotasDo(item),
            ),
        ],
      ),
      if (kDebugMode)
        GoRoute(
          path: Rotas.catalogo,
          builder: (_, _) => CatalogoPage(
            exemplos: [
              (
                titulo: 'Seletor de colaborador',
                descricao: 'ColaboradorPicker (#317), com os colaboradores da API. Pede login.',
                abrir: (context) => context.go(Rotas.exemploSeletorColaborador),
              ),
            ],
          ),
        ),
    ],
  );
  ref.onDispose(router.dispose);
  return router;
});
