// Tela /epis (RF03, UC06, US03; issues #123 e #133): catálogo com saldo e situação crítica,
// busca por nome, detalhes com histórico e, para o Gestor de Segurança, registro de entrada e
// saída. O Supervisor consulta; a movimentação segue a mesma matriz de papéis do SecurityConfig.

import { useState, type FormEvent } from 'react'
import { Button, SearchInput, Toast } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useConsulta } from '../../shared/api/useConsulta'
import { useSessaoAtiva } from '../autenticacao/contexto'
import { listarEpis, type Epi, type Movimentacao } from './api'
import { EpiDetalheDialog } from './EpiDetalheDialog'
import { EpisTabela } from './EpisTabela'
import { MovimentacaoDialog } from './MovimentacaoDialog'
import { rotuloDoTipo } from './formatos'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import './epis.css'

function filtrar(epis: Epi[], termo: string): Epi[] {
  const busca = termo.trim().toLocaleLowerCase()
  if (!busca) return epis
  return epis.filter(epi => epi.nome.toLocaleLowerCase().includes(busca)
    || (epi.localizacao ?? '').toLocaleLowerCase().includes(busca))
}

function mensagemDeMovimentacao(epi: Epi, mov: Movimentacao): string {
  const unidades = mov.quantidade === 1 ? 'unidade' : 'unidades'
  return `${rotuloDoTipo[mov.tipo]} de ${mov.quantidade} ${unidades} registrada para ${epi.nome}. Saldo atual: ${mov.saldoAtual}.`
}

export function EpiPage() {
  const sessao = useSessaoAtiva()
  const gestor = sessao.perfil === 'GESTOR_SEGURANCA'
  const [termo, setTermo] = useState('')
  const [detalhando, setDetalhando] = useState<Epi | null>(null)
  const [movimentando, setMovimentando] = useState<Epi | null>(null)
  const [versaoHistorico, setVersaoHistorico] = useState(0)
  const [aviso, setAviso] = useState('')
  const consulta = useConsulta('epis', listarEpis)

  function buscar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setTermo(String(new FormData(event.currentTarget).get('busca') ?? ''))
  }

  function aoMovimentar(epi: Epi, mov: Movimentacao) {
    setAviso(mensagemDeMovimentacao(epi, mov))
    setVersaoHistorico(v => v + 1)
    consulta.recarregar()
  }

  const criticos = consulta.dados?.filter(epi => epi.estoqueCritico).length ?? 0

  return <FeaturePage title="EPIs"
    description={gestor
      ? 'Consulte o catálogo, o saldo e o histórico de cada EPI e registre entradas e saídas de estoque.'
      : 'Consulte o catálogo, o saldo e o histórico de cada EPI. Entradas e saídas são registradas pelo Gestor de Segurança.'}>
    <div className="sp-painel">
      <form className="sp-busca" role="search" aria-label="Buscar EPIs" onSubmit={buscar} onReset={() => setTermo('')}>
        <SearchInput label="Nome ou localização" name="busca" hiddenLabel={false} placeholder="Parte do nome ou da localização" autoComplete="off" />
        <span />
        <Button type="submit" variant="secondary" icon="search">Buscar</Button>
        <Button type="reset" variant="quiet">Limpar</Button>
      </form>
      {criticos > 0 && <p role="status" className="sp-texto-secundario">
        {criticos === 1 ? '1 EPI com estoque crítico' : `${criticos} EPIs com estoque crítico`} (saldo igual ou abaixo do mínimo).
      </p>}
      <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando EPIs..."
        erroTitulo="Não foi possível carregar o estoque de EPIs."
        vazio={{ title: 'Nenhum EPI cadastrado', description: 'O catálogo aparecerá aqui assim que houver EPIs cadastrados.' }}>
        {epis => {
          const visiveis = filtrar(epis, termo)
          return visiveis.length === 0
            ? <p role="status">Nenhum EPI corresponde à busca. Ajuste o termo ou limpe o filtro.</p>
            : <EpisTabela epis={visiveis} podeMovimentar={gestor} aoDetalhar={setDetalhando} aoMovimentar={setMovimentando} />
        }}
      </ResultadoConsulta>
      <EpiDetalheDialog epi={detalhando} versao={versaoHistorico} aoFechar={() => setDetalhando(null)} />
      <MovimentacaoDialog epi={movimentando} aoFechar={() => setMovimentando(null)}
        aoSalvar={mov => { if (movimentando) aoMovimentar(movimentando, mov) }} />
      <Toast message={aviso} onDismiss={() => setAviso('')} />
    </div>
  </FeaturePage>
}
