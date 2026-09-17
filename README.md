<h1 align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="frontend/design-system/assets/logo-inverse.svg">
    <source media="(prefers-color-scheme: light)" srcset="frontend/design-system/assets/logo-primary.svg">
    <img src="frontend/design-system/assets/logo-primary.svg" alt="SafePlace" width="420">
  </picture>
</h1>

<p align="center">
  <strong>Registrar. Acompanhar. Agir.</strong><br>
  Segurança do trabalho: ocorrências, EPIs e áreas de risco.
</p>

<p align="center">
  <a href="backend/pom.xml"><img src="https://img.shields.io/badge/Java-21-174B3F?style=for-the-badge&amp;labelColor=243D35" alt="Java 21"></a>
  <a href="backend/pom.xml"><img src="https://img.shields.io/badge/Spring_Boot-4.1.1-174B3F?style=for-the-badge&amp;logo=springboot&amp;logoColor=white&amp;labelColor=243D35" alt="Spring Boot 4.1.1"></a>
  <a href="docker-compose.yml"><img src="https://img.shields.io/badge/PostgreSQL-16-174B3F?style=for-the-badge&amp;logo=postgresql&amp;logoColor=white&amp;labelColor=243D35" alt="PostgreSQL 16"></a>
  <br>
  <a href="frontend/package.json"><img src="https://img.shields.io/badge/React-19-174B3F?style=for-the-badge&amp;logo=react&amp;logoColor=white&amp;labelColor=243D35" alt="React 19"></a>
  <a href="frontend/package.json"><img src="https://img.shields.io/badge/TypeScript-6-174B3F?style=for-the-badge&amp;logo=typescript&amp;logoColor=white&amp;labelColor=243D35" alt="TypeScript 6"></a>
  <a href="frontend/package.json"><img src="https://img.shields.io/badge/Vite-8-174B3F?style=for-the-badge&amp;logo=vite&amp;logoColor=white&amp;labelColor=243D35" alt="Vite 8"></a>
</p>

<p align="center">
  <a href="#sobre-o-projeto">Sobre</a> ·
  <a href="#tecnologias">Tecnologias</a> ·
  <a href="#executar-com-docker-compose">Como executar</a> ·
  <a href="frontend/design-system/README.md">Design system</a> ·
  <a href="#documentação">Documentação</a> ·
  <a href="#membros-do-grupo">Equipe</a>
</p>

## Sobre o projeto

O SafePlace é um projeto de sistema para apoiar a segurança do trabalho, com foco no registro e acompanhamento de acidentes e incidentes, no controle de Equipamentos de Proteção Individual (EPIs) e no mapeamento de áreas de risco. Seus usuários com acesso são o Gestor de Segurança e o Supervisor, com permissões conforme cada perfil.

O Gestor gerencia as contas dos supervisores. O Supervisor cadastra os colaboradores e registra os relatos comunicados por eles. O Colaborador permanece cadastrado para vínculo a ocorrências, capacitações e empréstimos de EPIs, sem conta, senha ou acesso ao sistema.

Este repositório reúne o trabalho do Grupo 03 na disciplina de Resolução de Problemas IV, dando continuidade ao projeto elaborado em Análise e Projeto de Software (APS).

## Tecnologias

As tecnologias abaixo compõem a base do projeto. O que já está disponível está descrito em [Base implementada](#base-implementada).

| Parte | Tecnologias | Uso no projeto |
| --- | --- | --- |
| Interface web | **React 19**, **TypeScript 6** e **Vite 8** | Interface e componentes reutilizáveis. Configuração em [package.json](frontend/package.json). |
| API e regras de negócio | **Java 21** e **Spring Boot 4.1.1** | Serviços do backend e operações da API. Configuração em [pom.xml](backend/pom.xml). |
| Persistência | **PostgreSQL 16**, **Spring Data JPA** e **Flyway** | Armazenamento dos dados e controle das alterações no banco. Veja a [configuração de conexão](backend/src/main/resources/application.yaml) e as [migrações](backend/src/main/resources/db/migration/). |
| Ambiente de desenvolvimento | **Docker Compose** e **Maven Wrapper** | Execução dos serviços locais e compilação do backend. Veja o [Compose](docker-compose.yml) e os [comandos de desenvolvimento](#desenvolver-com-execução-local). |
| Documentação da API | **Springdoc**, **OpenAPI** e **Swagger UI** | Contrato da API e consulta interativa às operações em `/docs`. |
| Design e identidade visual | **Figma**, **SVG** e **CSS** | Logo, cores, tipografia e componentes do [design system](frontend/design-system/README.md). |

## Escopo inicial

O MVP, a versão inicial com as funcionalidades essenciais, prevê:

- Registro e acompanhamento de acidentes e incidentes.
- Identificação de acidentes relacionados a fatores humanos.
- Classificação do nível de periculosidade das tarefas.
- Definição e acompanhamento de planos de ação.
- Controle de certificações e treinamentos obrigatórios.
- Gestão do ciclo de vida e planejamento da substituição dos EPIs.
- Controle de estoque, manutenção, empréstimos e devoluções de EPIs.
- Cadastro, consulta e atualização de áreas de risco.
- Gestão de supervisores com conta de acesso e de colaboradores sem conta, conforme RF23.

A documentação dos casos de uso também inclui funcionalidades previstas para versões posteriores. O recorte da versão inicial está definido na [Especificação do MVP e Arquitetura](docs/mvp/especificacao-mvp-arquitetura.md).

## Base implementada

O [backend](backend/) usa Java 21, Spring Boot 4.1.1, persistência JPA, migrações Flyway e PostgreSQL. A base atual oferece cadastro e listagem de acidentes e incidentes em `/api/ocorrencias`, incluindo os dados de plano de ação recebidos no cadastro. Também oferece cadastro e consulta de EPIs e registro de entradas e saídas de estoque em `/api/epis`, provisionamento de supervisores e colaboradores em `/api/usuarios`, mapeamento das áreas de risco em `/api/areas-risco` e classificação do nível de periculosidade das tarefas em `/api/tarefas`, além da verificação de disponibilidade em `/api/health`. Isso ainda não cobre todos os fluxos do MVP. A configuração de segurança permite as requisições sem aplicar os perfis definidos em RNF03.

O [frontend](frontend/README.md) usa React 19, TypeScript e Vite 8. A interface integra o login e a gestão de supervisores e colaboradores com a API (`/api/auth/login` e `/api/usuarios`), aplicando os perfis de RF23. As telas de ocorrências, EPIs, áreas de risco e tarefas ainda são iniciais. O catálogo de componentes em `/design-system` continua disponível com dados fictícios.

## Executar com Docker Compose

É necessário ter Docker com o plugin Compose e as portas 5432, 8080 e 5173 disponíveis. Execute os comandos a partir da raiz do repositório. O [Compose](docker-compose.yml) inicia banco, backend e frontend na mesma máquina; os servidores dos diagramas de implantação não são pré-requisito para o desenvolvimento local.

Inicie o banco:

```sh
docker compose up -d postgres
docker compose exec postgres pg_isready -U safeplace -d safeplace
```

Quando a verificação informar `accepting connections`, inicie as aplicações:

```sh
docker compose up -d --build backend frontend
docker compose ps
```

O Compose atual declara a ordem dos serviços, mas não contém verificação de prontidão do banco. Se a API encerrar por falta de conexão durante a primeira inicialização, confira os logs e inicie o backend novamente após o banco aceitar conexões:

```sh
docker compose logs backend
docker compose up -d backend
```

| Serviço | Endereço local | Como conferir |
| --- | --- | --- |
| Interface | [Tela de login](http://localhost:5173/login) | Deve apresentar o formulário de e-mail e senha. O [catálogo de componentes](http://localhost:5173/design-system) continua acessível sem login. |
| Backend | [Estado da aplicação](http://localhost:8080/actuator/health) | A resposta deve conter `"status":"UP"`. |
| API | [Documentação interativa](http://localhost:8080/docs) | Deve listar as operações de ocorrências, EPIs e disponibilidade. |
| Contrato da API | [OpenAPI](http://localhost:8080/v3/api-docs) | Deve retornar a descrição da API em JSON. |
| Banco | `localhost:5432` | Banco `safeplace`, usuário `safeplace` e senha de desenvolvimento `safeplace`, conforme o Compose. |

Para entrar na interface é preciso existir um Gestor de Segurança na tabela `usuarios`. O repositório ainda não traz essa carga inicial; a pendência está registrada na [especificação do MVP, seção 3.1](docs/mvp/especificacao-mvp-arquitetura.md#31-pessoas-cadastradas-fluxo-de-acesso-e-ausência-de-autocadastro) e no [README do frontend](frontend/README.md#primeiro-acesso).

Para encerrar os serviços mantendo os dados do volume do PostgreSQL:

```sh
docker compose down
```

## Desenvolver com execução local

Para executar as aplicações fora dos contêineres, use JDK 21 e Node.js 22.12 ou superior com npm. O backend inclui o Maven Wrapper, que baixa a versão configurada do Maven na primeira execução; não é necessário instalar Maven separadamente. Mantenha o PostgreSQL iniciado e verificado conforme os comandos anteriores, sem iniciar os contêineres de backend e frontend.

Em um terminal, a partir da raiz:

```sh
cd backend
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`. A conexão é configurada em [application.yaml](backend/src/main/resources/application.yaml):

| Variável de ambiente | Padrão para execução local |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `safeplace` |
| `DB_USER` | `safeplace` |
| `DB_PASSWORD` | `safeplace` |

Se usar outra instância de PostgreSQL, crie o banco e o usuário e defina essas variáveis antes de iniciar o backend. O Flyway aplica os arquivos de [migração](backend/src/main/resources/db/migration/) na inicialização; o JPA valida o esquema resultante.

Em outro terminal, a partir da raiz:

```sh
cd frontend
npm ci
npm run dev
```

Use o endereço exibido pelo Vite. A configuração da URL da API e os comandos de validação da interface estão no [README do frontend](frontend/README.md).

Para testar e empacotar o backend, execute em `backend/`:

```sh
./mvnw test
./mvnw package
```

A suíte é dividida em dois grupos. Os testes de domínio, casos de uso e controladores rodam sempre, sem banco. Os testes de integração — que aplicam as migrações Flyway, validam o mapeamento JPA e exercitam a API com o servidor real — precisam de um PostgreSQL e são **ignorados** quando não encontram nenhum, em vez de falhar. Há duas formas de fornecê-lo:

```sh
# 1. Docker disponível: os testes sobem o PostgreSQL sozinhos, via Testcontainers.
./mvnw test

# 2. Sem Docker: aponte para um PostgreSQL já em execução.
./mvnw test -Dsafeplace.test.datasource.url=jdbc:postgresql://localhost:5432/safeplace
```

Use `-Dsafeplace.test.datasource.username` e `-Dsafeplace.test.datasource.password` se as credenciais forem diferentes de `safeplace`. O banco indicado recebe as migrações; os testes rodam em transações com rollback e não deixam dados. O relatório de cobertura fica em `backend/target/site/jacoco/index.html` e o empacotamento gera `backend/target/backend-0.0.1-SNAPSHOT.jar`.

## Documentação

- [Design system e biblioteca de componentes](frontend/design-system/README.md)
- [Identidade visual e arquivos de design](frontend/design/README.md)
- [Requisitos funcionais](docs/requisitos/requisitos-funcionais.md)
- [Requisitos não funcionais](docs/requisitos/requisitos-nao-funcionais.md)
- [Priorização de requisitos](docs/requisitos/priorizacao-moscow.md)
- [Casos de Uso](docs/casos-de-uso/casos-de-uso.md)
- [Histórias de usuário](docs/historias-de-usuario.md)
- [Glossário](docs/glossario.md)
- [Especificação do MVP e Arquitetura](docs/mvp/especificacao-mvp-arquitetura.md)
- [Especificação arquitetural](docs/arquitetura/especificacao-arquitetural.md)
- [Diagramas UML](docs/diagramas/)
- [Pendências da revisão documental](docs/mvp/especificacao-mvp-arquitetura.md#11-pendências-da-revisão-documental)

## Membros do grupo

| Membro |
| --- |
| Amanda Dias de Souza |
| Dyonathan Bento Laner |
| Iuri da Silva Fernandes |
| Rafaela de Menezes |
| Rafaela Pacheco Nunes |
| Sidnei Correia Junior |
