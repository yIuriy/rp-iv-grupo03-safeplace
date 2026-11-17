import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:safeplace/app/navegacao.dart';
import 'package:safeplace/app/router.dart';
import 'package:safeplace/core/sessao/controlador_sessao.dart';
import 'package:safeplace/core/sessao/sessao.dart';

import '../apoio.dart';

const sessaoSupervisor = Sessao(
  token: 'token-supervisor',
  email: 'supervisor.demo@safeplace.com',
  nome: 'Supervisor Demo',
  perfil: Perfil.supervisor,
);

void main() {
  group('menu por perfil', () {
    test('Gestor vê Usuários e Alertas', () {
      final rotulos = itensPara(Perfil.gestorSeguranca)
          .map((i) => i.rotuloPara(Perfil.gestorSeguranca));

      expect(
        rotulos,
        containsAll(['Início', 'Usuários', 'Alertas', 'Relatórios']),
      );
    });

    test('Supervisor vê Colaboradores e não vê Alertas', () {
      final rotulos = itensPara(Perfil.supervisor)
          .map((i) => i.rotuloPara(Perfil.supervisor));

      expect(rotulos, contains('Colaboradores'));
      expect(rotulos, isNot(contains('Usuários')));
      expect(rotulos, isNot(contains('Alertas')));
    });

    test('cada rota pertence a um item, inclusive sub-rotas', () {
      expect(itemDaRota('/')?.rotulo, 'Início');
      expect(itemDaRota('/ocorrencias')?.rotulo, 'Ocorrências');
      expect(itemDaRota('/ocorrencias/12')?.rotulo, 'Ocorrências');
      expect(itemDaRota('/epis-antigos'), isNull);
      expect(itemDaRota('/nao-existe'), isNull);
    });
  });

  group('guarda de rotas', () {
    AsyncValue<EstadoSessao> com(Sessao? sessao) =>
        AsyncValue.data(EstadoSessao(sessao: sessao));

    test('sem sessão, qualquer tela leva ao login', () {
      expect(redirecionar(com(null), '/ocorrencias'), Rotas.login);
      expect(redirecionar(com(null), Rotas.login), isNull);
    });

    test('com sessão, o login volta ao início', () {
      expect(redirecionar(com(sessaoGestor), Rotas.login), Rotas.inicio);
    });

    test('rota de outro perfil volta ao início; a do próprio perfil abre', () {
      expect(redirecionar(com(sessaoSupervisor), '/alertas'), Rotas.inicio);
      expect(redirecionar(com(sessaoGestor), '/alertas'), isNull);
      expect(redirecionar(com(sessaoSupervisor), '/usuarios'), isNull);
    });

    test('enquanto a sessão carrega, mostra a tela de carregamento', () {
      expect(
        redirecionar(const AsyncValue.loading(), '/epis'),
        Rotas.carregando,
      );
    });
  });
}
