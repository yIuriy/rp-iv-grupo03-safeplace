// Template Method (GoF) do envio de formulário (#233): executarEnvio fixa o esqueleto (ler, validar,
// focar o primeiro campo inválido, limpar a falha anterior, enviar e descrever a falha) e cada
// formulário informa só as etapas que mudam. Não depende do React, para ser testado sem navegador.

/** Resultado da validação: os dados prontos para a API, ou ausentes (null/undefined) quando há pendências. */
export type Validacao<D, E> = { dados?: D | null; erros: E }

/**
 * Ordem visual dos campos: lista de chaves do objeto de erros (vale a primeira com mensagem) ou
 * função que devolve os campos inválidos já ordenados, para erros aninhados.
 */
export type OrdemDosCampos<E> = readonly (keyof E & string)[] | ((erros: E) => readonly string[])

/**
 * Operações primitivas: os ganchos que cada formulário preenche. `liberarAposSucesso` é uma hook
 * operation (GoF): opcional, com padrão. Sem ela, o formulário é desmontado depois do sucesso e o botão
 * segue em "enviando"; com ela, o botão é liberado, para formulários que continuam montados (#243).
 */
export type EtapasDoEnvio<L, D, E> = {
  ler: (form: HTMLFormElement) => L
  validar: (lido: L) => Validacao<D, E>
  ordem: OrdemDosCampos<E>
  enviar: (dados: D) => Promise<void>
  descreverFalha: (erro: unknown) => string
  liberarAposSucesso?: boolean
}

/** Efeitos do esqueleto sobre a tela; o hook os liga ao estado do React. */
export type AcoesDoEnvio<E> = {
  definirErros: (erros: E) => void
  definirFalha: (falha: string) => void
  definirEnviando: (enviando: boolean) => void
  focar: (campo: string) => void
}

export type ResultadoDoEnvio = 'invalido' | 'enviado' | 'falhou'

/** Campos com pendência, na ordem em que aparecem na tela. */
export function camposInvalidos<E>(ordem: OrdemDosCampos<E>, erros: E): readonly string[] {
  if (typeof ordem === 'function') return ordem(erros)
  return ordem.filter(campo => Boolean(erros[campo]))
}

/** Ordem de campos identificados por id com prefixo, como `certificacao-nome` (#243). */
export function ordemPorId<E>(prefixo: string, campos: readonly (keyof E & string)[]): (erros: E) => string[] {
  return erros => campos.filter(campo => Boolean(erros[campo])).map(campo => `${prefixo}-${campo}`)
}

/** Método-modelo: a sequência é sempre a mesma; só as etapas variam entre formulários. */
export async function executarEnvio<L, D, E>(
  form: HTMLFormElement,
  etapas: EtapasDoEnvio<L, D, E>,
  acoes: AcoesDoEnvio<E>,
): Promise<ResultadoDoEnvio> {
  const { dados, erros } = etapas.validar(etapas.ler(form))
  acoes.definirErros(erros)
  if (dados === null || dados === undefined) {
    const [primeiro] = camposInvalidos(etapas.ordem, erros)
    if (primeiro) acoes.focar(primeiro)
    return 'invalido'
  }
  acoes.definirFalha('')
  acoes.definirEnviando(true)
  try {
    await etapas.enviar(dados)
    // Depois do sucesso o diálogo fecha; o botão segue em "enviando" para não aceitar outro clique.
    if (etapas.liberarAposSucesso) acoes.definirEnviando(false)
    return 'enviado'
  } catch (erro) {
    acoes.definirFalha(etapas.descreverFalha(erro))
    acoes.definirEnviando(false)
    return 'falhou'
  }
}

/** Foca o campo pelo name (num grupo de rádio, a primeira opção) e, sem name, pelo id dentro do formulário. */
export function focarCampo(form: HTMLFormElement, campo: string) {
  const porNome = form.elements.namedItem(campo)
  const alvo = porNome instanceof RadioNodeList ? porNome[0] : porNome ?? form.querySelector(`#${CSS.escape(campo)}`)
  if (alvo instanceof HTMLElement) alvo.focus()
}
