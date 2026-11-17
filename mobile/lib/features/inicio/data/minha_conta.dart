import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/leitura.dart';
import '../../../shared/offline/cache_offline.dart';

/// Dados da própria conta (`GET /api/usuarios/me`), mostrados no Início. Os dois perfis podem ler.
class MinhaConta {
  const MinhaConta({required this.nome, required this.email, this.cpf});

  final String nome;
  final String email;
  final String? cpf;

  factory MinhaConta.ler(Object? dado) {
    final b = objeto(dado, 'Dados da conta inválidos');
    return MinhaConta(
      nome: texto(b['nome'], 'Nome não informado'),
      email: texto(b['email'], 'E-mail não informado'),
      cpf: textoOuNulo(b['cpf']),
    );
  }
}

/// Consulta com cópia local (#316): é a tela de exemplo do modo offline. Sem tentativas
/// automáticas: a tela oferece "Tentar novamente".
final minhaContaProvider = FutureProvider<Consultado<MinhaConta>>(
  (ref) => consultarComCache(
    ref,
    chave: 'usuarios/me',
    buscar: (api) => api.get('/usuarios/me'),
    ler: MinhaConta.ler,
  ),
  retry: (_, _) => null,
);
