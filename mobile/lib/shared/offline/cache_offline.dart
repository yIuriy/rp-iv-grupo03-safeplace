import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../core/api/api_client.dart';
import '../../core/api/api_client_provider.dart';
import '../../core/sessao/controlador_sessao.dart';

/// Cópia local das consultas, por conta, para o modo offline somente leitura (RNF08, #316). É o
/// equivalente do `useConsultaOffline` + `cacheStorage` do web.
///
/// Regras:
/// - a cópia é da conta logada (`e-mail + chave`): quem entra depois no mesmo aparelho não vê os
///   dados de outra pessoa, e sair ou expirar a sessão apaga a cópia da conta;
/// - só leitura: o cache guarda respostas de GET; escrita sem conexão não é guardada para depois;
/// - os dados ficam no armazenamento seguro (Keystore/Keychain), porque incluem nome, e-mail e CPF.

/// Onde as cópias ficam. Interface para os testes usarem memória.
abstract interface class ArmazenamentoCache {
  Future<String?> ler(String chave);
  Future<void> gravar(String chave, String valor);
  Future<void> apagarComPrefixo(String prefixo);
}

class ArmazenamentoCacheSeguro implements ArmazenamentoCache {
  ArmazenamentoCacheSeguro([FlutterSecureStorage? armazenamento])
    : _armazenamento = armazenamento ?? const FlutterSecureStorage();

  final FlutterSecureStorage _armazenamento;

  @override
  Future<String?> ler(String chave) => _armazenamento.read(key: chave);

  @override
  Future<void> gravar(String chave, String valor) =>
      _armazenamento.write(key: chave, value: valor);

  @override
  Future<void> apagarComPrefixo(String prefixo) async {
    final todas = await _armazenamento.readAll();
    for (final chave in todas.keys.where((c) => c.startsWith(prefixo))) {
      await _armazenamento.delete(key: chave);
    }
  }
}

/// Uma resposta guardada: o JSON como veio da API e quando foi lido.
class EntradaCache {
  const EntradaCache({required this.dados, required this.salvoEm});

  final Object? dados;
  final DateTime salvoEm;
}

class CacheOffline {
  CacheOffline(this._armazenamento, {DateTime Function()? agora})
    : _agora = agora ?? DateTime.now;

  static const prefixo = 'safeplace.cache.';

  final ArmazenamentoCache _armazenamento;
  final DateTime Function() _agora;

  static String _chave(String conta, String chave) => '$prefixo$conta.$chave';

  /// Falhas do armazenamento não derrubam a tela: sem cache, a consulta só não tem cópia.
  Future<void> guardar(String conta, String chave, Object? dados) async {
    try {
      final entrada = {
        'conta': conta,
        'salvoEm': _agora().toIso8601String(),
        'dados': dados,
      };
      await _armazenamento.gravar(_chave(conta, chave), jsonEncode(entrada));
    } catch (erro) {
      debugPrint('Cache offline indisponível ao gravar $chave: $erro');
    }
  }

  Future<EntradaCache?> ler(String conta, String chave) async {
    try {
      final bruto = await _armazenamento.ler(_chave(conta, chave));
      if (bruto == null) return null;
      final entrada = jsonDecode(bruto);
      if (entrada is! Map || entrada['conta'] != conta) return null;
      final salvoEm = DateTime.tryParse('${entrada['salvoEm']}');
      if (salvoEm == null) return null;
      return EntradaCache(dados: entrada['dados'], salvoEm: salvoEm);
    } catch (erro) {
      debugPrint('Cache offline ilegível para $chave, ignorado: $erro');
      return null;
    }
  }

  Future<void> limparConta(String conta) async {
    try {
      await _armazenamento.apagarComPrefixo('$prefixo$conta.');
    } catch (erro) {
      debugPrint('Não foi possível limpar o cache da conta: $erro');
    }
  }
}

final armazenamentoCacheProvider = Provider<ArmazenamentoCache>(
  (ref) => ArmazenamentoCacheSeguro(),
);

final cacheOfflineProvider = Provider<CacheOffline>(
  (ref) => CacheOffline(ref.watch(armazenamentoCacheProvider)),
);

/// Resultado de uma consulta com cache. [somenteLeitura] é verdadeiro quando os dados vieram da
/// cópia local porque o servidor não respondeu: a tela mostra o `AvisoOffline` e desabilita
/// cadastros e alterações.
class Consultado<T> {
  const Consultado(
    this.dados, {
    this.somenteLeitura = false,
    this.sincronizadoEm,
  });

  final T dados;
  final bool somenteLeitura;

  /// Quando os dados foram lidos do servidor.
  final DateTime? sincronizadoEm;
}

/// Faz a consulta e guarda a resposta; se o servidor não responder (rede, timeout), devolve a
/// cópia local marcada como somente leitura. Respostas de erro do servidor (4xx, 5xx) não usam o
/// cache: o servidor está no ar e disse não, então a falha aparece como falha.
///
/// ```dart
/// final colaboradoresProvider = FutureProvider((ref) => consultarComCache(
///   ref,
///   chave: 'colaboradores',
///   buscar: (api) => api.get('/usuarios/colaboradores'),
///   ler: (json) => lista(json, Colaborador.ler, 'Lista de colaboradores inválida'),
/// ));
/// ```
Future<Consultado<T>> consultarComCache<T>(
  Ref ref, {
  required String chave,
  required Future<Object?> Function(ApiClient api) buscar,
  required T Function(Object? json) ler,
}) async {
  final conta = ref.read(controladorSessaoProvider).value?.sessao?.email;
  final cache = ref.read(cacheOfflineProvider);
  try {
    final json = await buscar(ref.read(apiClientProvider));
    final dados = ler(json);
    if (conta != null) await cache.guardar(conta, chave, json);
    return Consultado(dados, sincronizadoEm: DateTime.now());
  } on FalhaApi catch (falha) {
    if (!falha.semResposta || conta == null) rethrow;
    final copia = await cache.ler(conta, chave);
    if (copia == null) rethrow;
    return Consultado(
      ler(copia.dados),
      somenteLeitura: true,
      sincronizadoEm: copia.salvoEm,
    );
  }
}
