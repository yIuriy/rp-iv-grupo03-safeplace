# DI01 — Implantação SafePlace — MVP

- [Diagrama editável no Astah](<DI01 - Implantação SafePlace - MVP.asta>)
- [Imagem do diagrama](<DI01 - Implantação SafePlace - MVP.png>)

Nova versão baseada no arquivo antigo `DiagramaImplatacaoVersaoFinal.asta/EntityStore.asta`, preservado sem alterações. O recorte adotado é o MVP definido na [especificação da entrega](../../mvp/especificacao-mvp-arquitetura.md), com [Arquitetura Hexagonal](../../arquitetura/especificacao-arquitetural.md).

## Infraestrutura preservada

| Nó | Função | CPU | RAM | Disco | Sistema operacional |
| --- | --- | --- | --- | --- | --- |
| Pegasus | Comunicação / servidor web | Não informado | Não informado | Não informado | Não informado |
| Zeus | Aplicação | 4 cores | 8 GB | 100 GB SSD | Linux Ubuntu 20.04 |
| Poseidon | Persistência | 4 cores | 8 GB | 100 GB SSD | Linux Ubuntu 20.04 |
| Hera | Banco de dados PostgreSQL | 8 cores | 16 GB | 500 GB SSD | Linux Ubuntu 20.04 |

Os clientes desktop mantêm Windows, macOS e Linux; os clientes móveis mantêm iOS e Android. Safari foi acrescentado aos navegadores previstos para atender ao RNF16. O acesso móvel do MVP é pelo navegador, sem o funcionamento offline previsto no legado.

As configurações são valores herdados, não resultados de dimensionamento ou validação da instalação. A versão do PostgreSQL não foi informada no original. A compatibilidade com Windows Server continua sendo uma obrigação do RNF16, sem pressupor outro servidor instalado.

## Distribuição dos artefatos individuais

O diagrama apresenta os artefatos separadamente dentro dos ambientes de execução, seguindo o nível de detalhe do exemplo da aula. Os blocos genéricos `safeplace-ui`, `safeplace-web`, `safeplace-application` e `safeplace-persistence` foram substituídos pelos elementos descritos abaixo.

Os nomes identificam unidades de código propostas, como arquivos de formulário, controle, caso de uso, domínio e persistência. Não afirmam que esses arquivos já existam, nem que cada um será um executável ou processo independente. Linguagem, extensões e empacotamento continuam a definir. Os artefatos são desenhados individualmente, sem contêineres de agrupamento adicionais.

| Função | Navegadores e Pegasus | Pegasus | Zeus |
| --- | --- | --- | --- |
| Autenticação | `FormularioAutenticacao` | `ControleAutenticacao` | `CasoDeUsoAutenticacao` |
| Usuários | `FormularioUsuarios` | `ControleUsuarios` | `CasoDeUsoUsuarios` |
| Ocorrências | `FormularioOcorrencias` | `ControleOcorrencias` | `CasoDeUsoOcorrencias` |
| Estoque de EPIs | `FormularioEstoqueEPIs` | `ControleEstoqueEPIs` | `CasoDeUsoEstoqueEPIs` |
| Manutenção de EPIs | `FormularioManutencaoEPIs` | `ControleManutencaoEPIs` | `CasoDeUsoManutencaoEPIs` |
| Empréstimos e devoluções | `FormularioEmprestimosEPIs` | `ControleEmprestimosEPIs` | `CasoDeUsoEmprestimosEPIs` |
| Áreas de risco | `FormularioAreasRisco` | `ControleAreasRisco` | `CasoDeUsoAreasRisco` |

Cada formulário tem três apresentações do mesmo artefato: no servidor web do Pegasus, que o fornece, e nos navegadores desktop e móvel, que o carregam. Isso não representa três implementações distintas nem funcionamento offline. Os controles recebem solicitações web e acionam a aplicação em Zeus.

Em Zeus, cada elemento abaixo também aparece separadamente:

- Domínio: `DominioUsuario`, `DominioOcorrencia`, `DominioEPI`, `DominioEstoque`, `DominioMovimentacao`, `DominioManutencao`, `DominioEmprestimo` e `DominioAreaRisco`.
- Contratos de repositório: `RepositorioUsuarios`, `RepositorioOcorrencias`, `RepositorioEPIs` e `RepositorioAreasRisco`.
- Adaptadores e serviços: `ServicoAutenticacao`, `ServicoAutorizacao` e `AdaptadorPersistenciaRemota`.

Os repositórios representam os arquivos dos contratos de saída incluídos com o código da aplicação. Não são servidores nem executáveis independentes. O `AdaptadorPersistenciaRemota` implementa o acesso ao serviço em Poseidon. A verificação de permissão por perfil ocorre no servidor, conforme o RNF03.

Poseidon contém os artefatos `UsuarioDAO`, `PerfilAcessoDAO`, `OcorrenciaDAO`, `EPIDAO`, `EstoqueDAO`, `MovimentacaoDAO`, `ManutencaoDAO`, `EmprestimoDAO` e `AreaRiscoDAO`. Cada DAO representa o código de acesso aos dados correspondentes. `GravadorAuditoria` registra as operações e `CriptografiaDados` representa a proteção dos dados sensíveis. Esses artefatos permanecem no serviço de persistência, sem integrações externas adicionadas.

### Configurações e banco de dados

As três especificações de implantação continuam desenhadas individualmente. Suas dependências apontam para os artefatos parametrizados:

| Configuração | Artefato parametrizado | Papel proposto |
| --- | --- | --- |
| `web-security.conf` | `ControleAutenticacao` | Configuração da entrada de autenticação. |
| `application-security.conf` | `ServicoAutorizacao` | Configuração de autorização por perfis. |
| `persistence-security.conf` | `CriptografiaDados` | Configuração da proteção de dados sensíveis. |

A configuração efetiva do transporte seguro em cada ambiente também deve ser definida e validada. As três dependências não comprovam que TLS, criptografia e autorização estejam implementados.

Em Hera, `safeplace-schema.sql` representa o script de instalação/migração que cria a estrutura do banco; `safeplace-db` representa os dados persistidos, gerenciados pelo PostgreSQL. O script não é o banco nem o executor da política de auditoria. A nota sobre a responsabilidade ainda indefinida pelo RNF05 foi preservada, sem restaurar a ligação de `audit-policy.conf` ao script.

A topologia continua a mesma: clientes comunicam-se com Pegasus; Pegasus com Zeus; Zeus com Poseidon; Poseidon com Hera. O layout mudou para acomodar os artefatos, mas os equipamentos, os dados de hardware e os protocolos foram preservados. A estrutura de microsserviços do exemplo não foi adotada.

A distribuição entre servidores não comprova a Arquitetura Hexagonal. Essa arquitetura depende dos contratos e da direção das dependências no código: o domínio deve permanecer independente dos adaptadores. O diagrama de implantação informa onde os artefatos ficam; a separação lógica deve ser demonstrada pelos diagramas correspondentes e pela implementação.

## Alinhamento com os requisitos

O desenho cobre os RF03, RF04, RF05, RF11, RF13, RF16 e RF23, classificados como `Must have` na [priorização](../../requisitos/priorizacao-moscow.md). Os [casos de uso](../../casos-de-uso/casos-de-uso.md) são considerados somente no recorte da primeira entrega.

Sensores e simulações pertencem ao legado e não possuem requisito vigente. Relatórios e outras funcionalidades fora do MVP continuam documentados no backlog. O diagrama antigo permanece preservado.

Serviços de apoio devem aparecer quando forem necessários à implantação, mesmo no MVP. Os elementos externos do desenho antigo foram avaliados separadamente:

| Elemento antigo | Decisão nesta proposta | Base da decisão |
| --- | --- | --- |
| ERP | Não incluído. | A integração externa do RNF17 está fora do MVP; o requisito atual prevê API REST, sem determinar SOAP. |
| Servidor de e-mail SMTP | Uso não definido. | O RF23 prevê geração de senha, mas não determina envio por e-mail. Se esse canal for adotado, será necessário representar o serviço e sua conexão. |
| Monitoramento externo | Serviço e local de execução não definidos. | Não há decisão atual de usar o serviço do legado. Monitoramento operacional pode apoiar a disponibilidade, sem depender da existência de sensores. |
| Nuvem AWS | Provedor e finalidade não definidos. | Hospedagem, armazenamento e backup precisam de uma decisão concreta; o termo "nuvem" não identifica sozinho o recurso usado. |
| RabbitMQ | Não incluído. | A especificação arquitetural exclui o barramento de eventos do MVP. |
| eSocial | Não incluído. | O RF19 prevê geração de dados para CAT, fora do MVP; não especifica transmissão ao eSocial. |

As justificativas sobre esses serviços ficam neste README. O `.asta` mantém apenas uma referência curta às pendências, sem conexões que indiquem serviços já adotados. Caso o grupo escolha algum deles, o diagrama deverá mostrar sua responsabilidade, o ambiente que o hospeda quando conhecido e a comunicação com o adaptador que o utiliza. Não há motivo para ligar todos os serviços ao servidor de persistência. Serviços de software também não devem ser classificados automaticamente como equipamentos físicos (`device`).

### Interpretação do RNF04

TCP/IP foi preservado como base das conexões. A proposta usa HTTPS com TLS 1.3 nas chamadas web: clientes–Pegasus, Pegasus–Zeus e Zeus–Poseidon. Entre Poseidon e Hera, usa o protocolo PostgreSQL protegido por TLS 1.3, sem HTTPS. Dados sensíveis devem ser armazenados com AES-256. Os canais internos não representam a integração externa do RNF17.

O texto do [RNF04](../../requisitos/requisitos-nao-funcionais.md#rnf04--criptografia-de-dados-sensíveis) exige HTTPS com TLS 1.3, sem explicitar essa distinção para o acesso ao banco. Portanto, esta é uma interpretação de projeto a confirmar com o grupo, não uma declaração de atendimento integral. A conexão com Hera não foi alterada para HTTPS, e o requisito original não foi modificado. Antes de declarar conformidade, é necessário resolver a diferença documental e verificar a configuração efetiva de todos os canais e da criptografia em armazenamento.

### Responsabilidade pela auditoria

O RNF05 exige log imutável com usuário, data/hora, dados alterados e retenção mínima de cinco anos. A proposta atribui a gravação dos registros ao serviço em Poseidon e o armazenamento ao banco em Hera. Ainda é necessário definir quem aplica a imutabilidade e a retenção: o serviço em Poseidon, mecanismos do PostgreSQL ou uma combinação de ambos. O script de criação do esquema não é, por si só, o executor dessa política durante a operação.

Os mecanismos de criptografia, gestão de chaves, imutabilidade e retenção ainda precisam ser definidos, implementados e testados. Disponibilidade, backup e restauração devem ser reavaliados antes de um piloto com dados reais, conforme a especificação do MVP.

## Notação e conferência

O arquivo é um diagrama de implantação nativo do Astah. Equipamentos usam `device`; softwares de apoio usam `executionEnvironment`; artefatos têm o símbolo de documento; configurações usam `deployment spec`. A implantação é mostrada por contenção. Os servidores usam o nome da função seguido do identificador entre parênteses, evitando confusão com a notação de instância e tipo. As configurações de hardware também estão registradas como *tagged values*, pares de chave e valor nos próprios nós.

O desenho prioriza os artefatos individuais, os ambientes que os hospedam, as configurações e os protocolos. As justificativas de escopo, legado e validação ficam neste README para evitar excesso de texto na imagem.

Foram conferidos seis dispositivos, seis ambientes de execução, 52 artefatos distintos, 66 apresentações de artefatos, cinco caminhos de comunicação e três dependências de configuração. Os sete formulários têm três apresentações cada; os demais artefatos têm uma apresentação cada. O arquivo foi salvo, reaberto e validado no Astah; a imagem exportada foi revisada quanto à legibilidade e às conexões.
