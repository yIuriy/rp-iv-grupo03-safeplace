// Pré-visualização de um relatório (RF18, issue #299): o mesmo conteúdo que vai para o CSV e o PDF.
// Indicadores do resumo em cartões e cada seção em tabela. Na coluna "Total" ou "Quantidade" a tela
// desenha uma barra proporcional ao maior valor, só como reforço visual (o número continua escrito).

import { useId } from 'react'
import { Alert, Table, TableRow } from '../../shared/components'
import type { Relatorio, Secao, Tabela } from './api'
import { formatarData, formatarDataHora } from './periodo'

const COLUNAS_COM_BARRA = ['Total', 'Quantidade']

function colunaDaBarra(tabela: Tabela): number {
  const indice = tabela.colunas.findIndex(coluna => COLUNAS_COM_BARRA.includes(coluna))
  if (indice < 0) return -1
  return tabela.linhas.every(linha => /^\d+$/.test(linha[indice] ?? '')) ? indice : -1
}

function TabelaSecao({ secao, tabela }: { secao: Secao; tabela: Tabela }) {
  if (tabela.linhas.length === 0) return <p className="sp-texto-secundario">{tabela.semDados}</p>
  const barra = colunaDaBarra(tabela)
  const maior = barra < 0 ? 0 : Math.max(...tabela.linhas.map(linha => Number(linha[barra])))
  const numericas = tabela.colunas.map((_, c) => tabela.linhas.every(linha => /^\d+$/.test(linha[c] ?? '')))
  const classe = (c: number) => (numericas[c] ? 'sp-relatorio-numero' : undefined)
  return <Table caption={secao.titulo} className="sp-relatorio-tabela">
    <thead><tr>{tabela.colunas.map((coluna, c) => <th key={coluna} scope="col" className={classe(c)}>{coluna}</th>)}</tr></thead>
    <tbody>
      {tabela.linhas.map((linha, i) => <TableRow key={i}>
        {linha.map((celula, c) => <td key={c} className={classe(c)}>
          {c === barra
            ? <span className="sp-relatorio-barra">
                <span className="sp-relatorio-barra-trilho" aria-hidden="true">
                  <span style={{ width: maior > 0 ? `${(Number(celula) / maior) * 100}%` : '0%' }} />
                </span>
                {celula}
              </span>
            : celula}
        </td>)}
      </TableRow>)}
    </tbody>
  </Table>
}

function SecaoRelatorio({ secao }: { secao: Secao }) {
  const idTitulo = useId()
  return <section className="sp-relatorio-secao" aria-labelledby={idTitulo}>
    <h2 id={idTitulo} className="sp-type-heading-sm">{secao.titulo}</h2>
    {secao.indicadores.length > 0 && <dl className="sp-relatorio-indicadores">
      {secao.indicadores.map(indicador => <div key={indicador.rotulo} className="sp-relatorio-indicador">
        <dt>{indicador.rotulo}</dt>
        <dd>{indicador.valor}</dd>
      </div>)}
    </dl>}
    {secao.tabela && <TabelaSecao secao={secao} tabela={secao.tabela} />}
  </section>
}

export function PreviaRelatorio({ relatorio }: { relatorio: Relatorio }) {
  const idTitulo = useId()
  return <article className="sp-relatorio" aria-labelledby={idTitulo}>
    <header className="sp-relatorio-cabecalho">
      <h2 id={idTitulo} className="sp-type-heading-md">{relatorio.titulo}</h2>
      <p className="sp-texto-secundario">
        Período de <time dateTime={relatorio.inicio}>{formatarData(relatorio.inicio)}</time> a{' '}
        <time dateTime={relatorio.fim}>{formatarData(relatorio.fim)}</time>; gerado em{' '}
        <time dateTime={relatorio.geradoEm}>{formatarDataHora(relatorio.geradoEm)}</time>.
      </p>
    </header>
    {relatorio.semRegistros && <Alert tone="info" live="polite" title="Nenhum registro neste período.">
      Confira se as datas cadastradas estão dentro do período escolhido. O recorte anual ou outro mês pode trazer os dados.
    </Alert>}
    {relatorio.secoes.map(secao => <SecaoRelatorio key={secao.titulo} secao={secao} />)}
  </article>
}
