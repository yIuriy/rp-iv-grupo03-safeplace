import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../shared/formularios/cpf.dart';
import '../../../shared/formularios/data.dart';
import '../../../shared/tema/cores_safeplace.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/aviso.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/dialogo_confirmacao.dart';
import '../data/usuario.dart';
import '../data/usuarios_api.dart';
import '../domain/cadastro_de_usuarios.dart';
import '../domain/validacao_pessoa.dart';

/// Resultado do formulário, para a lista avisar o que mudou.
typedef CadastroSalvo = ({Usuario usuario, bool criado});

/// Abre o formulário em tela cheia por cima da casca. Sem [usuario], cadastra; com ele, edita.
/// Devolve o que foi salvo, ou `null` se a pessoa desistiu.
Future<CadastroSalvo?> abrirFormularioPessoa(
  BuildContext context, {
  required TipoCadastro tipo,
  Usuario? usuario,
}) {
  return Navigator.of(context, rootNavigator: true).push<CadastroSalvo>(
    MaterialPageRoute(
      fullscreenDialog: true,
      builder: (_) => FormularioPessoaPage(tipo: tipo, usuario: usuario),
    ),
  );
}

/// Textos e regras de cada tipo de cadastro, os mesmos do `CadastroDialog` do web.
class _Configuracao {
  const _Configuracao({
    required this.titulo,
    required this.descricao,
    required this.tituloEdicao,
    required this.descricaoEdicao,
    required this.emailObrigatorio,
    required this.ajudaEmail,
    required this.rotuloEnviar,
  });

  final String titulo;
  final String descricao;
  final String tituloEdicao;
  final String descricaoEdicao;
  final bool emailObrigatorio;
  final String ajudaEmail;
  final String rotuloEnviar;
}

const _configuracoes = {
  TipoCadastro.supervisor: _Configuracao(
    titulo: 'Novo supervisor',
    descricao: 'O Supervisor recebe conta de acesso. A senha inicial é gerada pelo sistema e exibida uma única vez, ao final do cadastro.',
    tituloEdicao: 'Editar supervisor',
    descricaoEdicao: 'Altere nome, data de nascimento ou e-mail de acesso. CPF, perfil e senha não mudam por aqui.',
    emailObrigatorio: true,
    ajudaEmail: 'Identificação de acesso usada no login.',
    rotuloEnviar: 'Cadastrar supervisor',
  ),
  TipoCadastro.colaborador: _Configuracao(
    titulo: 'Novo colaborador',
    descricao: 'O Colaborador é cadastrado para vínculo em ocorrências, capacitações e empréstimos de EPIs. Não recebe conta, senha nem acesso ao sistema.',
    tituloEdicao: 'Editar colaborador',
    descricaoEdicao: 'Altere nome, data de nascimento ou e-mail de contato. O Colaborador continua sem conta de acesso e o CPF não muda por aqui.',
    emailObrigatorio: false,
    ajudaEmail: 'Contato opcional. O Colaborador não usa o sistema.',
    rotuloEnviar: 'Cadastrar colaborador',
  ),
};

/// Falha geral do formulário, quando o erro não é de um campo só. Os dados ficam preenchidos.
final descreverFalhaDeCadastro = criarDescritorDeFalha(
  porStatus: {
    400: usarMensagemDaApi(
      'Os dados informados são inválidos. Confira os campos.',
    ),
    403: fixa('Seu perfil não tem permissão para esta operação.'),
    404: fixa(
      'Cadastro não encontrado. Ele pode ter sido alterado por outra pessoa; volte e recarregue a lista.',
    ),
    409: usarMensagemDaApi(
      'Os dados informados conflitam com um cadastro existente.',
    ),
  },
  padrao: 'Não foi possível concluir a operação agora. Os dados preenchidos foram mantidos; tente novamente.',
);

/// Cadastro e edição de Supervisor ou Colaborador (RF23). Na edição, o CPF aparece somente leitura
/// e não vai no corpo; no cadastro de Supervisor, a senha inicial aparece uma única vez no fim.
class FormularioPessoaPage extends ConsumerStatefulWidget {
  const FormularioPessoaPage({super.key, required this.tipo, this.usuario});

  final TipoCadastro tipo;
  final Usuario? usuario;

  @override
  ConsumerState<FormularioPessoaPage> createState() =>
      _FormularioPessoaPageState();
}

class _FormularioPessoaPageState extends ConsumerState<FormularioPessoaPage> {
  final _form = GlobalKey<FormState>();
  late final TextEditingController _nome;
  late final TextEditingController _cpf;
  late final TextEditingController _data;
  late final TextEditingController _email;
  final _focos = {for (final campo in CampoPessoa.values) campo: FocusNode()};

  Map<CampoPessoa, String> _errosDoServidor = const {};
  bool _temPendencias = false;
  String? _falha;
  bool _enviando = false;
  bool _alterado = false;
  UsuarioCriado? _supervisorCriado;

  _Configuracao get _config => _configuracoes[widget.tipo]!;
  bool get _editando => widget.usuario != null;

  @override
  void initState() {
    super.initState();
    final usuario = widget.usuario;
    final nascimento = usuario?.dataNascimento;
    _nome = TextEditingController(text: usuario?.nome);
    _cpf = TextEditingController(text: mascararCpf(usuario?.cpf ?? ''));
    _data = TextEditingController(
      text: nascimento == null ? '' : formatarData(nascimento),
    );
    _email = TextEditingController(text: usuario?.email);
  }

  @override
  void dispose() {
    for (final controlador in [_nome, _cpf, _data, _email]) {
      controlador.dispose();
    }
    for (final foco in _focos.values) {
      foco.dispose();
    }
    super.dispose();
  }

  /// Mesmas regras dos validadores dos campos, para achar o primeiro com erro.
  Map<CampoPessoa, String?> _errosLocais() => {
    CampoPessoa.nome: validarNome(_nome.text),
    CampoPessoa.cpf: _editando ? null : validarCpf(_cpf.text),
    CampoPessoa.dataNascimento: validarDataNascimento(_data.text),
    CampoPessoa.email: validarEmail(
      _email.text,
      obrigatorio: _config.emailObrigatorio,
    ),
  };

  void _focarPrimeiro(Iterable<CampoPessoa> comErro) {
    for (final campo in CampoPessoa.values) {
      if (comErro.contains(campo)) {
        _focos[campo]!.requestFocus();
        return;
      }
    }
  }

  /// O `CampoTexto` apaga o erro do servidor quando a pessoa edita o campo; o formulário esquece
  /// junto, senão o mesmo erro, devolvido de novo, não chegaria ao campo (o valor não mudaria).
  void _editou(CampoPessoa campo) {
    if (!_errosDoServidor.containsKey(campo)) return;
    setState(
      () => _errosDoServidor = {
        for (final MapEntry(:key, :value) in _errosDoServidor.entries)
          if (key != campo) key: value,
      },
    );
  }

  Future<void> _enviar() async {
    if (_enviando) return;
    setState(() => _falha = null);
    final valido = _form.currentState!.validate();
    if (!valido) {
      setState(() => _temPendencias = true);
      _focarPrimeiro([
        for (final MapEntry(:key, :value) in _errosLocais().entries)
          if (value != null) key,
      ]);
      return;
    }
    setState(() {
      _temPendencias = false;
      _enviando = true;
    });

    final email = _email.text.trim();
    final dados = DadosPessoa(
      nome: _nome.text.trim(),
      cpf: _editando ? (widget.usuario!.cpf ?? '') : digitosDoCpf(_cpf.text),
      dataNascimento: lerData(_data.text)!,
      email: email.isEmpty ? null : email,
    );

    try {
      final salvo = await _salvar(dados);
      if (!mounted) return;
      // As listas do módulo buscam de novo na próxima leitura.
      ref.invalidate(supervisoresProvider);
      ref.invalidate(colaboradoresProvider);
      if (salvo is UsuarioCriado) {
        setState(() {
          _enviando = false;
          _supervisorCriado = salvo;
        });
        return;
      }
      Navigator.of(context)
          .pop<CadastroSalvo>((usuario: salvo as Usuario, criado: !_editando));
    } catch (erro) {
      if (!mounted) return;
      final doCampo = erroDeCampo(erro);
      setState(() {
        _enviando = false;
        if (doCampo != null) {
          _errosDoServidor = {doCampo.campo: doCampo.mensagem};
          _temPendencias = true;
        } else {
          _falha = descreverFalhaDeCadastro(erro);
        }
      });
      if (doCampo != null) _focarPrimeiro([doCampo.campo]);
    }
  }

  /// Supervisor cadastrado devolve [UsuarioCriado], para mostrar a senha; o resto, [Usuario].
  Future<Object> _salvar(DadosPessoa dados) {
    final cadastro = ref.read(cadastroDeUsuariosProvider);
    final id = widget.usuario?.id;
    return switch ((widget.tipo, id)) {
      (TipoCadastro.supervisor, null) => cadastro.cadastrarSupervisor(dados),
      (TipoCadastro.colaborador, null) => cadastro.cadastrarColaborador(dados),
      (TipoCadastro.supervisor, final int id) => cadastro.atualizarSupervisor(
        id,
        dados,
      ),
      (TipoCadastro.colaborador, final int id) => cadastro.atualizarColaborador(
        id,
        dados,
      ),
    };
  }

  /// Sair com dados digitados pede confirmação, como o `ConfirmDialog` do web.
  Future<void> _sairSemSalvar() async {
    final descartar = await confirmar(
      context,
      titulo: _editando ? 'Descartar as alterações?' : 'Descartar o cadastro?',
      descricao: 'Os dados preenchidos neste formulário serão perdidos.',
      rotuloConfirmar: 'Descartar',
    );
    if (descartar && mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final criado = _supervisorCriado;
    if (criado != null) {
      return _SenhaInicialPage(
        criado: criado,
        aoConcluir: () =>
            Navigator.of(context)
                .pop<CadastroSalvo>((usuario: criado.usuario, criado: true)),
      );
    }

    return PopScope(
      canPop: !_alterado && !_enviando,
      onPopInvokedWithResult: (saiu, _) {
        if (!saiu && !_enviando) _sairSemSalvar();
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(_editando ? _config.tituloEdicao : _config.titulo),
        ),
        body: SafeArea(
          child: Form(
            key: _form,
            onChanged: () {
              if (!_alterado) setState(() => _alterado = true);
            },
            child: ListView(
              padding: const EdgeInsets.all(
                TokensDimensoes.layoutMobilePadding,
              ),
              children: [
                Text(
                  _editando ? _config.descricaoEdicao : _config.descricao,
                  style: Theme.of(context).textTheme.bodyMedium
                      ?.copyWith(color: context.cores.textoSecundario),
                ),
                const SizedBox(height: TokensDimensoes.space24),
                ..._campos(),
                if (_temPendencias) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  const Aviso(
                    tom: TomAviso.perigo,
                    anunciar: true,
                    titulo: 'Confira os campos destacados',
                    mensagem: 'Os dados preenchidos foram mantidos. Corrija os campos indicados e envie novamente.',
                  ),
                ],
                if (_falha != null) ...[
                  const SizedBox(height: TokensDimensoes.space16),
                  Aviso(
                    tom: TomAviso.perigo,
                    anunciar: true,
                    titulo: _editando
                        ? 'Não foi possível salvar as alterações'
                        : 'Não foi possível concluir o cadastro',
                    mensagem: _falha,
                  ),
                ],
                const SizedBox(height: TokensDimensoes.space24),
                FilledButton.icon(
                  onPressed: _enviando ? null : _enviar,
                  icon: _enviando
                      ? const SizedBox.square(
                          dimension: TokensDimensoes.sizeIcon,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.check),
                  label: Text(
                    _editando ? 'Salvar alterações' : _config.rotuloEnviar,
                  ),
                ),
                const SizedBox(height: TokensDimensoes.space8),
                TextButton(
                  onPressed: _enviando
                      ? null
                      : () => Navigator.of(context).maybePop(),
                  child: const Text('Cancelar'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  List<Widget> _campos() {
    const espaco = SizedBox(height: TokensDimensoes.space16);
    return [
      CampoTexto(
        rotulo: 'Nome completo',
        controlador: _nome,
        foco: _focos[CampoPessoa.nome],
        habilitado: !_enviando,
        validador: validarNome,
        erroDoServidor: _errosDoServidor[CampoPessoa.nome],
        aoMudar: (_) => _editou(CampoPessoa.nome),
        acaoTeclado: TextInputAction.next,
        dicasAutopreenchimento: const [AutofillHints.name],
      ),
      espaco,
      CampoTexto(
        rotulo: 'CPF',
        controlador: _cpf,
        foco: _focos[CampoPessoa.cpf],
        habilitado: !_enviando,
        somenteLeitura: _editando,
        ajuda: _editando
            ? 'O CPF identifica o cadastro e não é alterado nesta operação.'
            : 'Digite somente os números; a formatação é automática.',
        dica: '000.000.000-00',
        teclado: TextInputType.number,
        formatadores: const [MascaraCpf()],
        validador: _editando ? null : validarCpf,
        erroDoServidor: _errosDoServidor[CampoPessoa.cpf],
        aoMudar: (_) => _editou(CampoPessoa.cpf),
        acaoTeclado: TextInputAction.next,
      ),
      espaco,
      CampoTexto(
        rotulo: 'Data de nascimento',
        controlador: _data,
        foco: _focos[CampoPessoa.dataNascimento],
        habilitado: !_enviando,
        ajuda: 'No formato dd/mm/aaaa.',
        dica: 'dd/mm/aaaa',
        teclado: TextInputType.datetime,
        formatadores: [
          FilteringTextInputFormatter.allow(RegExp(r'[\d/]')),
          const MascaraData(),
        ],
        validador: validarDataNascimento,
        erroDoServidor: _errosDoServidor[CampoPessoa.dataNascimento],
        aoMudar: (_) => _editou(CampoPessoa.dataNascimento),
        acaoTeclado: TextInputAction.next,
      ),
      espaco,
      CampoTexto(
        rotulo: _config.emailObrigatorio
            ? 'E-mail corporativo'
            : 'E-mail de contato (opcional)',
        controlador: _email,
        foco: _focos[CampoPessoa.email],
        habilitado: !_enviando,
        ajuda: _config.ajudaEmail,
        teclado: TextInputType.emailAddress,
        validador: (valor) =>
            validarEmail(valor, obrigatorio: _config.emailObrigatorio),
        erroDoServidor: _errosDoServidor[CampoPessoa.email],
        aoMudar: (_) => _editou(CampoPessoa.email),
        acaoTeclado: TextInputAction.done,
        aoEnviar: (_) => _enviar(),
        dicasAutopreenchimento: const [AutofillHints.email],
      ),
    ];
  }
}

/// Passo final do cadastro de Supervisor: única vez em que a senha inicial aparece (RF23, #91).
/// Ela não é guardada em lugar nenhum do app; sair desta tela a descarta.
class _SenhaInicialPage extends StatelessWidget {
  const _SenhaInicialPage({required this.criado, required this.aoConcluir});

  final UsuarioCriado criado;
  final VoidCallback aoConcluir;

  @override
  Widget build(BuildContext context) {
    final usuario = criado.usuario;
    final senha = criado.senhaInicial;
    final textos = Theme.of(context).textTheme;

    return PopScope(
      canPop: false,
      // Voltar pelo sistema conclui, para a lista saber do cadastro.
      onPopInvokedWithResult: (saiu, _) {
        if (!saiu) aoConcluir();
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Supervisor cadastrado'),
          automaticallyImplyLeading: false,
        ),
        body: SafeArea(
          child: ListView(
            padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
            children: [
              Aviso(
                tom: TomAviso.sucesso,
                anunciar: true,
                titulo: 'Cadastro concluído',
                mensagem:
                    '${usuario.nome} já pode entrar no SafePlace com o e-mail ${usuario.email ?? 'cadastrado'}.',
              ),
              const SizedBox(height: TokensDimensoes.space16),
              if (senha != null)
                Card(
                  margin: EdgeInsets.zero,
                  child: Padding(
                    padding: const EdgeInsets.all(TokensDimensoes.space16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Senha inicial',
                          style: textos.labelLarge?.copyWith(
                            color: context.cores.textoSecundario,
                          ),
                        ),
                        const SizedBox(height: TokensDimensoes.space4),
                        SelectableText(
                          senha,
                          style: textos.headlineSmall?.copyWith(
                            fontFamily: 'monospace',
                            letterSpacing: 1,
                          ),
                        ),
                        const SizedBox(height: TokensDimensoes.space8),
                        OutlinedButton.icon(
                          onPressed: () async {
                            await Clipboard.setData(ClipboardData(text: senha));
                            if (!context.mounted) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Senha inicial copiada.'),
                              ),
                            );
                          },
                          icon: const Icon(Icons.copy_outlined),
                          label: const Text('Copiar senha'),
                        ),
                      ],
                    ),
                  ),
                )
              else
                const Aviso(
                  tom: TomAviso.alerta,
                  titulo: 'Senha inicial não recebida',
                  mensagem: 'A API não devolveu a senha inicial. Confira o cadastro no servidor antes de comunicar o acesso.',
                ),
              const SizedBox(height: TokensDimensoes.space16),
              Text(
                'Anote e entregue esta senha ao Supervisor agora. Por segurança, ela não é exibida novamente nem aparece em consultas.',
                style: textos.bodyMedium,
              ),
              const SizedBox(height: TokensDimensoes.space24),
              FilledButton(
                onPressed: aoConcluir,
                child: const Text('Concluir'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
