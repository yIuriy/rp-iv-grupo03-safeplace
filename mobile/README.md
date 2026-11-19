# SafePlace mobile (Flutter)

Cliente Android e iOS do SafePlace. Usa o mesmo backend do projeto (`/api`), sem alterações nele. Preparação da máquina: `docs/flutter/configuracao-ambiente.md`.

```bash
flutter pub get
flutter run                                              # emulador Android, API em 10.0.2.2:8080
flutter run --dart-define=API_URL=http://localhost:8080/api   # celular por USB, após adb reverse tcp:8080 tcp:8080
dart format . && flutter analyze && flutter test
```

## Estrutura

```
lib/
  app/        composição: router, menu por perfil, casca e ligações (dependencias.dart)
  core/       config, cliente HTTP, erros, sessão; não importa nenhuma feature
  shared/     widgets e cache offline (#316)
  features/<modulo>/
    domain/        interfaces (portas) e validação local
    data/          modelos (X.ler) e implementações que chamam a API
    presentation/  telas e diálogos
```

Regras de dependência: `features` → `core`; `core` nunca → `features`; só `app/` junta as features. Só `core/api/dio_api_client.dart` conhece o dio.

## Interfaces para as features

**Cliente HTTP** (`core/api/api_client.dart`): dependa da interface `ApiClient`, lida pelo `apiClientProvider` (`core/api/api_client_provider.dart`). Os métodos (`get`, `post`, `put`, `patch`, `delete`) recebem caminhos relativos a `/api`, devolvem o corpo JSON sem tipo e lançam `FalhaApi` (`status`, `corpo`; `status` nulo = sem resposta). O token vai sozinho; um 401 fora do login encerra a sessão e volta ao login, então a tela não trata 401. Se a feature precisar de algo novo (download binário dos relatórios, na #326), acrescente o método na interface e no `DioApiClient`.

Cada feature declara a sua porta em `domain/` e a implementa em `data/`, como `Autenticador` e `AutenticacaoApi`:

```dart
// domain/epis_repositorio.dart
abstract interface class EpisRepositorio {
  Future<List<Epi>> listar();
}

// data/epis_api.dart
class EpisApi implements EpisRepositorio {
  const EpisApi(this._client);
  final ApiClient _client;

  @override
  Future<List<Epi>> listar() async =>
      lista(await _client.get('/epis'), Epi.ler, 'Lista de EPIs inválida');
}

final episRepositorioProvider =
    Provider<EpisRepositorio>((ref) => EpisApi(ref.read(apiClientProvider)));
```

**Leitura das respostas** (`core/api/leitura.dart`): `objeto`, `lista`, `listaOuVazia`, `texto`, `textoOuNulo`, `numeroOuNulo`, `dataOuNula` (`LocalDate` ou `LocalDateTime` do backend). Cada modelo tem um `factory X.ler(Object? dado)` que usa essas funções; campo opcional ausente não derruba a tela.

**Mensagens de erro** (`core/api/erros.dart`): cada tela cria o seu descritor e sobrescreve só os status que precisa. 400, 404, 409 e 422 já usam o `message` do backend; falha de rede vira aviso de conexão. Para os campos extras de um 409 ou 422, leia `(erro as FalhaApi).corpo`.

```dart
final _descrever = criarDescritorDeFalha(
  porStatus: {409: usarMensagemDaApi('Já existe um colaborador com esse CPF.')},
  padrao: 'Não foi possível salvar o colaborador.',
);
// catch (erro) { setState(() => _falha = _descrever(erro)); }
```

**Sessão** (`core/sessao/`): `ref.watch(controladorSessaoProvider).value?.sessao` devolve `Sessao` (`token`, `email`, `nome`, `perfil`, `ehGestor`) ou `null`. Use `perfil` para mostrar ou esconder ações; o backend continua sendo quem autoriza. `iniciar(sessao)` abre a sessão (o login chama depois de autenticar) e `sair()` a encerra. O cache offline registra a limpeza da conta sobrescrevendo `limpezaDaContaProvider`.

## Tema e componentes compartilhados (#316)

**Tema** (`shared/tema/`): `AppTheme.claro()` monta o `ThemeData` a partir de `tokens.g.dart`, gerado do `frontend/design-system/tokens.json`. Depois de mudar o `tokens.json`, rode `dart run tool/gerar_tokens.dart`; um teste falha se esquecer. Não use cor nem medida solta nas telas:

- cores do Material: `Theme.of(context).colorScheme` (`primary` é o pinho, `error` o vermelho);
- cores que o Material não tem: `context.cores` (`alerta`, `perigoFundo`, `destaque`, `textoSecundario`...);
- medidas: `TokensDimensoes.space16`, `radius8`, `sizeControl`...;
- texto: `Theme.of(context).textTheme` (`headlineMedium` = Heading MD em Manrope, `bodyLarge` = Body MD em Inter, `labelLarge` = Label MD).

Botões, como no web: `FilledButton` (Primary, confirma), `OutlinedButton` (Secondary), `TextButton` (Quiet, cancela) e `FilledButton(style: AppTheme.botaoPerigo)` (Danger, operação destrutiva).

**Componentes** (`shared/widgets/` e `shared/formularios/`):

| Widget | Para quê |
| --- | --- |
| `Aviso(tom:, titulo:, mensagem:, anunciar:, rotuloAcao:, aoAgir:)` | Resultado de operação, falha com "Tentar novamente", alerta fixo (`TomAviso.info`, `sucesso`, `alerta`, `perigo`) |
| `AvisoOffline(sincronizadoEm:)` | Faixa de modo offline no topo da tela que mostra cache |
| `confirmar(context, titulo:, descricao:, rotuloConfirmar:, aoConfirmar:, descreverFalha:)` | Confirmação antes de descartar, remover ou arquivar; com `aoConfirmar`, executa a operação mostrando carregando e o erro dentro do diálogo |
| `CampoTexto(rotulo:, validador:, erroDoServidor:, linhas:, somenteLeitura:, habilitado:)` | Texto e área de texto; `erroDoServidor` mostra o erro de um 409/422 no campo certo e some quando a pessoa edita |
| `CampoSelecao<T>(rotulo:, opcoes: [(valor:, rotulo:)], aoMudar:)` | Poucas opções fixas (nível de perigo, status) |
| `SeletorComBusca<T>(rotulo:, opcoes:, descrever:, aoMudar:, validator:, carregando:, falha:, termosDeBusca:)` | Listas longas (colaborador, EPI, área): abre uma lista com busca que ignora acentos; `termosDeBusca` busca também o que não aparece na lista (dígitos de um CPF oculto) |
| `ConteudoAssincrono<T>(valor: ref.watch(provider), construir:, vazio:, falha:, aoTentarDeNovo:)` | Os quatro estados de uma consulta: carregando, falha, vazia e com dados |
| `EstadoVazio`, `EstadoCarregando` | Lista sem resultado (com ação opcional) e carregamento com texto |
| `MascaraCpf`, `validarCpf`, `digitosDoCpf`, `ocultarCpf` | CPF `000.000.000-00` na tela, 11 dígitos no envio, mesmas mensagens do web |
| `MascaraData`, `lerData`, `formatarData`, `dataIso`, `hoje` (`shared/formularios/data.dart`) | Data `dd/mm/aaaa` na tela (com máscara e recusa de 31/02), `aaaa-mm-dd` no envio |

**Navegação por perfil** (`app/navegacao.dart`, `app/casca.dart`): o menu é a lista `itensDoMenu`. Cada item tem a rota, o rótulo (com `rotuloSupervisor` quando muda, como "Colaboradores"), os `perfis` que o veem e a issue que entrega a tela. A casca monta o menu lateral pelo perfil, e o router manda ao início quem abre pelo endereço uma rota de outro perfil. Para entregar o seu módulo, troque a `PaginaEmConstrucao` pela sua tela em `_telaDo` (`app/router.dart`); sub-rotas (`/ocorrencias/:id`) entram como `routes:` do `GoRoute` do item.

**Cache offline somente leitura** (`shared/offline/`, RNF08): declare a consulta com `consultarComCache` e mostre com `ConteudoComCache`:

```dart
final episProvider = FutureProvider(
  (ref) => consultarComCache(
    ref,
    chave: 'epis',
    buscar: (api) => api.get('/epis'),
    ler: (json) => lista(json, Epi.ler, 'Lista de EPIs inválida'),
  ),
  retry: (_, _) => null,
);

ConteudoComCache(
  valor: ref.watch(episProvider),
  falha: 'Não foi possível carregar os EPIs.',
  aoTentarDeNovo: () => ref.invalidate(episProvider),
  construir: (epis, somenteLeitura) => ..., // somenteLeitura: desabilite cadastrar/editar
)
```

A resposta é guardada por conta (e-mail + chave) no armazenamento seguro e apagada ao sair ou expirar a sessão. Só quando o servidor **não responde** a tela recebe a cópia, com `somenteLeitura: true` e o `AvisoOffline` em cima. Um 4xx ou 5xx continua sendo erro. Escritas não são guardadas para enviar depois. A tela Início é o exemplo: em modo avião, mostra os dados da conta em cache e desabilita os atalhos de cadastro.

**Catálogo**: em debug, `flutter run --route=/catalogo` abre todos os componentes e seus estados, sem precisar de login. No Git Bash, use `MSYS_NO_PATHCONV=1 flutter run --route=/catalogo`, senão o `/catalogo` vira caminho do Windows. Para testar uma tela com o tema, use `montarComTema(tester, widget)` de `test/shared/apoio_tema.dart`.

**Testes**: `test/apoio.dart` tem `sessaoGestor`, `falhaHttp(status, corpo)` e `ArmazenamentoSessaoEmMemoria`. Teste a classe de `data/` com um mock de `ApiClient` e as telas sobrescrevendo o provider da porta da feature com um mock (`mocktail`), mais `armazenamentoSessaoProvider` com `ArmazenamentoSessaoEmMemoria(sessao)`. `AdaptadorFalso` serve só para testar o `DioApiClient`.

## Usuários, perfil e `ColaboradorPicker` (#317)

`features/usuarios/` entrega o item "Usuários" do menu (rótulo "Colaboradores" para o Supervisor) e "Meu perfil":

- `/usuarios`: o Gestor vê as abas Supervisores (`GET /api/usuarios`, filtrado para `SUPERVISOR`) e Colaboradores; o Supervisor vê só Colaboradores (`GET /api/usuarios/colaboradores?nome=&cpf=`) e não tem como listar nem cadastrar supervisores. A busca por CPF pede os 11 dígitos, porque o backend compara o CPF inteiro.
- Cadastro e edição em tela cheia (`FormularioPessoaPage`), com validação local em `domain/validacao_pessoa.dart` (mesmas mensagens do web). O Colaborador vai sem senha nem perfil; a edição envia só nome, data de nascimento e e-mail, com o CPF somente leitura. O cadastro de Supervisor termina mostrando a `senhaInicial` uma única vez; ela não vai para listas nem para o cache.
- 409 de CPF ou e-mail duplicado e 400 de validação (`campo: mensagem`) aparecem no campo certo (`erroDeCampo`); o resto vai para o aviso do formulário.
- `/perfil`: dados da conta (`GET /api/usuarios/me`) e ações recentes (`GET /api/usuarios/me/historico?limite=`, 20 de início e "Ver mais ações" até 100).
- Todas as leituras usam o cache offline da conta; sem conexão, "Novo" e "Editar" ficam desabilitados com o aviso.

**`ColaboradorPicker`** (`features/usuarios/presentation/colaborador_picker.dart`) é o campo para vincular um colaborador nos formulários de ocorrências (#318), empréstimos (#320), alocações (#324) e capacitações (#325). Carrega os colaboradores uma vez (com cópia offline), mostra o CPF oculto e busca por nome ou CPF, com ou sem máscara e sem acentos. Participa do `Form` como os outros campos:

```dart
ColaboradorPicker(
  rotulo: 'Colaborador envolvido',
  colaboradorId: _colaboradorId,   // escolha inicial por id, como vem nos registros (opcional)
  obrigatorio: true,               // "Escolha o colaborador." no validate()
  aoMudar: (colaborador) => setState(() => _colaboradorId = colaborador?.id),
)
```

Por padrão só oferece colaboradores ativos, como o web na entrega de EPI (`somenteAtivos: false` mostra todos); o já escolhido em `colaboradorId` aparece mesmo inativo, para a edição de um registro antigo não perder o vínculo. `validador:` troca a regra de obrigatoriedade. A tela de exemplo (`ExemploColaboradorPickerPage`, só em debug) abre pelo catálogo, em "Componentes das features", ou em `/usuarios/exemplo-seletor`; ela pede login, porque usa a API.
