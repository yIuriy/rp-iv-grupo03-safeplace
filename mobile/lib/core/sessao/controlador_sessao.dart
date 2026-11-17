import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'armazenamento_sessao.dart';
import 'sessao.dart';

/// Por que a sessão terminou, para a tela de login avisar.
enum MotivoSaida { manual, expirada }

class EstadoSessao {
  const EstadoSessao({this.sessao, this.motivoSaida});

  final Sessao? sessao;
  final MotivoSaida? motivoSaida;
}

/// Fonte única da sessão. O router, o cliente HTTP e as telas leem daqui. Não sabe como a sessão
/// é obtida: quem autentica (a feature de login) entrega a sessão pronta em [iniciar].
class ControladorSessao extends AsyncNotifier<EstadoSessao> {
  ArmazenamentoSessao get _armazenamento =>
      ref.read(armazenamentoSessaoProvider);

  @override
  Future<EstadoSessao> build() async =>
      EstadoSessao(sessao: await _armazenamento.ler());

  Future<void> iniciar(Sessao sessao) async {
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
