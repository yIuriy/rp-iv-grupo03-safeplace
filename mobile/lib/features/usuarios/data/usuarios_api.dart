import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_client.dart';
import '../../../core/api/api_client_provider.dart';
import '../../../shared/formularios/cpf.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/offline/cache_offline.dart';
import '../domain/cadastro_de_usuarios.dart';
import 'acao_usuario.dart';
import 'usuario.dart';

/// Escritas de `/api/usuarios` (RF23). Os corpos seguem os `Request` do backend, que recusam com
/// 400 qualquer campo a mais: o Colaborador não recebe senha nem perfil, e a edição não leva CPF.
class UsuariosApi implements CadastroDeUsuarios {
  const UsuariosApi(this._client);

  final ApiClient _client;

  @override
  Future<UsuarioCriado> cadastrarSupervisor(DadosPessoa dados) async =>
      UsuarioCriado.ler(
        await _client.post('/usuarios/supervisores', corpo: _cadastro(dados)),
      );

  @override
  Future<Usuario> cadastrarColaborador(DadosPessoa dados) async => Usuario.ler(
    await _client.post('/usuarios/colaboradores', corpo: _cadastro(dados)),
  );

  @override
  Future<Usuario> atualizarSupervisor(int id, DadosPessoa dados) async =>
      Usuario.ler(
        await _client.put(
          '/usuarios/supervisores/$id',
          corpo: _atualizacao(dados),
        ),
      );

  @override
  Future<Usuario> atualizarColaborador(int id, DadosPessoa dados) async =>
      Usuario.ler(
        await _client.put(
          '/usuarios/colaboradores/$id',
          corpo: _atualizacao(dados),
        ),
      );

  static Map<String, Object?> _cadastro(DadosPessoa dados) => {
    'nome': dados.nome,
    'cpf': digitosDoCpf(dados.cpf),
    'dataNascimento': dataIso(dados.dataNascimento),
    'email': ?dados.email,
  };

  /// Só os dados editáveis (issue #122). Sem e-mail, o do Colaborador fica sem contato.
  static Map<String, Object?> _atualizacao(DadosPessoa dados) => {
    'nome': dados.nome,
    'dataNascimento': dataIso(dados.dataNascimento),
    'email': ?dados.email,
  };
}

final cadastroDeUsuariosProvider = Provider<CadastroDeUsuarios>(
  (ref) => UsuariosApi(ref.read(apiClientProvider)),
);

/// Filtros de `GET /api/usuarios/colaboradores?nome=&cpf=`. O backend busca parte do nome, sem
/// diferenciar maiúsculas, e o CPF inteiro (11 dígitos).
class FiltroColaboradores {
  /// Normaliza o que foi digitado: nome sem espaços nas pontas e CPF só com dígitos; vazio vira
  /// "sem filtro".
  factory FiltroColaboradores({String? nome, String? cpf}) {
    final nomeLimpo = nome?.trim() ?? '';
    final digitos = digitosDoCpf(cpf ?? '');
    return FiltroColaboradores._(
      nomeLimpo.isEmpty ? null : nomeLimpo,
      digitos.isEmpty ? null : digitos,
    );
  }

  const FiltroColaboradores._(this.nome, this.cpf);

  static const nenhum = FiltroColaboradores._(null, null);

  final String? nome;
  final String? cpf;

  bool get vazio => nome == null && cpf == null;

  Map<String, String> get consulta => {'nome': ?nome, 'cpf': ?cpf};

  /// Chave do cache offline: cada busca guarda a sua cópia.
  String get chaveCache => vazio
      ? 'usuarios/colaboradores'
      : 'usuarios/colaboradores?${Uri(queryParameters: consulta).query}';

  @override
  bool operator ==(Object other) =>
      other is FiltroColaboradores && other.nome == nome && other.cpf == cpf;

  @override
  int get hashCode => Object.hash(nome, cpf);
}

// Leituras com cópia local por conta (RNF08). `autoDispose`: ao sair da tela (ou da conta), a
// consulta é descartada e a próxima abertura busca de novo. Sem tentativas automáticas: as telas
// oferecem "Tentar novamente" e o puxar para atualizar.

/// Supervisores, a partir de `GET /api/usuarios` (só o Gestor).
final supervisoresProvider =
    FutureProvider.autoDispose<Consultado<List<Usuario>>>(
      (ref) => consultarComCache(
        ref,
        chave: 'usuarios/supervisores',
        buscar: (api) => api.get('/usuarios'),
        ler: lerSupervisores,
      ),
      retry: (_, _) => null,
    );

/// Colaboradores, com os filtros opcionais (Gestor e Supervisor).
final colaboradoresProvider = FutureProvider.autoDispose
    .family<Consultado<List<Usuario>>, FiltroColaboradores>(
      (ref, filtro) => consultarComCache(
        ref,
        chave: filtro.chaveCache,
        buscar: (api) => api.get(
          '/usuarios/colaboradores',
          consulta: filtro.vazio ? null : filtro.consulta,
        ),
        ler: lerUsuarios,
      ),
      retry: (_, _) => null,
    );

/// Dados da própria conta (`GET /api/usuarios/me`). Mesma chave de cache do Início, que lê a
/// mesma resposta.
final meuPerfilProvider = FutureProvider.autoDispose<Consultado<Usuario>>(
  (ref) => consultarComCache(
    ref,
    chave: 'usuarios/me',
    buscar: (api) => api.get('/usuarios/me'),
    ler: Usuario.ler,
  ),
  retry: (_, _) => null,
);

/// Quantas ações o perfil mostra de início, como o web, e o máximo que o backend aceita.
const limiteHistoricoPadrao = 20;
const limiteHistoricoMaximo = 100;

/// O backend aceita `limite` de 1 a 100 e ajusta o que vier fora; o app já envia ajustado.
int ajustarLimiteHistorico(int limite) =>
    limite.clamp(1, limiteHistoricoMaximo);

/// Ações recentes da pessoa logada (`GET /api/usuarios/me/historico?limite=`), da mais recente
/// para a mais antiga.
final historicoProvider = FutureProvider.autoDispose
    .family<Consultado<List<AcaoUsuario>>, int>((ref, limite) {
      final ajustado = ajustarLimiteHistorico(limite);
      return consultarComCache(
        ref,
        chave: 'usuarios/me/historico?limite=$ajustado',
        buscar: (api) =>
            api.get('/usuarios/me/historico', consulta: {'limite': ajustado}),
        ler: lerAcoes,
      );
    }, retry: (_, _) => null);
