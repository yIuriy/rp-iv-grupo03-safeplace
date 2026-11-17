import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/api/erros.dart';
import '../../../core/sessao/controlador_sessao.dart';

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
  bool _senhaVisivel = false;

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
      await ref
          .read(controladorSessaoProvider.notifier)
          .entrar(_email.text.trim(), _senha.text);
      // O router troca de tela ao perceber a sessão nova.
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
    final tema = Theme.of(context);

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
                      Text(
                        'SafePlace',
                        textAlign: TextAlign.center,
                        style: tema.textTheme.headlineMedium?.copyWith(
                          color: tema.colorScheme.primary,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Entrar no SafePlace',
                        textAlign: TextAlign.center,
                        style: tema.textTheme.titleLarge,
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Acesso para Gestores de Segurança e Supervisores com o e-mail corporativo e a senha recebida.',
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 24),
                      if (_falha == null && motivo == MotivoSaida.expirada)
                        const _Aviso(
                          titulo: 'Sua sessão expirou',
                          texto:
                              'Entre novamente para continuar de onde parou.',
                        ),
                      if (_falha == null && motivo == MotivoSaida.manual)
                        const _Aviso(
                          titulo: 'Você saiu do SafePlace',
                          texto: 'Entre novamente quando precisar.',
                        ),
                      TextFormField(
                        controller: _email,
                        decoration: const InputDecoration(labelText: 'E-mail'),
                        keyboardType: TextInputType.emailAddress,
                        autofillHints: const [AutofillHints.username],
                        textInputAction: TextInputAction.next,
                        enabled: !_enviando,
                        validator: (v) => (v ?? '').trim().isEmpty
                            ? 'Informe o e-mail de acesso.'
                            : null,
                      ),
                      const SizedBox(height: 16),
                      TextFormField(
                        controller: _senha,
                        focusNode: _focoSenha,
                        obscureText: !_senhaVisivel,
                        autofillHints: const [AutofillHints.password],
                        textInputAction: TextInputAction.done,
                        enabled: !_enviando,
                        onFieldSubmitted: (_) => _enviar(),
                        decoration: InputDecoration(
                          labelText: 'Senha',
                          suffixIcon: IconButton(
                            tooltip: _senhaVisivel
                                ? 'Ocultar senha'
                                : 'Mostrar senha',
                            icon: Icon(
                              _senhaVisivel
                                  ? Icons.visibility_off
                                  : Icons.visibility,
                            ),
                            onPressed: () =>
                                setState(() => _senhaVisivel = !_senhaVisivel),
                          ),
                        ),
                        validator: (v) =>
                            (v ?? '').isEmpty ? 'Informe a senha.' : null,
                      ),
                      if (_falha != null) ...[
                        const SizedBox(height: 16),
                        Semantics(
                          liveRegion: true,
                          child: Text(
                            _falha!,
                            style: TextStyle(color: tema.colorScheme.error),
                          ),
                        ),
                      ],
                      const SizedBox(height: 24),
                      FilledButton(
                        onPressed: _enviando ? null : _enviar,
                        child: _enviando
                            ? const SizedBox.square(
                                dimension: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                ),
                              )
                            : const Text('Entrar'),
                      ),
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

class _Aviso extends StatelessWidget {
  const _Aviso({required this.titulo, required this.texto});

  final String titulo;
  final String texto;

  @override
  Widget build(BuildContext context) {
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
