import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:go_router/go_router.dart';
import 'package:material_ui/material_ui.dart';
import 'package:safeplace/app/router.dart';
import 'package:safeplace/core/api/api_client.dart';
import 'package:safeplace/core/api/api_client_provider.dart';
import 'package:safeplace/core/sessao/armazenamento_sessao.dart';
import 'package:safeplace/core/sessao/sessao.dart';
import 'package:safeplace/main.dart';
import 'package:safeplace/shared/offline/cache_offline.dart';

import '../../apoio.dart';
import '../../shared/apoio_offline.dart';

/// Uma chamada recebida pela [ApiUsuariosFalsa].
class Requisicao {
  const Requisicao(this.metodo, this.caminho, {this.consulta, this.corpo});

  final String metodo;
  final String caminho;
  final Map<String, Object?>? consulta;
  final Object? corpo;

  @override
  String toString() => '$metodo $caminho $consulta $corpo';
}

/// API falsa do módulo: [responder] devolve o corpo de cada chamada (ou lança uma [FalhaApi]) e
/// todas as requisições ficam em [recebidas], para conferir caminho, filtros e corpo enviado.
class ApiUsuariosFalsa implements ApiClient {
  ApiUsuariosFalsa([Object? Function(Requisicao)? responder])
    : responder = responder ?? responderPadrao;

  Object? Function(Requisicao requisicao) responder;
  final List<Requisicao> recebidas = [];

  Iterable<Requisicao> chamadas(String metodo, String caminho) =>
      recebidas.where((r) => r.metodo == metodo && r.caminho == caminho);

  Future<Object?> _responder(Requisicao requisicao) async {
    recebidas.add(requisicao);
    return responder(requisicao);
  }

  @override
  Future<Object?> get(String caminho, {Map<String, Object?>? consulta}) =>
      _responder(Requisicao('GET', caminho, consulta: consulta));

  @override
  Future<Object?> post(String caminho, {Object? corpo}) =>
      _responder(Requisicao('POST', caminho, corpo: corpo));

  @override
  Future<Object?> put(String caminho, {Object? corpo}) =>
      _responder(Requisicao('PUT', caminho, corpo: corpo));

  @override
  Future<Object?> patch(String caminho, {Object? corpo}) =>
      _responder(Requisicao('PATCH', caminho, corpo: corpo));

  @override
  Future<Object?> delete(String caminho) =>
      _responder(Requisicao('DELETE', caminho));
}

const sessaoSupervisor = Sessao(
  token: 'token-supervisor',
  email: 'supervisor.demo@safeplace.com',
  nome: 'Supervisor Demo',
  perfil: Perfil.supervisor,
);

// CPFs válidos, para passar na validação local.
const cpfJoana = '52998224725';
const cpfJoao = '11144477735';
const cpfAna = '12345678909';

final gestorJson = {
  'id': 1,
  'nome': 'Gestor Demo',
  'cpf': '39053344705',
  'dataNascimento': '1980-01-15',
  'email': 'gestor.demo@safeplace.com',
  'perfil': 'GESTOR_SEGURANCA',
  'ativo': true,
  'criadoEm': '2026-09-07T10:00:00',
};

final supervisorJson = {
  'id': 2,
  'nome': 'Joana Ribeiro',
  'cpf': cpfJoana,
  'dataNascimento': '1988-04-20',
  'email': 'joana.ribeiro@safeplace.com',
  'perfil': 'SUPERVISOR',
  'ativo': true,
};

final joaoJson = {
  'id': 10,
  'nome': 'João da Silva',
  'cpf': cpfJoao,
  'dataNascimento': '1992-06-18',
  'email': 'joao.silva@safeplace.com',
  'perfil': null,
  'ativo': true,
};

final anaJson = {
  'id': 11,
  'nome': 'Ana Souza',
  'cpf': cpfAna,
  'dataNascimento': '1995-02-03',
  'email': null,
  'perfil': null,
  'ativo': true,
};

/// Colaborador inativo: aparece na lista do módulo, mas não no seletor.
final brunoInativoJson = {
  'id': 12,
  'nome': 'Bruno Lima',
  'cpf': '98765432100',
  'dataNascimento': '1990-10-10',
  'perfil': null,
  'ativo': false,
};

final historicoJson = [
  {
    'id': 42,
    'tipoOperacao': 'CADASTRO',
    'dataHora': '2026-10-05T20:53:27',
    'entidade': 'Supervisor',
    'entidadeId': 2,
    'descricao': 'Cadastro de Supervisor',
  },
  {
    'id': 41,
    'tipoOperacao': 'ALTERACAO',
    'dataHora': '2026-10-04T09:10:00',
    'entidade': 'Emprestimo',
    'entidadeId': 4,
    'descricao': null,
  },
];

/// Respostas de um servidor com dados de exemplo; escritas não são aceitas.
Object? responderPadrao(Requisicao r) => switch ((r.metodo, r.caminho)) {
  ('GET', '/usuarios') => [gestorJson, supervisorJson, joaoJson, anaJson],
  ('GET', '/usuarios/colaboradores') => [anaJson, brunoInativoJson, joaoJson],
  ('GET', '/usuarios/me') => gestorJson,
  ('GET', '/usuarios/me/historico') => historicoJson,
  _ => throw UnimplementedError('Chamada inesperada: $r'),
};

/// Abre o app logado com [sessao], numa tela de celular, e vai para [rota]. Devolve o router.
Future<GoRouter> abrirApp(
  WidgetTester tester, {
  required Sessao sessao,
  required ApiClient api,
  required String rota,
  ArmazenamentoCache? cache,
}) async {
  tester.view.physicalSize = const Size(1080, 2400);
  tester.view.devicePixelRatio = 2.625;
  addTearDown(tester.view.reset);
  final conteiner = ProviderContainer(
    overrides: [
      armazenamentoSessaoProvider.overrideWithValue(
        ArmazenamentoSessaoEmMemoria(sessao),
      ),
      apiClientProvider.overrideWithValue(api),
      armazenamentoCacheProvider.overrideWithValue(
        cache ?? ArmazenamentoCacheEmMemoria(),
      ),
    ],
  );
  addTearDown(conteiner.dispose);
  await tester.pumpWidget(
    UncontrolledProviderScope(
      container: conteiner,
      child: const SafePlaceApp(),
    ),
  );
  await tester.pumpAndSettle();
  final router = conteiner.read(routerProvider);
  router.go(rota);
  await tester.pumpAndSettle();
  return router;
}

/// Campo de formulário pelo rótulo.
Finder campo(String rotulo) => find.widgetWithText(TextFormField, rotulo);

/// Texto dentro do campo de rótulo [rotulo], como o erro que aparece embaixo dele.
Finder noCampo(String rotulo, String texto) =>
    find.descendant(of: campo(rotulo), matching: find.text(texto));

/// Rola a tela até o [alvo] aparecer e toca nele.
Future<void> tocar(WidgetTester tester, Finder alvo) async {
  await tester.ensureVisible(alvo);
  await tester.pumpAndSettle();
  await tester.tap(alvo);
  await tester.pumpAndSettle();
}

/// Servidor fora do ar, para o modo offline.
Never servidorFora(Requisicao _) => throw const FalhaApi();

/// Botão (`FilledButton`, `OutlinedButton`, `TextButton`) que contém o [texto].
T botao<T extends ButtonStyleButton>(WidgetTester tester, String texto) =>
    tester.widget<T>(
      find.ancestor(of: find.text(texto), matching: find.byType(T)).first,
    );

/// Falha HTTP com o corpo padrão do backend.
FalhaApi falhaComMensagem(int status, String mensagem, {String? erro}) =>
    falhaHttp(status, {
      'timestamp': '2026-11-19T10:00:00',
      'status': status,
      'error': erro ?? 'Erro',
      'message': mensagem,
    });
