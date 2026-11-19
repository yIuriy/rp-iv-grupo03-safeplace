import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import '../../../core/sessao/controlador_sessao.dart';
import '../../../shared/formularios/cpf.dart';
import '../../../shared/offline/conteudo_com_cache.dart';
import '../../../shared/tema/tokens.g.dart';
import '../../../shared/widgets/campos.dart';
import '../../../shared/widgets/estados.dart';
import '../data/usuario.dart';
import '../data/usuarios_api.dart';
import '../domain/cadastro_de_usuarios.dart';
import 'componentes.dart';
import 'formulario_pessoa_page.dart';

/// Tela `/usuarios` (RF23), com a mesma matriz do `SecurityConfig`: o Gestor vê as abas
/// Supervisores e Colaboradores; o Supervisor vê só os colaboradores (o menu diz "Colaboradores")
/// e não tem como listar nem cadastrar supervisores.
class UsuariosPage extends ConsumerWidget {
  const UsuariosPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessao = ref.watch(controladorSessaoProvider).value?.sessao;
    if (sessao == null) return const SizedBox.shrink();
    if (!sessao.ehGestor) return const ColaboradoresPainel();

    return DefaultTabController(
      length: 2,
      child: Column(
        children: [
          Material(
            color: Theme.of(context).colorScheme.surface,
            child: const TabBar(
              tabs: [
                Tab(text: 'Supervisores'),
                Tab(text: 'Colaboradores'),
              ],
            ),
          ),
          const Expanded(
            child: TabBarView(
              children: [SupervisoresPainel(), ColaboradoresPainel()],
            ),
          ),
        ],
      ),
    );
  }
}

/// Avisa o que foi salvo, como o `Toast` do web.
void _avisarSalvo(BuildContext context, CadastroSalvo? salvo, String sufixo) {
  if (salvo == null || !context.mounted) return;
  final nome = salvo.usuario.nome;
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Text(salvo.criado ? '$nome $sufixo' : '$nome atualizado.'),
    ),
  );
}

/// Supervisores, só do Gestor: lista, cadastro com senha inicial e edição.
class SupervisoresPainel extends ConsumerWidget {
  const SupervisoresPainel({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    Future<void> abrir([Usuario? usuario]) async {
      final salvo = await abrirFormularioPessoa(
        context,
        tipo: TipoCadastro.supervisor,
        usuario: usuario,
      );
      if (context.mounted) _avisarSalvo(context, salvo, 'cadastrado.');
    }

    return RefreshIndicator(
      onRefresh: () => ref
          .refresh(supervisoresProvider.future)
          .then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          ConteudoComCache<List<Usuario>>(
            valor: ref.watch(supervisoresProvider),
            carregando: 'Carregando supervisores...',
            falha: 'Não foi possível carregar os supervisores.',
            descreverFalha: descreverFalhaDeConsulta,
            aoTentarDeNovo: () => ref.invalidate(supervisoresProvider),
            construir: (supervisores, somenteLeitura) => ListaDeCadastros(
              descricao: 'Supervisores têm conta de acesso e gerenciam os colaboradores.',
              rotuloCadastrar: 'Novo supervisor',
              aoCadastrar: abrir,
              usuarios: supervisores,
              somenteLeitura: somenteLeitura,
              aoEditar: abrir,
              vazio: const EstadoVazio(
                icone: Icons.badge_outlined,
                titulo: 'Nenhum supervisor cadastrado',
                descricao: 'Cadastre o primeiro supervisor para que ele possa acessar o SafePlace.',
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Colaboradores, do Gestor e do Supervisor: busca por nome ou CPF no servidor, lista, cadastro e
/// edição. O Colaborador não tem conta de acesso.
class ColaboradoresPainel extends ConsumerStatefulWidget {
  const ColaboradoresPainel({super.key});

  @override
  ConsumerState<ColaboradoresPainel> createState() =>
      _ColaboradoresPainelState();
}

class _ColaboradoresPainelState extends ConsumerState<ColaboradoresPainel> {
  final _busca = GlobalKey<FormState>();
  final _nome = TextEditingController();
  final _cpf = TextEditingController();
  FiltroColaboradores _filtro = FiltroColaboradores.nenhum;

  @override
  void dispose() {
    _nome.dispose();
    _cpf.dispose();
    super.dispose();
  }

  void _buscar() {
    if (!_busca.currentState!.validate()) return;
    FocusScope.of(context).unfocus();
    setState(
      () => _filtro = FiltroColaboradores(nome: _nome.text, cpf: _cpf.text),
    );
  }

  void _limpar() {
    _nome.clear();
    _cpf.clear();
    _busca.currentState!.reset();
    setState(() => _filtro = FiltroColaboradores.nenhum);
  }

  Future<void> _abrir([Usuario? usuario]) async {
    final salvo = await abrirFormularioPessoa(
      context,
      tipo: TipoCadastro.colaborador,
      usuario: usuario,
    );
    if (!mounted) return;
    _avisarSalvo(context, salvo, 'cadastrado sem conta de acesso.');
  }

  @override
  Widget build(BuildContext context) {
    final consulta = colaboradoresProvider(_filtro);
    return RefreshIndicator(
      onRefresh: () =>
          ref.refresh(consulta.future).then((_) {}, onError: (_) {}),
      child: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: [
          _FormularioBusca(
            chave: _busca,
            nome: _nome,
            cpf: _cpf,
            aoBuscar: _buscar,
            aoLimpar: _limpar,
          ),
          const SizedBox(height: TokensDimensoes.space24),
          ConteudoComCache<List<Usuario>>(
            valor: ref.watch(consulta),
            carregando: 'Carregando colaboradores...',
            falha: 'Não foi possível carregar os colaboradores.',
            descreverFalha: descreverFalhaDeConsulta,
            aoTentarDeNovo: () => ref.invalidate(consulta),
            construir: (colaboradores, somenteLeitura) => ListaDeCadastros(
              descricao: 'Colaboradores são vinculados a ocorrências, capacitações e empréstimos. Não recebem conta nem senha.',
              rotuloCadastrar: 'Novo colaborador',
              aoCadastrar: _abrir,
              usuarios: colaboradores,
              somenteLeitura: somenteLeitura,
              aoEditar: _abrir,
              vazio: _filtro.vazio
                  ? const EstadoVazio(
                      icone: Icons.group_outlined,
                      titulo: 'Nenhum colaborador cadastrado',
                      descricao: 'Cadastre o primeiro colaborador para vinculá-lo aos registros do sistema.',
                    )
                  : EstadoVazio(
                      titulo: 'Nenhum colaborador encontrado',
                      descricao: 'Nenhum cadastro corresponde ao nome ou CPF informados. Ajuste a busca ou limpe os filtros.',
                      rotuloAcao: 'Limpar filtros',
                      aoAgir: _limpar,
                    ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Busca de colaboradores (`?nome=&cpf=`). O backend compara o CPF inteiro, então a busca por CPF
/// pede os 11 dígitos em vez de devolver uma lista vazia sem explicar.
class _FormularioBusca extends StatelessWidget {
  const _FormularioBusca({
    required this.chave,
    required this.nome,
    required this.cpf,
    required this.aoBuscar,
    required this.aoLimpar,
  });

  final GlobalKey<FormState> chave;
  final TextEditingController nome;
  final TextEditingController cpf;
  final VoidCallback aoBuscar;
  final VoidCallback aoLimpar;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      container: true,
      label: 'Buscar colaboradores',
      child: Form(
        key: chave,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            CampoTexto(
              rotulo: 'Nome',
              controlador: nome,
              dica: 'Parte do nome',
              acaoTeclado: TextInputAction.search,
              aoEnviar: (_) => aoBuscar(),
            ),
            const SizedBox(height: TokensDimensoes.space12),
            CampoTexto(
              rotulo: 'CPF',
              controlador: cpf,
              dica: '000.000.000-00',
              teclado: TextInputType.number,
              formatadores: const [MascaraCpf()],
              acaoTeclado: TextInputAction.search,
              aoEnviar: (_) => aoBuscar(),
              validador: (valor) {
                final digitos = digitosDoCpf(valor ?? '');
                return digitos.isEmpty || digitos.length == 11
                    ? null
                    : 'Para buscar por CPF, informe os 11 dígitos.';
              },
            ),
            const SizedBox(height: TokensDimensoes.space12),
            Wrap(
              spacing: TokensDimensoes.space8,
              runSpacing: TokensDimensoes.space8,
              children: [
                OutlinedButton.icon(
                  onPressed: aoBuscar,
                  icon: const Icon(Icons.search),
                  label: const Text('Buscar'),
                ),
                TextButton(onPressed: aoLimpar, child: const Text('Limpar')),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
