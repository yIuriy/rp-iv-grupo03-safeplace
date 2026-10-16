// Modal de visualização de exigências da tarefa pelo Supervisor e Gestor (US06 critério 4):
// exibe grau de periculosidade, EPIs obrigatórios e capacitações normativas registradas.

import { useEffect, useState } from 'react'
import { Alert, Badge, Button, Dialog, Icon, Table, TableRow } from '../../shared/components'
import { mensagemDaApi } from '../../shared/api/erros'
import { tomDoNivelPerigo } from '../areas-risco/api'
import {
  obterExigenciasTarefa,
  rotuloNivelPerigo,
  type ExigenciasTarefa,
  type Tarefa,
} from './api'

export function ExigenciasTarefaDialog({
  tarefa,
  aoFechar,
}: {
  tarefa: Tarefa | null
  aoFechar: () => void
}) {
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [exigencias, setExigencias] = useState<ExigenciasTarefa | null>(null)

  useEffect(() => {
    if (!tarefa) {
      setExigencias(null)
      setErro(null)
      return
    }

    const controller = new AbortController()
    setCarregando(true)
    setErro(null)

    obterExigenciasTarefa(tarefa.id, controller.signal)
      .then(dados => {
        setExigencias(dados)
      })
      .catch(err => {
        if (!controller.signal.aborted) {
          setErro(mensagemDaApi(err) || 'Não foi possível carregar as exigências da tarefa.')
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setCarregando(false)
        }
      })

    return () => controller.abort()
  }, [tarefa])

  return (
    <Dialog
      open={!!tarefa}
      title="Exigências da Atividade"
      description={
        tarefa
          ? `Consulte as diretrizes de periculosidade, EPIs e capacitações obrigatórias para: "${tarefa.descricao}".`
          : undefined
      }
      onClose={aoFechar}
      className="sp-dialog--largo sp-dialog--exigencias"
    >
      <div className="sp-dialog-body">
        {carregando && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '1rem 0' }}>
            <Icon name="loader" />
            <span role="status">Consultando exigências registradas...</span>
          </div>
        )}

        {erro && (
          <Alert tone="danger" title="Falha ao carregar" live="assertive">
            {erro}
          </Alert>
        )}

        {!carregando && exigencias && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <strong>Grau de periculosidade:</strong>
              {exigencias.nivelPerigo ? (
                <Badge tone={tomDoNivelPerigo[exigencias.nivelPerigo]}>
                  {rotuloNivelPerigo[exigencias.nivelPerigo]}
                </Badge>
              ) : (
                <span className="sp-texto-secundario">Pendente de classificação</span>
              )}
            </div>

            <section>
              <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem' }}>
                EPIs Obrigatórios ({exigencias.episObrigatorios.length})
              </h3>
              {exigencias.episObrigatorios.length === 0 ? (
                <p className="sp-texto-secundario">
                  Nenhum EPI obrigatório vinculado a esta atividade operacional.
                </p>
              ) : (
                <Table caption="EPIs obrigatórios para execução da tarefa">
                  <thead>
                    <tr>
                      <th scope="col">Equipamento</th>
                      <th scope="col">Número CA</th>
                      <th scope="col">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {exigencias.episObrigatorios.map(epi => (
                      <TableRow key={epi.id}>
                        <td><strong>{epi.nome}</strong></td>
                        <td>{epi.numeroCa}</td>
                        <td>{epi.status}</td>
                      </TableRow>
                    ))}
                  </tbody>
                </Table>
              )}
            </section>

            <section>
              <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem' }}>
                Capacitações e Treinamentos Normativos ({exigencias.capacitacoes.length})
              </h3>
              {exigencias.capacitacoes.length === 0 ? (
                <p className="sp-texto-secundario">
                  Nenhuma exigência normativa de capacitação registrada para esta tarefa.
                </p>
              ) : (
                <Table caption="Exigências normativas de capacitação">
                  <thead>
                    <tr>
                      <th scope="col">Capacitação</th>
                      <th scope="col">Tipo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {exigencias.capacitacoes.map(item => (
                      <TableRow key={item.id}>
                        <td><strong>{item.nomeCapacitacao}</strong></td>
                        <td>
                          {item.tipoCapacitacao === 'CERTIFICACAO'
                            ? 'Certificação'
                            : 'Treinamento'}
                        </td>
                      </TableRow>
                    ))}
                  </tbody>
                </Table>
              )}
            </section>
          </div>
        )}

        <div className="sp-dialog-actions" style={{ marginTop: '1.5rem' }}>
          <Button variant="secondary" onClick={aoFechar}>
            Fechar
          </Button>
        </div>
      </div>
    </Dialog>
  )
}
