// Aba "Setores físicos" do módulo de áreas de risco (UC03 passo 2, issue #174): lista os setores
// com grau de perigo e áreas vinculadas; o Gestor cadastra novos setores, o Supervisor consulta.

import { useState } from 'react'
import { Badge, Button, Table, TableRow, Toast } from '../../shared/components'
import type { ConsultaOffline } from '../../shared/offline/useConsultaOffline'
import { OfflineBanner } from '../../shared/offline/OfflineBanner'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import { rotuloNivelPerigo, tomDoNivelPerigo, type AreaRisco, type Setor } from './api'
import { SetorFormDialog } from './SetorFormDialog'

/**
 * Quantas áreas de risco apontam para o setor (associação `AreaRisco [0..*] -- Setor`). Sem a lista
 * de áreas, a contagem fica indisponível em vez de mostrar zero.
 */
function areasVinculadas(setor: Setor, areas: AreaRisco[] | null): string {
  if (areas === null) return 'Indisponível'
  const total = areas.filter(area => area.setorId === setor.id).length
  return total === 1 ? '1 área' : `${total} áreas`
}

export function SetoresPainel({ consulta, areas, gestor, aoCadastrar }: {
  consulta: ConsultaOffline<Setor[]>
  areas: AreaRisco[] | null
  gestor: boolean
  aoCadastrar: (setor: Setor) => void
}) {
  const [cadastroAberto, setCadastroAberto] = useState(false)
  const [setorEmEdicao, setSetorEmEdicao] = useState<Setor | null>(null)
  const [aviso, setAviso] = useState('')

  return <div className="sp-painel">
    <OfflineBanner isOffline={consulta.isOffline} sincronizadoEm={consulta.sincronizadoEm} />
    <div className="sp-painel-acoes">
      <p>Setores físicos agrupam as áreas de risco da planta. O grau de perigo do setor também orienta a projeção de substituição de EPIs.</p>
      {gestor && <Button icon="plus" onClick={() => setCadastroAberto(true)} disabled={consulta.isOffline}
        title={consulta.isOffline ? 'Cadastro indisponível em modo offline' : undefined}>Cadastrar setor</Button>}
    </div>
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando setores..."
      erroTitulo="Não foi possível carregar os setores."
      vazio={gestor
        ? { title: 'Nenhum setor cadastrado', description: 'Cadastre o primeiro setor para vincular as áreas de risco à planta.' }
        : { title: 'Nenhum setor cadastrado', description: 'O Gestor de Segurança ainda não cadastrou setores físicos.' }}>
      {setores => <Table caption="Setores físicos cadastrados">
        <thead><tr>
          <th scope="col">Setor</th><th scope="col">Localização</th><th scope="col">Grau de perigo</th>
          <th scope="col">Áreas de risco</th><th scope="col">Descrição</th>
          {gestor && <th scope="col">Ações</th>}
        </tr></thead>
        <tbody>{setores.map(setor => <TableRow key={setor.id}>
          <td><strong>{setor.nome}</strong></td>
          <td>{setor.localizacao ?? 'Não informada'}</td>
          <td><Badge tone={tomDoNivelPerigo[setor.status]}>{rotuloNivelPerigo[setor.status]}</Badge></td>
          <td>{areasVinculadas(setor, areas)}</td>
          <td>{setor.descricao ?? 'Sem descrição'}</td>
          {gestor && <td>
            <Button variant="secondary" onClick={() => setSetorEmEdicao(setor)} disabled={consulta.isOffline}
              title={consulta.isOffline ? 'Edição indisponível em modo offline' : undefined}>
              Editar
            </Button>
          </td>}
        </TableRow>)}</tbody>
      </Table>}
    </ResultadoConsulta>
    {gestor && <>
      <SetorFormDialog open={cadastroAberto} aoFechar={() => setCadastroAberto(false)}
        aoSalvar={setor => {
          setAviso(`Setor ${setor.nome} cadastrado. Ele já pode ser escolhido no cadastro de áreas de risco.`)
          aoCadastrar(setor)
        }} />
      <SetorFormDialog open={!!setorEmEdicao} setor={setorEmEdicao} aoFechar={() => setSetorEmEdicao(null)}
        aoSalvar={setor => {
          setAviso(`Setor ${setor.nome} atualizado com sucesso.`)
          consulta.recarregar()
        }} />
    </>}
    <Toast message={aviso} onDismiss={() => setAviso('')} />
  </div>
}
