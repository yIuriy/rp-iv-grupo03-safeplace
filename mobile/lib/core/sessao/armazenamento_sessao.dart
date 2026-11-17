import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'sessao.dart';

/// Onde a sessão sobrevive ao fechamento do app. Interface para os testes usarem memória.
abstract interface class ArmazenamentoSessao {
  Future<Sessao?> ler();
  Future<void> guardar(Sessao sessao);
  Future<void> apagar();
}

/// Keychain (iOS) e Keystore (Android). O token é credencial e não vai para preferências comuns.
class ArmazenamentoSessaoSeguro implements ArmazenamentoSessao {
  ArmazenamentoSessaoSeguro([FlutterSecureStorage? armazenamento])
    : _armazenamento = armazenamento ?? const FlutterSecureStorage();

  static const chave = 'safeplace.sessao';
  final FlutterSecureStorage _armazenamento;

  @override
  Future<Sessao?> ler() async {
    try {
      final bruto = await _armazenamento.read(key: chave);
      return bruto == null ? null : Sessao.ler(jsonDecode(bruto));
    } catch (erro) {
      // Sessão corrompida ou de formato antigo: descarta e pede login de novo.
      debugPrint('Sessão guardada ilegível, descartada: $erro');
      await apagar();
      return null;
    }
  }

  @override
  Future<void> guardar(Sessao sessao) =>
      _armazenamento.write(key: chave, value: jsonEncode(sessao.toJson()));

  @override
  Future<void> apagar() => _armazenamento.delete(key: chave);
}

final armazenamentoSessaoProvider = Provider<ArmazenamentoSessao>(
  (ref) => ArmazenamentoSessaoSeguro(),
);

/// Limpa os dados locais da conta ao sair ou expirar (RNF08). O app liga aqui a limpeza do cache
/// offline (`app/dependencias.dart`); o núcleo não conhece o `shared/`, então o padrão não faz nada.
final limpezaDaContaProvider = Provider<Future<void> Function(String email)>(
  (ref) => (_) async {},
);
