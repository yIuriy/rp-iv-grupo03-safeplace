// Tela /relatorios, menu "Relatórios" (RF18, issue #299): Gestor de Segurança e Supervisor escolhem o
// relatório e o período (mês ou ano), pré-visualizam o conteúdo e baixam o arquivo em CSV ou PDF. O
// período é conferido aqui antes do envio e de novo no backend.

import { useCallback, useState, type FormEvent } from 'react'
import { Alert, Button, EmptyState, Select, TextField, Toast } from '../../shared/components'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'
import { useConsulta } from '../../shared/api/useConsulta'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { baixarRelatorio, gerarRelatorio, tiposRelatorio, type FormatoRelatorio, type TipoRelatorio } from './api'
import { anoAtual, mesAtual, periodoDoAno, periodoDoMes, type Periodo, type Recorte } from './periodo'
import { PreviaRelatorio } from './PreviaRelatorio'
import './relatorios.css'

const descreverFalha = criarDescritorDeFalha({
  403: 'Seu perfil não tem permissão para emitir relatórios.',
}, usarMensagemDaApi('Não foi possível gerar o relatório. Tente novamente em alguns instantes.'))

type Pedido = { tipo: TipoRelatorio; periodo: Periodo }

export function RelatoriosPage() {
  const [tipo, setTipo] = useState<TipoRelatorio>('acidentes')
  // Abre no ano corrente: o mês corrente costuma estar vazio no começo do mês.
  const [recorte, setRecorte] = useState<Recorte>('anual')
  const [mes, setMes] = useState(mesAtual())
  const [ano, setAno] = useState(String(anoAtual()))
  const [erroPeriodo, setErroPeriodo] = useState<string>()
  const [pedido, setPedido] = useState<Pedido | null>(null)
  const [baixando, setBaixando] = useState<FormatoRelatorio | null>(null)
  const [erroDownload, setErroDownload] = useState<string>()
  const [mensagem, setMensagem] = useState('')

  const carregar = useCallback(
    (signal: AbortSignal) => (pedido ? gerarRelatorio(pedido.tipo, pedido.periodo, signal) : Promise.resolve(null)),
    [pedido],
  )
  const consulta = useConsulta(pedido ? `${pedido.tipo}-${pedido.periodo.inicio}-${pedido.periodo.fim}` : 'nenhum', carregar)
  const descricaoTipo = tiposRelatorio.find(item => item.valor === tipo)?.descricao

  /** Período do formulário, ou `null` com a mensagem no campo a corrigir. */
  function lerPeriodo(): Periodo | null {
    const periodo = recorte === 'mensal' ? periodoDoMes(mes) : periodoDoAno(Number(ano))
    setErroPeriodo(periodo ? undefined
      : recorte === 'mensal' ? 'Informe o mês do relatório.' : 'Informe um ano entre 2000 e 2100.')
    return periodo
  }

  function previsualizar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const periodo = lerPeriodo()
    if (periodo) setPedido({ tipo, periodo })
  }

  async function baixar(formato: FormatoRelatorio) {
    const periodo = lerPeriodo()
    if (!periodo) return
    setBaixando(formato)
    setErroDownload(undefined)
    try {
      const nome = await baixarRelatorio(tipo, periodo, formato)
      setMensagem(`Relatório baixado: ${nome}.`)
    } catch (erro) {
      setErroDownload(descreverFalha(erro))
    } finally {
      setBaixando(null)
    }
  }

  return <FeaturePage title="Relatórios"
    description="Emita relatórios estatísticos de acidentes, treinamentos e áreas de risco por mês ou por ano, em CSV ou PDF.">
    <div className="sp-painel">
      <form className="sp-relatorio-filtros" aria-label="Escolher relatório" noValidate onSubmit={previsualizar}>
        <Select label="Relatório" value={tipo} helper={descricaoTipo}
          onChange={e => setTipo(e.target.value as TipoRelatorio)}>
          {tiposRelatorio.map(item => <option key={item.valor} value={item.valor}>{item.rotulo}</option>)}
        </Select>
        <Select label="Período" value={recorte} onChange={e => { setRecorte(e.target.value as Recorte); setErroPeriodo(undefined) }}>
          <option value="mensal">Mensal</option>
          <option value="anual">Anual</option>
        </Select>
        {recorte === 'mensal'
          ? <TextField label="Mês" type="month" value={mes} max={mesAtual()} error={erroPeriodo}
              onChange={e => setMes(e.target.value)} />
          : <TextField label="Ano" type="number" inputMode="numeric" min={2000} max={anoAtual()} value={ano}
              error={erroPeriodo} onChange={e => setAno(e.target.value)} />}
        <div className="sp-relatorio-acoes">
          <Button type="submit" variant="secondary" icon="eye">Pré-visualizar</Button>
          <Button type="button" variant="primary" loading={baixando === 'PDF'} disabled={baixando !== null}
            onClick={() => baixar('PDF')}>Baixar PDF</Button>
          <Button type="button" variant="primary" loading={baixando === 'CSV'} disabled={baixando !== null}
            onClick={() => baixar('CSV')}>Baixar CSV</Button>
        </div>
      </form>

      {erroDownload && <Alert tone="danger" live="assertive" title="O arquivo não foi gerado.">{erroDownload}</Alert>}

      {pedido === null
        ? <EmptyState title="Escolha o relatório e o período"
            description="Use Pré-visualizar para conferir o conteúdo na tela antes de baixar o arquivo." />
        : consulta.erro
          ? <Alert tone="danger" live="assertive" title="Não foi possível carregar a pré-visualização.">
              Verifique o período escolhido ou tente novamente em alguns instantes.
            </Alert>
          : consulta.dados === null
            ? <p role="status">Gerando a pré-visualização...</p>
            : <PreviaRelatorio relatorio={consulta.dados} />}
    </div>
    <Toast message={mensagem} onDismiss={() => setMensagem('')} />
  </FeaturePage>
}
