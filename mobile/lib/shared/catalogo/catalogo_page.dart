import 'package:material_ui/material_ui.dart';

import '../formularios/cpf.dart';
import '../tema/app_theme.dart';
import '../tema/cores_safeplace.dart';
import '../tema/tokens.g.dart';
import '../widgets/aviso.dart';
import '../widgets/campos.dart';
import '../widgets/dialogo_confirmacao.dart';
import '../widgets/estados.dart';
import '../widgets/seletor_com_busca.dart';

/// Catálogo dos componentes compartilhados (#316), equivalente ao `/design-system` do web: mostra
/// cada widget e seus estados com dados de exemplo. Só existe no modo debug (rota `/catalogo`).
class CatalogoPage extends StatelessWidget {
  const CatalogoPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Catálogo de componentes')),
      body: ListView(
        padding: const EdgeInsets.all(TokensDimensoes.layoutMobilePadding),
        children: const [
          _Secao('Tipografia', _Tipografia()),
          _Secao('Cores', _Cores()),
          _Secao('Botões', _Botoes()),
          _Secao('Avisos', _Avisos()),
          _Secao('Campos', _Campos()),
          _Secao('Estados', _Estados()),
        ],
      ),
    );
  }
}

class _Secao extends StatelessWidget {
  const _Secao(this.titulo, this.conteudo);

  final String titulo;
  final Widget conteudo;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: TokensDimensoes.space32),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Semantics(
            header: true,
            child: Text(
              titulo,
              style: Theme.of(context).textTheme.headlineSmall,
            ),
          ),
          const Divider(),
          conteudo,
        ],
      ),
    );
  }
}

class _Tipografia extends StatelessWidget {
  const _Tipografia();

  @override
  Widget build(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Heading MD · Manrope 600 28', style: t.headlineMedium),
        Text('Heading SM · Manrope 600 20', style: t.titleLarge),
        Text('Body MD · Inter 400 16', style: t.bodyLarge),
        Text('Body SM · Inter 400 14', style: t.bodyMedium),
        Text('Label MD · Inter 600 14', style: t.labelLarge),
        Text('Label SM · Inter 500 12', style: t.labelMedium),
      ],
    );
  }
}

class _Cores extends StatelessWidget {
  const _Cores();

  @override
  Widget build(BuildContext context) {
    final c = context.cores;
    final amostras = <(String, Color)>[
      ('brand/primary', TokensCores.brandPrimary),
      ('brand/deep', TokensCores.brandDeep),
      ('brand/soft', c.suave),
      ('brand/accent', c.destaque),
      ('surface/page', c.pagina),
      ('text/secondary', c.textoSecundario),
      ('feedback/info', c.info),
      ('feedback/success', c.sucesso),
      ('feedback/warning', c.alerta),
      ('feedback/danger', c.perigo),
    ];
    return Wrap(
      spacing: TokensDimensoes.space8,
      runSpacing: TokensDimensoes.space8,
      children: [
        for (final (nome, cor) in amostras)
          SizedBox(
            width: 150,
            child: Row(
              children: [
                Container(
                  width: 24,
                  height: 24,
                  decoration: BoxDecoration(
                    color: cor,
                    borderRadius: BorderRadius.circular(
                      TokensDimensoes.radius4,
                    ),
                    border: Border.all(color: c.borda),
                  ),
                ),
                const SizedBox(width: TokensDimensoes.space8),
                Flexible(
                  child: Text(
                    nome,
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ),
              ],
            ),
          ),
      ],
    );
  }
}

class _Botoes extends StatelessWidget {
  const _Botoes();

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: TokensDimensoes.space8,
      runSpacing: TokensDimensoes.space8,
      children: [
        FilledButton(onPressed: () {}, child: const Text('Primary')),
        OutlinedButton(onPressed: () {}, child: const Text('Secondary')),
        TextButton(onPressed: () {}, child: const Text('Quiet')),
        FilledButton(
          style: AppTheme.botaoPerigo,
          onPressed: () {},
          child: const Text('Danger'),
        ),
        const FilledButton(onPressed: null, child: Text('Desabilitado')),
        FilledButton.icon(
          onPressed: () {},
          icon: const Icon(Icons.add),
          label: const Text('Com ícone'),
        ),
        OutlinedButton(
          onPressed: () => confirmar(
            context,
            titulo: 'Descartar alterações?',
            descricao: 'O registro ainda não foi enviado. Ao sair, o que foi preenchido será perdido.',
            rotuloConfirmar: 'Descartar',
          ),
          child: const Text('Abrir diálogo de confirmação'),
        ),
      ],
    );
  }
}

class _Avisos extends StatelessWidget {
  const _Avisos();

  @override
  Widget build(BuildContext context) {
    return Column(
      spacing: TokensDimensoes.space12,
      children: [
        const Aviso(
          titulo: 'Informação',
          mensagem: 'A lista é atualizada a cada consulta.',
        ),
        const Aviso(tom: TomAviso.sucesso, titulo: 'Colaborador cadastrado'),
        const Aviso(
          tom: TomAviso.alerta,
          titulo: 'Certificação vence em 10 dias',
        ),
        Aviso(
          tom: TomAviso.perigo,
          titulo: 'Não foi possível salvar',
          mensagem: 'Verifique sua conexão.',
          rotuloAcao: 'Tentar novamente',
          aoAgir: () {},
        ),
        AvisoOffline(sincronizadoEm: DateTime(2026, 11, 17, 14, 30)),
      ],
    );
  }
}

class _Campos extends StatefulWidget {
  const _Campos();

  @override
  State<_Campos> createState() => _CamposState();
}

class _CamposState extends State<_Campos> {
  final _formulario = GlobalKey<FormState>();
  String? _nivel;

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formulario,
      child: Column(
        spacing: TokensDimensoes.space16,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          CampoTexto(
            rotulo: 'Nome completo',
            ajuda: 'Como consta no documento.',
            validador: obrigatorio('Informe o nome completo.'),
          ),
          const CampoTexto(
            rotulo: 'CPF',
            teclado: TextInputType.number,
            formatadores: [MascaraCpf()],
            validador: validarCpf,
          ),
          const CampoTexto(
            rotulo: 'E-mail',
            erroDoServidor: 'Já existe um usuário com esse e-mail.',
          ),
          const CampoTexto(
            rotulo: 'Matrícula',
            valorInicial: '000123',
            somenteLeitura: true,
          ),
          const CampoTexto(
            rotulo: 'Setor',
            valorInicial: 'Caldeiras',
            habilitado: false,
          ),
          const CampoTexto(rotulo: 'Observações', linhas: 4),
          CampoSelecao<String>(
            rotulo: 'Nível de perigo',
            valor: _nivel,
            opcoes: const [
              (valor: 'BAIXO', rotulo: 'Baixo'),
              (valor: 'MEDIO', rotulo: 'Médio'),
              (valor: 'ALTO', rotulo: 'Alto'),
              (valor: 'CRITICO', rotulo: 'Crítico'),
            ],
            aoMudar: (valor) => setState(() => _nivel = valor),
          ),
          SeletorComBusca<String>(
            rotulo: 'Colaborador',
            opcoes: const [
              'João da Silva',
              'Joana Ribeiro',
              'Ana Souza',
              'Bruno Lima',
            ],
            descrever: (nome) => nome,
            aoMudar: (_) {},
            validator: (valor) =>
                valor == null ? 'Escolha o colaborador.' : null,
          ),
          FilledButton(
            onPressed: () => _formulario.currentState!.validate(),
            child: const Text('Validar formulário'),
          ),
        ],
      ),
    );
  }
}

class _Estados extends StatelessWidget {
  const _Estados();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        EstadoVazio(
          titulo: 'Nenhum colaborador encontrado',
          descricao: 'Ajuste a busca ou cadastre um novo colaborador.',
          rotuloAcao: 'Limpar busca',
          aoAgir: () {},
        ),
        const SizedBox(
          height: 120,
          child: EstadoCarregando(texto: 'Carregando colaboradores...'),
        ),
      ],
    );
  }
}
