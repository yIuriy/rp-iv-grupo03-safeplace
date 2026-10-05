// Seção "Minhas ações recentes" da tela /perfil (RNF05): tabela com as ações que a pessoa logada
// registrou, com data, tipo da operação, registro afetado e a descrição gravada na auditoria.

import { Badge, Table, TableRow } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarDataHora } from '../estoque/formatos'
import { ResultadoConsulta } from './ResultadoConsulta'
import {
  descreverRegistro, LIMITE_HISTORICO, listarMinhasAcoes, rotuloOperacao, tomOperacao, type TipoOperacao,
} from './apiHistorico'

function Operacao({ tipo }: { tipo: string }) {
  const conhecido = tipo in rotuloOperacao ? (tipo as TipoOperacao) : null
  return conhecido ? <Badge tone={tomOperacao[conhecido]}>{rotuloOperacao[conhecido]}</Badge> : <span>{tipo}</span>
}

export function HistoricoAcoes() {
  const consulta = useConsulta('minhas-acoes', listarMinhasAcoes)

  return <section className="sp-perfil-historico" aria-labelledby="perfil-historico-titulo">
    <h2 id="perfil-historico-titulo" className="sp-type-heading-sm">Minhas ações recentes</h2>
    <p className="sp-texto-secundario">
      As {LIMITE_HISTORICO} ações mais recentes registradas com o seu e-mail na trilha de auditoria.
    </p>
    <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando ações..."
      erroTitulo="Não foi possível carregar as suas ações."
      vazio={{ title: 'Nenhuma ação registrada', description: 'Cadastros e alterações que você fizer aparecerão aqui.' }}>
      {acoes => <Table caption="Minhas ações recentes">
        <thead>
          <tr>
            <th scope="col">Data e hora</th>
            <th scope="col">Ação</th>
            <th scope="col">Registro</th>
            <th scope="col">Descrição</th>
          </tr>
        </thead>
        <tbody>
          {acoes.map(acao => <TableRow key={acao.id}>
            <td>{formatarDataHora(acao.dataHora)}</td>
            <td><Operacao tipo={acao.tipoOperacao} /></td>
            <td>{descreverRegistro(acao)}</td>
            <td className="sp-perfil-acao-descricao">{acao.descricao ?? <span className="sp-texto-secundario">Sem descrição</span>}</td>
          </TableRow>)}
        </tbody>
      </Table>}
    </ResultadoConsulta>
  </section>
}
