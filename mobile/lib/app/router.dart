import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';

import '../features/autenticacao/presentation/login_page.dart';
import '../features/inicio/presentation/inicio_page.dart';
import '../core/sessao/controlador_sessao.dart';

abstract final class Rotas {
  static const carregando = '/carregando';
  static const login = '/login';
  static const inicio = '/';
}

/// Decide a rota pela sessão: sem sessão só o login abre; com sessão o login volta ao início.
/// A #316 acrescenta aqui as rotas por perfil.
String? redirecionar(AsyncValue<EstadoSessao> estado, String local) {
  if (estado.isLoading && !estado.hasValue) {
    return local == Rotas.carregando ? null : Rotas.carregando;
  }
  final logado = estado.value?.sessao != null;
  if (!logado) return local == Rotas.login ? null : Rotas.login;
  return local == Rotas.login || local == Rotas.carregando
      ? Rotas.inicio
      : null;
}

final routerProvider = Provider<GoRouter>((ref) {
  final sessao = ValueNotifier(ref.read(controladorSessaoProvider));
  ref.listen(controladorSessaoProvider, (_, novo) => sessao.value = novo);
  ref.onDispose(sessao.dispose);

  final router = GoRouter(
    initialLocation: Rotas.carregando,
    refreshListenable: sessao,
    redirect: (_, state) => redirecionar(sessao.value, state.matchedLocation),
    routes: [
      GoRoute(
        path: Rotas.carregando,
        builder: (_, _) =>
            const Scaffold(body: Center(child: CircularProgressIndicator())),
      ),
      GoRoute(path: Rotas.login, builder: (_, _) => const LoginPage()),
      GoRoute(path: Rotas.inicio, builder: (_, _) => const InicioPage()),
    ],
  );
  ref.onDispose(router.dispose);
  return router;
});
