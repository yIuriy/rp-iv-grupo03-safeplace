// Validação local do formulário de pessoa, portada de `frontend/src/features/usuarios/validacaoPessoa.ts`
// com as mesmas mensagens: nome obrigatório, CPF válido (`validarCpf`, de `shared/formularios/cpf.dart`),
// data de nascimento no passado e e-mail (obrigatório para o Supervisor, que entra com ele; opcional
// para o Colaborador). O backend confere de novo; aqui é para a pessoa corrigir antes de enviar.

import '../../../core/api/api_client.dart';
import '../../../core/api/erros.dart';
import '../../../shared/formularios/data.dart';

/// Campos do formulário, na ordem da tela: o foco vai para o primeiro com erro.
enum CampoPessoa { nome, cpf, dataNascimento, email }

String? validarNome(String? valor) =>
    (valor ?? '').trim().isEmpty ? 'Informe o nome completo.' : null;

/// `dd/mm/aaaa`, estritamente antes de hoje, como o `@Past` do backend.
String? validarDataNascimento(String? valor, {DateTime? agora}) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) return 'Informe a data de nascimento.';
  final data = lerData(digitado);
  if (data == null) return 'Informe uma data válida, no formato dd/mm/aaaa.';
  if (!data.isBefore(hoje(agora))) {
    return 'A data de nascimento deve estar no passado.';
  }
  return null;
}

/// Mesmo padrão do `z.email()` usado pelo web: parte local sem ponto no início, no fim ou repetido,
/// e domínio com pelo menos um ponto e terminação de duas letras ou mais.
final _email = RegExp(
  r"^(?!\.)(?!.*\.\.)([A-Za-z0-9_'+\-.]*)[A-Za-z0-9_+\-]@([A-Za-z0-9][A-Za-z0-9\-]*\.)+[A-Za-z]{2,}$",
);

String? validarEmail(String? valor, {required bool obrigatorio}) {
  final digitado = (valor ?? '').trim();
  if (digitado.isEmpty) {
    return obrigatorio ? 'Informe o e-mail corporativo usado no login.' : null;
  }
  return _email.hasMatch(digitado) ? null : 'Informe um e-mail válido.';
}

/// Erro de um campo apontado pelo servidor.
typedef ErroDeCampo = ({CampoPessoa campo, String mensagem});

/// Leva ao campo certo o que só o backend sabe (critério de aceite da #317):
/// - 409 do `IdentificacaoDisponivelElo`: "CPF já cadastrado..." vai para o CPF e "Email já
///   cadastrado..." para o e-mail;
/// - 400 do Bean Validation, no formato `campo: mensagem` (`ApiExceptionHandler.validacaoFalhou`);
/// - 400 "CPF Inválido" do `CpfValidador`.
///
/// Qualquer outra falha devolve `null` e aparece no aviso geral do formulário.
ErroDeCampo? erroDeCampo(Object erro) {
  if (erro is! FalhaApi) return null;
  final mensagem = mensagemDaApi(erro)?.trim();
  if (mensagem == null) return null;
  final minusculas = mensagem.toLowerCase();

  if (erro.status == 409) {
    if (minusculas.startsWith('cpf')) {
      return (
        campo: CampoPessoa.cpf,
        mensagem: 'Já existe um cadastro com este CPF.',
      );
    }
    if (minusculas.startsWith('email') || minusculas.startsWith('e-mail')) {
      return (
        campo: CampoPessoa.email,
        mensagem: 'Este e-mail já está em uso por outro cadastro.',
      );
    }
    return null;
  }

  if (erro.status == 400) {
    final porCampo = RegExp(
      r'^(\w+):\s*(.+)$',
      dotAll: true,
    ).firstMatch(mensagem);
    if (porCampo != null) {
      final campo = _campoDaApi[porCampo[1]];
      return campo == null ? null : (campo: campo, mensagem: porCampo[2]!);
    }
    if (corpoDoErro(erro)?.error == 'CPF Inválido') {
      return (campo: CampoPessoa.cpf, mensagem: mensagem);
    }
  }
  return null;
}

const _campoDaApi = {
  'nome': CampoPessoa.nome,
  'cpf': CampoPessoa.cpf,
  'dataNascimento': CampoPessoa.dataNascimento,
  'email': CampoPessoa.email,
};
