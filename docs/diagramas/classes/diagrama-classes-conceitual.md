# Diagrama de Classes Conceitual

**Link:** https://drive.google.com/file/d/11Dlp0f2jS6RFaou4GOPT2ptH0Kf_OZFB/view?usp=sharing

**Descrição do esboço das classes:**

## 1. Módulo de Segurança e Inspeção

### ItemVerificado

**Atributos:**

- descricao: String
- dataVerificacao: Date
- conforme: boolean
- observacoes: String

**Relacionamentos:**

- Associação com Inspecao (Multiplicidade: 0..* para 1)

### Inspecao

**Atributos:**

- dataInspecao: Date
- responsavel: String
- conformidade: boolean
- observacoes: String

**Relacionamentos:**

- Associação com ItemVerificado (Multiplicidade: 1 para 0..*)
- Associação com GestaoDeSeguranca (Multiplicidade: 0..* para 1)

### GestaoDeSeguranca

**Atributos:**

- nome: String
- email: String
- senha: String

**Relacionamentos:**

- Associação com Inspecao (Multiplicidade: 1 para 0..*)
- Associação com AlertaComportamento (Multiplicidade: 1 para 0..*)
- Associação com PlanoDeAcao (Multiplicidade: 1 para 0..1)
- Associação com Ocorrencia (Multiplicidade: 1 para 0..*)

### AlertaComportamento

**Atributos:**

- dataEmissao: DateTime
- nivelGravidade: NivelPerigo
- mensagemNotificacao: String
- lidoPeloGestor: boolean

**Relacionamentos:**

- Associação com GestaoDeSeguranca (Multiplicidade: 0..* para 1)
- Associação com AreaDeRisco (Multiplicidade: 0..* para 1)

### PlanoDeAcao

**Atributos:**

- medidasCorretivas: String
- prazo: Date
- status: String
- medidasPreventivas: String

**Métodos:**

- +criarPlano(dados): PlanoDeAcao
- +atualizar(novosDados): void
- +obterDadosParaEdicao(): PlanoDeAcao

**Relacionamentos:**

- Associação com GestaoDeSeguranca (Multiplicidade: 0..1 para 1)

## 2. Módulo de Infraestrutura, Setores e Rotas

### Sensor

**Atributos:**

- localizacao: String
- tipo: TipoSensor (vinculado ao Enum de propriedades físicas)

**Relacionamentos:**

- Associação com AreaDeRisco (Multiplicidade: 0..* para 1)

### AreaDeRisco

**Atributos:**

- nome: String
- status: String
- status: NivelPerigo

**Métodos:**

- +processarLeitura(leitura): void
- +avaliarNivelPerigo(dado: NivelPerigo): void

**Relacionamentos:**

- Associação com Sensores (Multiplicidade: 1 para 0..*)
- Associação com AlertaComportamento (Multiplicidade: 1 para 0..*)
- Associação com Setor (Multiplicidade: 0..* para 1)

### Setor

**Atributos:**

- nome: String
- descricao: String
- localizacao: String
- status: NivelPerigo

**Métodos:**

- + obterDadosPlanta(): Setor
- + validarLimitesSetor(localOrigem): boolean
- + validarSaidasCadastradas(): List<SaidaEmergencia>
- + bloquearZonaDeImpacto(localOrigem): boolean
- + verificarRotasLivres(): List<RotaSaida>
- + aplicarBloqueioFisico(idSaida): boolean

**Relacionamentos:**

- Associação com AreaDeRisco (Multiplicidade: 1 para 0..*)
- Associação com SaidaEmergencia (Multiplicidade: 1 para 0..*)
- Associação com SimulacaoEmergencia (Multiplicidade: 1 para 0..*)
- Associação com Vulnerabilidade (Multiplicidade: 1 para 1..*)
- Associação com Tarefa (Multiplicidade: 1 para 1..*)

### SaidaEmergencia

**Atributos:**

- idSaida: int
- capacidadeEvacuacao: String
- localizacao: String
- estaDesobstruida: boolean

**Métodos:**

- + definirBloqueio(bloqueado): boolean

**Relacionamentos:**

- Associação com Setor (Multiplicidade: 0..* para 1)

### SimulacaoEmergencia

**Atributos:**

- idSimulacao: int
- dataRealizacao: Date
- tempoEvacuacaoPrat: Time
- eficacia: float

**Relacionamentos:**

- Associação com Setor (Multiplicidade: 0..* para 1)

### Vulnerabilidade

**Atributos:**

- idSetor: int
- descricao: String
- local: String

**Relacionamentos:**

- Associação com Setor (Multiplicidade: 1..* para 1)

### ServicoDeRoteamento

**Atributos:**

- reguaVelocidade: double
- algoritmoBusca: String

**Métodos:**

- + executarBuscaDeCaminho(): List<RotaSaida>
- + calcularTempoEstimado(): int
- + identificarGargalos(): List<Gargalo>
- + recolocarRotas(): List<RotaSaida>
- + gerarComparativoRealESimulado(): RelatorioComparativo

**Relacionamentos:**

- Associação com RotaSaida (Multiplicidade: 1 para 0..*)
Associação com RelatorioComparativo de dependência em que a seta está
para o lado de RelatorioComparativo

### RotaSaida

**Atributos:**

- caminho: int
- distanciaTotal: int
- tempoEstimado: int
- status: boolean
- gargalos: List<Gargalo>

**Métodos:**

- + calcularTempoEstimado(): int
- + estaBloqueada(): boolean

**Relacionamentos:**

- Associação com ServicoDeRoteamento (Multiplicidade: 0..* para 1)
- Agregação com Gargalo (Multiplicidade: 1 para 0..* / Losango transparente
no lado da RotaSaida)

### Gargalo

**Atributos:**

- localizacao: int
- descricao: String
- gravidade: NivelPerigo

**Relacionamentos:**

- Parte integrante de RotaSaida via agregação.

## 3. Módulo de Funcionários e Treinamentos

### Funcionario

**Atributos:**

- cpf: String
- nome: String
- dataAdmissao: Date
- funcao: String
- classificacao: StatusFuncionario
- historicoComportamento: String

**Métodos:**

- +estaApto(setor:Setor): boolean
- +buscarFuncionario(): List<Funcionario>
- +atualizarStatusAptidao(): void

**Relacionamentos:**

- Associação com Certificacao (Multiplicidade: 1 para 0..*)
- Associação com AtestadoSaude (Multiplicidade: 1 para 0..*)
- Associação com Treinamento (Multiplicidade: 1..* para 0..*)
- Associação com Tarefa (Multiplicidade: 1..* para 1)
- Associação com Ocorrencia (Multiplicidade: 1 para 0..*)
- Associação com Emprestimo (Multiplicidade: 1 para 0..*)

### Certificacao

**Atributos:**

- nome: String
- status: String
- dataVencimento: Date
- dataEmissao: Date

**Métodos:**

- +buscarCertificado(nome): Certificado
- +estaVencido(): boolean

**Relacionamentos:**

- Associação com Funcionario (Multiplicidade: 0..* para 1)

### AtestadoSaude

**Atributos:**

- dataEmissao: Date
- tipo: String
- dataVencimento: Date
- apto: boolean

**Relacionamentos:**

- Associação com Funcionario (Multiplicidade: 0..* para 1)

### Treinamento

**Atributos:**

- nome: String
- conteudo: String
- instrutor: String
- dataRealizacao: Date
- cargaHoraria: int

**Relacionamentos:**

- Associação com Funcionario (Multiplicidade: 0..* para 1..*)

### Tarefa

**Atributos:**

- descricao: String
- status: NivelPerigo

**Relacionamentos:**

- Associação com Funcionario (Multiplicidade: 1 para 1..*)
- Associação com Setor (Multiplicidade: 1..* para 1)

## 4. Módulo de Ocorrências, Incidentes e Acidentes

### Ocorrencia (Classe Base / Superclasse)

**Atributos:**

- dataOcorrencia: Date
- local: String
- descricao: String

**Relacionamentos:**

- Associação com Funcionario (Multiplicidade: 0..* para 1)
- Associação com GestaoDeSeguranca (Multiplicidade: 0..* para 1)
- Associação com Testemunha (Multiplicidade: 1 para 0..*)
- Associação com MídiaAnexa (Multiplicidade: 1 para 0..*)
- *Herança/Especialização:* Superclasse de Incidente e Acidente.

### Incidente (Herda de Ocorrencia)

**Atributos:**

- idLocalRisco: String
- potencialDano: String

### Acidente (Herda de Ocorrencia)

**Atributos:**

- causaRaiz: String
- tipo: String

**Relacionamentos:**

- Associação com CAT (Multiplicidade: 1 para 0..1)

### CAT (Comunicação de Acidente de Trabalho)

**Atributos:**

- numeroProtocolo: String
- destino: OrgaoDestino

**Relacionamentos:**

- Associação com Acidente (Multiplicidade: 0..1 para 1)
- Associação com OrgaoDestino (Multiplicidade: 1..* para 1)

### OrgaoDestino

**Atributos:**

- nome: String
- cnpj: String
- endereco: String

**Relacionamentos:**

- Associação com CAT (Multiplicidade: 1 para 1..*)

### Testemunha

**Atributos:**

- nome: String
- documentoIdentificacao: String
- depoimento: String
- contato: String

**Relacionamentos:**

- Associação com Ocorrencia (Multiplicidade: 0..* para 1)

### MídiaAnexa

**Atributos:**

- nomeArquivo: String
- tipoMídia: String
- caminhoArquivo: String
- dataUpload: DateTime

**Relacionamentos:**

- Associação com Ocorrencia (Multiplicidade: 0..* para 1)

## 5. Módulo de Inventário e Logística de EPIs

### Emprestimo

**Atributos:**

- dataRetirada: Date
- dataDevolucaoPrevista: Date
- dataDevolucaoReal: Date
- status: ClassificacaoEmprestimo

**Métodos:**

- + obterHistoricoUsoFrequencia(): List<Emprestimo>

**Relacionamentos:**

- Associação com Funcionario (Multiplicidade: 0..* para 1)
- Associação com EPI (Multiplicidade: 0..* para 1)

### EPI

**Atributos:**

- ca: int
- nome: String
- status: ClassificacaoEPI
- validade: Date
- dataFornecimento: Date
- dataPrevisaoDescarte: Date

**Métodos:**

- + obterDataValidadeCondicoes(): int
- + verificarValidadeCA(): Date
- + atualizarPrevisaoDescarte(novaDate: Date): void

**Relacionamentos:**

- Associação com Emprestimo (Multiplicidade: 1 para 0..*)
- Associação com Fornecedor (Multiplicidade: 0..* para 1)
- Associação com EntradaEstoque (Multiplicidade: 1 para 0..*)
- Associação com Estoque (Multiplicidade: 1 para 1)
- Associação com ProjecaoSubstituicao (Multiplicidade: 1 para 0..*)
- Associação com Manutencao_EPI (Multiplicidade: 1 para 0..*)

### Fornecedor

**Atributos:**

- cnpj: String
- razaoSocial: String
- representante: String
- email: String
- telefone: int

**Relacionamentos:**

- Associação com EPI (Multiplicidade: 1 para 0..*)

### EntradaEstoque

**Atributos:**

- numeroNf: int
- dataFiscal: Date
- valorUnitario: double

**Relacionamentos:**

- Associação com EPI (Multiplicidade: 0..* para 1)
- Associação com MovimentacaoEstoque (Conexão estrutural/fluxo de
estoque)

### MovimentacaoEstoque

**Atributos:**

- data: Date
- quantidade: int
- situacao: int
- motivo: String

**Métodos:**

- + atualizarQuantidadeMov(novaQtd: int): void

**Relacionamentos:**

- Associação com Estoque (Multiplicidade: 0..* para 1)

### Estoque

**Atributos:**

- quantAtual: int
- quantMinima: int
- localizacao: String

**Métodos:**

- + compararComQuantMinima(): List<EPI>
- + validarCustoHistoricoConsumo(novaQtd: int): boolean

**Relacionamentos:**

- Associação com EPI (Multiplicidade: 1 para 1)
- Associação com MovimentacaoEstoque (Multiplicidade: 1 para 0..*)

### ProjecaoSubstituicao

**Atributos:**

- dataProjecao: Date
- dataEstimadaDescarte: Date
- quantidadeCalculada: int
- justificativa: String

**Relacionamentos:**

- Associação com EPI (Multiplicidade: 0..* para 1)

### Manutencao_EPI

**Atributos:**

- dataManutencao: Date
- descricaoManutencao: String
- resultadoManutencao: Boolean

**Relacionamentos:**

- Associação com EPI (Multiplicidade: 0..* para 1)

## 6. Enums (Enumeradores estruturais)

### <<Enum>> ClassificacaoEmprestimo

* Aberto
* Disponível
* Em uso
* EmDevolucao

### <<Enum>> EstadoManutencao

* Válido
* Na validade
* Em manutenção
* Fora da validade

### <<Enum>> ClassificacaoEPI

* Proteção contra quedas
* Proteção de membros inferiores
* Proteção de membros superiores
* Proteção de tronco
* Proteção respiratória
* Proteção auditiva
* Proteção dos olhos
* Proteção da face
* Proteção da cabeça

### <<Enum>> StatusFuncionario

* Indisponível
* Disponível
* Em tarefa
* Em férias
* Afastado

### <<Enum>> NivelPerigo

* Baixo
* Médio
* Crítico

### <<Enum>> TipoSensor / Propriedades Físicas

* Presença e Proximidade
* Gases
* Qualidade do ar
* Ambientes
* Movimento
* Vibração
* Radiação

## Associações e observações extraídas

### AreaDeRisco

**Associações:**

- AreaDeRisco 0..1 -- 0…* Tarefa
- AreaDeRisco 1 –1 setor
- AreaDeRisco 1 — 1…* SimulacaoEmergencia
- AreaDeRisco 1 — 0…* SaidaEmergencia
- AreaDeRisco 1…* — 0…* Inspecao
- AreaDeRisco 0...1 — Ocorrência
- AreaDeRisco 1…* — 1 GestorDeSeguranca
AlertaComportamento:
**Associações:**

- AlertaComportamento 0…* — 1 Funcionário
- AlertaComportamento 0 — 1 AreaDeRisco
Setor:
**Associações:**

- Setor 1 -- "0..*" AreaDeRisco
- Setor 1 -- "1..*" Tarefa
- setor 1..* – 0..* gestor
- setor 1 – 0..* serviço
- ServicoDeRoteamento --<<use>>--> Gargalo
- ServicoDeRoteamento --<<use>>--> RotaSaida
Isso significa que a classe ServicoDeRoteamento usa essas classes para
realizar suas operações.
- movimentacaoEstoque 0..* – 1 gestor
- midiaAnexo 0..* – 1 inspecao
- tarefa 1 – 0..* epi
- emprestimo 0..* – 1..* epi

### Inspecao

- Associação com ItemVerificado (1 para 0..)
- Associação com GestaoDeSeguranca (0.. para 1)
- associação com MidiaAnexa (1 para 0..*)
associacao com area de risco ()

### GestorDeSeguranca

- Associação com Inspecao (1 para 0..)
- Associação com AlertaComportamento (1 para 0..)
- Associação com PlanoDeAcao (1 para 0..1)
- Associação com Ocorrencia (1 para 0..)
- Associação com AreaDeRisco (1 para 1..)
- Associação com AlertaSensor (1 para 0..)
- REMOVER Associação com Setor (1 para 1..) por conta de transitividade
Especialização com Funcionário
- Associação com Treinamento (1 para 1)
- Associação com movimentacaoEstoque (1 para 0…*)

### AlertaComportamento

- remove associacao com GestorSeguranca

### PlanoDeAcao

- Associação com GestaoDeSeguranca (0..1 para 1)
- REMOVER Composição com Tarefa (1 para 1..*, com PlanoDeAcao como Todo)
- Associacao com ocorrrencia(1…* para 1…*)

### SaidaEmergencia

- Associação com AreaDeRisco (0..* para 1)
- Associação com SimulacaoEmergencia (0..* para 1)

### ServicoRoteamento

- Associação com Setor (0…* para 1)
- Dependência <<use>> com RotaSaida
- Dependência <<use>> com Gargalo

### RotaSaida

- Composição com Gargalo ()
- Usada por ServicoDeRoteamento via dependência <<use>>

### AtestadoSaude

- Associação com Funcionario (1..* para 1)
- REMOVER Associação com Acidente (0..1 para 0..1)

### Treinamento

- Associação com Funcionario (0..* para 0..*)
- Associação com Certificacao (1 para 1..*)
- Associação com GestorSeguranca (1 para 0..*)

### Tarefa

- REMOVER Associação com Funcionario (1 para 1..)
- Associação com Setor (1.. para 1)
- Associação com AreaDeRisco (0.. para 0..1)
- REMOVER Composição com PlanoDeAcao (1.. para 1, com PlanoDeAcao como Todo)
- Associação com Ocorrencia (0..1 para 0..)
- Associação com EPI (1.. para 0..*)

### Funcionario

- Associação com Certificacao (1 para 0..)
- Associação com AtestadoSaude (1 para 0..)
- Associação com Treinamento (1.. para 0..)
- Associação com Tarefa (1.. para 1)
- Associação com Ocorrencia (1 para 0..)
- Associação com Emprestimo (1 para 0..)
- Associação com AlertaComportamento (1 para 0..)
- Associação com setor (1 para 1..* )

### Acidente

- Herda de Ocorrencia
- Associação com CAT (1 para 0..1)
- REMOVER Associação com AtestadoSaude (0..1 para 0..1)

### EPI

- Associação com Emprestimo (1 para 0..)
- Associação com Fornecedor (1.. para 1..)
- Composição com Estoque (1.. para 1, com Estoque como Todo)
- Associação com ProjecaoSubstituicao (1 para 0..)
- Associação com Manutencao_EPI (1 para 0..)
- Associação com Tarefa (1 para 0..*)
- Associação com Ocorrencia
- ENUM EstadoManutencao tirar “valido”
