import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../core/sessao/controlador_sessao.dart';
import '../data/autenticacao_api.dart';

final _descreverFalha = criarDescritorDeFalha(
  porStatus: {
    401: fixa(
      'E-mail ou senha incorretos. Confira os dados e tente novamente.',
    ),
  },
  padrao: 'Não foi possível entrar agora. Tente novamente.',
);

class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  final _form = GlobalKey<FormState>();
  final _email = TextEditingController();
  final _senha = TextEditingController();
  final _focoSenha = FocusNode();
  String? _falha;
  bool _enviando = false;

  @override
  void dispose() {
    _email.dispose();
    _senha.dispose();
    _focoSenha.dispose();
    super.dispose();
  }

  Future<void> _enviar() async {
    if (_enviando || !_form.currentState!.validate()) return;
    setState(() {
      _falha = null;
      _enviando = true;
    });
    try {
      final sessao = await ref
          .read(autenticadorProvider)
          .autenticar(_email.text.trim(), _senha.text);
      // O router troca de tela ao perceber a sessão nova.
      await ref.read(controladorSessaoProvider.notifier).iniciar(sessao);
    } catch (erro) {
      if (!mounted) return;
      setState(() {
        _falha = _descreverFalha(erro);
        _enviando = false;
      });
      _focoSenha.requestFocus();
    }
  }

  @override
  Widget build(BuildContext context) {
    final motivo = ref.watch(controladorSessaoProvider).value?.motivoSaida;

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Form(
                key: _form,
                child: AutofillGroup(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const _Cabecalho(),
                      const SizedBox(height: 24),
                      if (_falha == null && motivo != null)
                        _AvisoDeSaida(motivo),
                      _CampoEmail(_email, habilitado: !_enviando),
                      const SizedBox(height: 16),
                      _CampoSenha(
                        _senha,
                        foco: _focoSenha,
                        habilitado: !_enviando,
                        aoConfirmar: _enviar,
                      ),
                      if (_falha != null) _MensagemDeFalha(_falha!),
                      const SizedBox(height: 24),
                      _BotaoEntrar(enviando: _enviando, aoTocar: _enviar),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _Cabecalho extends StatelessWidget {
  const _Cabecalho();

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context);
    return Column(
      children: [
        Text(
          'SafePlace',
          style: tema.textTheme.headlineMedium?.copyWith(
            color: tema.colorScheme.primary,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 8),
        Text('Entrar no SafePlace', style: tema.textTheme.titleLarge),
        const SizedBox(height: 8),
        const Text(
          'Acesso para Gestores de Segurança e Supervisores com o e-mail corporativo e a senha recebida.',
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
}

class _AvisoDeSaida extends StatelessWidget {
  const _AvisoDeSaida(this.motivo);

  final MotivoSaida motivo;

  @override
  Widget build(BuildContext context) {
    final (titulo, texto) = switch (motivo) {
      MotivoSaida.expirada => (
        'Sua sessão expirou',
        'Entre novamente para continuar de onde parou.',
      ),
      MotivoSaida.manual => (
        'Você saiu do SafePlace',
        'Entre novamente quando precisar.',
      ),
    };
    final tema = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Semantics(
        liveRegion: true,
        child: Card(
          color: tema.colorScheme.secondaryContainer,
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(titulo, style: tema.textTheme.titleSmall),
                Text(texto),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _CampoEmail extends StatelessWidget {
  const _CampoEmail(this.controlador, {required this.habilitado});

  final TextEditingController controlador;
  final bool habilitado;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controlador,
      decoration: const InputDecoration(labelText: 'E-mail'),
      keyboardType: TextInputType.emailAddress,
      autofillHints: const [AutofillHints.username],
      textInputAction: TextInputAction.next,
      enabled: habilitado,
      validator: (v) =>
          (v ?? '').trim().isEmpty ? 'Informe o e-mail de acesso.' : null,
    );
  }
}

class _CampoSenha extends StatefulWidget {
  const _CampoSenha(
    this.controlador, {
    required this.foco,
    required this.habilitado,
    required this.aoConfirmar,
  });

  final TextEditingController controlador;
  final FocusNode foco;
  final bool habilitado;
  final VoidCallback aoConfirmar;

  @override
  State<_CampoSenha> createState() => _CampoSenhaState();
}

class _CampoSenhaState extends State<_CampoSenha> {
  bool _visivel = false;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: widget.controlador,
      focusNode: widget.foco,
      obscureText: !_visivel,
      autofillHints: const [AutofillHints.password],
      textInputAction: TextInputAction.done,
      enabled: widget.habilitado,
      onFieldSubmitted: (_) => widget.aoConfirmar(),
      decoration: InputDecoration(
        labelText: 'Senha',
        suffixIcon: IconButton(
          tooltip: _visivel ? 'Ocultar senha' : 'Mostrar senha',
          icon: Icon(_visivel ? Icons.visibility_off : Icons.visibility),
          onPressed: () => setState(() => _visivel = !_visivel),
        ),
      ),
      validator: (v) => (v ?? '').isEmpty ? 'Informe a senha.' : null,
    );
  }
}

class _MensagemDeFalha extends StatelessWidget {
  const _MensagemDeFalha(this.mensagem);

  final String mensagem;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 16),
      child: Semantics(
        liveRegion: true,
        child: Text(
          mensagem,
          style: TextStyle(color: Theme.of(context).colorScheme.error),
        ),
      ),
    );
  }
}

class _BotaoEntrar extends StatelessWidget {
  const _BotaoEntrar({required this.enviando, required this.aoTocar});

  final bool enviando;
  final VoidCallback aoTocar;

  @override
  Widget build(BuildContext context) {
    return FilledButton(
      onPressed: enviando ? null : aoTocar,
      child: enviando
          ? const SizedBox.square(
              dimension: 20,
              child: CircularProgressIndicator(strokeWidth: 2),
            )
          : const Text('Entrar'),
    );
  }
}
