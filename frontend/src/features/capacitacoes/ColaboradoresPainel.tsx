// Listagem de colaboradores do UC02 (passo 2) com o filtro por situação das certificações (cenário
// alternativo IV), aplicado pela API, e busca local por nome. Quem não tem certificação aparece com
// essa ausência informada (cenário III; US10, critério 5).

import { useCallback, useState, type FormEvent } from 'react'
import { Badge, Button, SearchInput, Select, Table, TableRow } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { normalizar } from '../../shared/formatos'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import { listarColaboradores, situacoesCertificacao, type ResumoColaborador, type SituacaoCertificacao } from './api'
import { plural, rotuloDaSituacao, rotuloDoFiltro, situacaoDoResumo } from './formatos'

function vazioDoFiltro(situacao: SituacaoCertificacao | null) {
  if (!situacao) {
    return { title: 'Nenhum colaborador cadastrado', description: 'Os colaboradores cadastrados em Usuários aparecerão aqui com a situação das capacitações.' }
  }
  return {
    title: `Nenhum colaborador com certificações ${rotuloDaSituacao[situacao].toLocaleLowerCase('pt-BR')}`,
    description: 'Nenhum colaborador corresponde à situação escolhida. Escolha outra situação ou limpe o filtro.',
  }
}

export function ColaboradoresPainel({ situacao, aoFiltrar, aoConsultar }: {
  situacao: SituacaoCertificacao | null
  aoFiltrar: (situacao: SituacaoCertificacao | null) => void
  aoConsultar: (colaboradorId: number) => void
}) {
  const [termo, setTermo] = useState('')
  const carregar = useCallback((signal: AbortSignal) => listarColaboradores(situacao, signal), [situacao])
  const consulta = useConsulta(`capacitacoes-colaboradores:${situacao ?? 'todas'}`, carregar)

  function buscar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setTermo(String(new FormData(event.currentTarget).get('nome') ?? ''))
  }

  return <div className="sp-painel">
    <form className="sp-busca" role="search" aria-label="Filtrar colaboradores por capacitação" onSubmit={buscar}
      onReset={() => { setTermo(''); aoFiltrar(null) }}>
      <SearchInput label="Nome do colaborador" name="nome" hiddenLabel={false} placeholder="Parte do nome" autoComplete="off" />
      <Select label="Situação das certificações" name="situacao" value={situacao ?? ''}
        onChange={event => aoFiltrar((event.target.value || null) as SituacaoCertificacao | null)}>
        <option value="">Todas, incluindo sem certificações</option>
        {situacoesCertificacao.map(valor => <option key={valor} value={valor}>{rotuloDoFiltro(valor)}</option>)}
      </Select>
      <Button type="submit" variant="secondary" icon="search">Buscar</Button>
      <Button type="reset" variant="quiet">Limpar</Button>
    </form>
    <p className="sp-texto-secundario">
      A situação de cada colaborador é a da certificação mais crítica. O cadastro de certificações e treinamentos
      ainda não está disponível nesta tela.
    </p>
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando colaboradores..."
      erroTitulo="Não foi possível carregar as capacitações dos colaboradores." vazio={vazioDoFiltro(situacao)}>
      {colaboradores => {
        const busca = normalizar(termo.trim())
        const visiveis = busca ? colaboradores.filter(c => normalizar(c.colaboradorNome).includes(busca)) : colaboradores
        return visiveis.length === 0
          ? <p role="status">Nenhum colaborador corresponde ao nome informado. Ajuste a busca ou limpe o filtro.</p>
          : <TabelaColaboradores colaboradores={visiveis} aoConsultar={aoConsultar} />
      }}
    </ResultadoConsulta>
  </div>
}

function TabelaColaboradores({ colaboradores, aoConsultar }: {
  colaboradores: ResumoColaborador[]
  aoConsultar: (colaboradorId: number) => void
}) {
  return <Table caption="Situação das capacitações por colaborador">
    <thead><tr>
      <th scope="col">Colaborador</th><th scope="col">Situação das certificações</th>
      <th scope="col">Capacitações registradas</th><th scope="col">Ações</th>
    </tr></thead>
    <tbody>{colaboradores.map(colaborador => {
      const situacao = situacaoDoResumo(colaborador)
      return <TableRow key={colaborador.colaboradorId}
        className={colaborador.situacaoCertificacoes === 'VENCIDA' ? 'sp-linha-critica' : undefined}>
        <td><strong>{colaborador.colaboradorNome}</strong></td>
        <td>
          <Badge tone={situacao.tom}>{situacao.rotulo}</Badge>
          {colaborador.pendenteIntegracaoAdmissional && <><br />
            <span className="sp-texto-secundario">Pendência de integração e treinamentos admissionais</span></>}
        </td>
        <td>
          {plural(colaborador.totalCertificacoes, 'certificação', 'certificações')}
          <br /><span className="sp-texto-secundario">{plural(colaborador.totalTreinamentos, 'treinamento', 'treinamentos')}</span>
        </td>
        <td><Button variant="secondary" onClick={() => aoConsultar(colaborador.colaboradorId)}
          aria-label={`Consultar capacitações de ${colaborador.colaboradorNome}`}>Consultar</Button></td>
      </TableRow>
    })}</tbody>
  </Table>
}
