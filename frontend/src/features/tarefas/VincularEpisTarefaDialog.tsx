// Modal de vinculação de EPIs obrigatórios a uma tarefa (RF12, UC08, US12).
// Exclusivo do Gestor de Segurança: seleciona quais EPIs do catálogo são obrigatórios.

import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Button, Checkbox, Dialog } from '../../shared/components'
import { mensagemDaApi } from '../../shared/api/erros'
import { listarEpis, type Epi } from '../epis/api'
import {
  associarEpisTarefa,
  listarEpisTarefa,
  type Tarefa,
} from './api'

export function VincularEpisTarefaDialog({
  tarefa,
  aoFechar,
  aoSalvar,
}: {
  tarefa: Tarefa | null
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  const [episDisponiveis, setEpisDisponiveis] = useState<Epi[]>([])
  const [episSelecionados, setEpisSelecionados] = useState<number[]>([])
  const [carregando, setCarregando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (tarefa) {
      setErro(null)
      setCarregando(true)
      if (tarefa.episIds) {
        setEpisSelecionados(tarefa.episIds)
      }
      Promise.all([
        listarEpis(),
        tarefa.episIds ? Promise.resolve([]) : listarEpisTarefa(tarefa.id),
      ])
        .then(([catalogo, episVinculados]) => {
          setEpisDisponiveis(catalogo.filter(e => e.status !== 'DESCARTADO'))
          if (!tarefa.episIds) {
            setEpisSelecionados(episVinculados.map(e => e.id))
          }
        })
        .catch(err => {
          setErro(
            mensagemDaApi(err) ||
              'Não foi possível carregar os EPIs. Verifique sua conexão.'
          )
        })
        .finally(() => {
          setCarregando(false)
        })
    } else {
      setEpisDisponiveis([])
      setEpisSelecionados([])
      setErro(null)
    }
  }, [tarefa])

  if (!tarefa) return null

  function alternarEpi(id: number) {
    setEpisSelecionados(atuais =>
      atuais.includes(id) ? atuais.filter(item => item !== id) : [...atuais, id]
    )
  }

  async function submeter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro(null)
    setEnviando(true)

    try {
      const atualizada = await associarEpisTarefa(tarefa!.id, episSelecionados)
      aoSalvar(atualizada)
      aoFechar()
    } catch (err: unknown) {
      setErro(
        mensagemDaApi(err) ||
          'Não foi possível associar os EPIs à tarefa. Tente novamente.'
      )
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog
      open={!!tarefa}
      title="Vincular EPIs obrigatórios"
      description="Selecione os equipamentos de proteção individual necessários para a realização desta atividade operacional (RF12, UC08)."
      onClose={aoFechar}
    >
      <form onSubmit={submeter} className="sp-dialog-body" noValidate>
        {erro && (
          <Alert tone="danger" title="Não foi possível salvar" live="assertive">
            {erro}
          </Alert>
        )}

        <div className="sp-campo-somente-leitura">
          <p className="sp-field-label">Descrição da atividade</p>
          <p className="sp-descricao-tarefa">{tarefa.descricao}</p>
        </div>

        <fieldset className="sp-field">
          <legend className="sp-field-label">
            EPIs de uso obrigatório
          </legend>
          <p className="sp-field-message">
            Marque todos os EPIs exigidos para executar esta tarefa.
          </p>

          <div
            className="sp-escolha-multipla"
            style={{
              maxHeight: '220px',
              overflowY: 'auto',
              display: 'flex',
              flexDirection: 'column',
              gap: '8px',
              marginTop: '8px',
              padding: '4px',
              border: '1px solid var(--sp-color-border, #e5e7eb)',
              borderRadius: '6px',
            }}
          >
            {carregando ? (
              <p className="sp-texto-secundario" style={{ padding: '8px' }}>
                Carregando catálogo de EPIs...
              </p>
            ) : episDisponiveis.length === 0 ? (
              <p className="sp-texto-secundario" style={{ padding: '8px' }}>
                Nenhum EPI disponível no catálogo.
              </p>
            ) : (
              episDisponiveis.map(epi => (
                <Checkbox
                  key={epi.id}
                  label={`${epi.nome} (CA ${epi.numeroCa})`}
                  checked={episSelecionados.includes(epi.id)}
                  onChange={() => alternarEpi(epi.id)}
                  disabled={enviando}
                />
              ))
            )}
          </div>
          {!carregando && episDisponiveis.length > 0 && (
            <p className="sp-texto-secundario" style={{ marginTop: '8px' }}>
              {episSelecionados.length}{' '}
              {episSelecionados.length === 1 ? 'EPI selecionado' : 'EPIs selecionados'}
            </p>
          )}
        </fieldset>

        <div className="sp-dialog-actions">
          <Button type="button" variant="secondary" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary" disabled={enviando || carregando}>
            {enviando ? 'Salvando...' : 'Salvar EPIs'}
          </Button>
        </div>
      </form>
    </Dialog>
  )
}
