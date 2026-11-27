// Operações do Gestor sobre uma ocorrência (#321), no padrão Command (GoF): cada operação é um
// objeto que sabe o próprio rótulo, a confirmação que pede, a mensagem de sucesso e como se
// executar. A tela não conhece nenhuma delas em particular: recebe a lista do estado atual
// (`EstadoOcorrencia.operacoes`) e executa qualquer uma pelo mesmo caminho.

import '../data/ocorrencia.dart';
import 'ocorrencia_repository.dart';

/// Texto do diálogo de confirmação, quando a operação pede um.
typedef Confirmacao = ({
  String titulo,
  String descricao,
  String rotuloConfirmar,
});

/// Command: uma operação que muda a ocorrência no servidor e devolve o registro atualizado.
sealed class OperacaoOcorrencia {
  const OperacaoOcorrencia();

  String get rotulo;

  /// Operação que encerra algo (arquivar): o botão usa o estilo Danger do design system.
  bool get destrutiva => false;

  /// Nulo quando a operação é direta; senão, o diálogo pede confirmação antes de executar.
  Confirmacao? get confirmacao;

  Future<Ocorrencia> executar(OcorrenciaRepository repositorio, int id);

  String mensagemDeSucesso(Ocorrencia atualizada);
}

/// ABERTA → EM_TRIAGEM (`PATCH /api/ocorrencias/{id}/triagem`).
final class EnviarParaTriagem extends OperacaoOcorrencia {
  const EnviarParaTriagem();

  @override
  String get rotulo => 'Enviar para triagem';

  @override
  Confirmacao get confirmacao => (
    titulo: 'Enviar para triagem?',
    descricao: 'A ocorrência passa a ser analisada. Ela continua editável e pode receber ações no plano.',
    rotuloConfirmar: 'Enviar para triagem',
  );

  @override
  Future<Ocorrencia> executar(OcorrenciaRepository repositorio, int id) =>
      repositorio.enviarParaTriagem(id);

  @override
  String mensagemDeSucesso(Ocorrencia atualizada) =>
      'Ocorrência enviada para triagem.';
}

/// EM_TRIAGEM → ARQUIVADA (`PATCH /api/ocorrencias/{id}/arquivar`). Não há exclusão de registro.
final class Arquivar extends OperacaoOcorrencia {
  const Arquivar();

  @override
  String get rotulo => 'Arquivar';

  @override
  bool get destrutiva => true;

  @override
  Confirmacao get confirmacao => (
    titulo: 'Arquivar ocorrência?',
    descricao: 'Arquivar encerra o acompanhamento desta ocorrência. O registro e o plano de ação são preservados, sem exclusão, mas não podem mais ser editados.',
    rotuloConfirmar: 'Confirmar arquivamento',
  );

  @override
  Future<Ocorrencia> executar(OcorrenciaRepository repositorio, int id) =>
      repositorio.arquivar(id);

  @override
  String mensagemDeSucesso(Ocorrencia atualizada) => 'Ocorrência arquivada.';
}

/// Emite o protocolo CAT de um acidente que ainda não tem (`PATCH /api/ocorrencias/{id}/consolidar-cat`).
/// O backend recusa um segundo protocolo: "Protocolo CAT já emitido e não pode ser alterado."
final class ConsolidarCat extends OperacaoOcorrencia {
  const ConsolidarCat();

  @override
  String get rotulo => 'Emitir protocolo CAT';

  @override
  Confirmacao get confirmacao => (
    titulo: 'Emitir protocolo CAT?',
    descricao: 'O servidor gera o número da Comunicação de Acidente de Trabalho a partir do mês do fato. Depois de emitido, o protocolo não muda.',
    rotuloConfirmar: 'Emitir protocolo',
  );

  @override
  Future<Ocorrencia> executar(OcorrenciaRepository repositorio, int id) =>
      repositorio.consolidarCat(id);

  @override
  String mensagemDeSucesso(Ocorrencia atualizada) =>
      switch (atualizada.protocoloCat) {
        final protocolo? => 'Protocolo CAT $protocolo emitido.',
        null => 'Protocolo CAT emitido.',
      };
}
