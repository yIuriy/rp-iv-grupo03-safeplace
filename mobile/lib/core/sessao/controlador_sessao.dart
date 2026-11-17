import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/autenticacao/data/autenticacao_api.dart';
import 'armazenamento_sessao.dart';
import 'sessao.dart';

/// Por que a sessão terminou, para a tela de login avisar.
enum MotivoSaida { manual, expirada }

class EstadoSessao {
  const EstadoSessao({this.sessao, this.motivoSaida});

  final Sessao? sessao;
  final MotivoSaida? motivoSaida;
}

/// Fonte única da sessão. O router, o cliente HTTP e as telas leem daqui.
class ControladorSessao extends AsyncNotifier<EstadoSessao> {
  ArmazenamentoSessao get _armazenamento =>
      ref.read(armazenamentoSessaoProvider);

  @override
  Future<EstadoSessao> build() async =>
      EstadoSessao(sessao: await _armazenamento.ler());

  /// Lança a falha do login para a tela escolher a mensagem.
  Future<void> entrar(String email, String senha) async {
    final sessao = await ref
        .read(autenticacaoApiProvider)
        .autenticar(email, senha);
    await _armazenamento.guardar(sessao);
    state = AsyncData(EstadoSessao(sessao: sessao));
  }

  Future<void> sair() => _encerrar(MotivoSaida.manual);

  Future<void> expirar() => _encerrar(MotivoSaida.expirada);

  Future<void> _encerrar(MotivoSaida motivo) async {
    final email = state.value?.sessao?.email;
    // Várias chamadas podem receber 401 juntas; só a primeira encerra.
    if (email == null) return;
    state = AsyncData(EstadoSessao(motivoSaida: motivo));
    await _armazenamento.apagar();
    await ref.read(limpezaDaContaProvider)(email);
  }
}

final controladorSessaoProvider =
    AsyncNotifierProvider<ControladorSessao, EstadoSessao>(
      ControladorSessao.new,
    );
