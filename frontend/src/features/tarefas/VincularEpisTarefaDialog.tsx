// Modal de vinculação de EPIs obrigatórios a uma tarefa (RF12, UC08, US12).
// Exclusivo do Gestor de Segurança: seleciona quais EPIs do catálogo são obrigatórios.

import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Button, Checkbox, Dialog, Select } from '../../shared/components'
import { mensagemDaApi } from '../../shared/api/erros'
import {
  classificacoesEpi,
  listarEpis,
  rotuloClassificacaoEpi,
  type Epi,
} from '../epis/api'
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
  if (!tarefa) return null
  return <DialogoVinculo key={tarefa.id} tarefa={tarefa} aoFechar={aoFechar} aoSalvar={aoSalvar} />
}

function DialogoVinculo({
  tarefa,
  aoFechar,
  aoSalvar,
}: {
  tarefa: Tarefa
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  const [episDisponiveis, setEpisDisponiveis] = useState<Epi[]>([])
  const [episSelecionados, setEpisSelecionados] = useState<number[]>(tarefa.episIds ?? [])
  const [filtroClassificacao, setFiltroClassificacao] = useState<string>('')
  const [carregando, setCarregando] = useState(true)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
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
  }, [tarefa])

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
      const atualizada = await associarEpisTarefa(tarefa.id, episSelecionados)
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

  const episFiltrados = episDisponiveis.filter(epi => {
    if (!filtroClassificacao) return true
    if (filtroClassificacao === 'SEM_CLASSIFICACAO') return !epi.classificacao
    return epi.classificacao === filtroClassificacao
  })

  return (
    <Dialog
      open
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

        <Select
          label="Filtrar por classificação do EPI"
          value={filtroClassificacao}
          onChange={e => setFiltroClassificacao(e.target.value)}
          helper="Filtre os EPIs do catálogo por tipo de proteção para facilitar a busca."
        >
          <option value="">Todas as classificações</option>
          {classificacoesEpi.map(classificacao => (
            <option key={classificacao} value={classificacao}>
              {rotuloClassificacaoEpi[classificacao]}
            </option>
          ))}
          <option value="SEM_CLASSIFICACAO">Outros / Sem classificação</option>
        </Select>

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
            ) : episFiltrados.length === 0 ? (
              <p className="sp-texto-secundario" style={{ padding: '8px' }}>
                {filtroClassificacao
                  ? 'Nenhum EPI encontrado para a classificação selecionada.'
                  : 'Nenhum EPI disponível no catálogo.'}
              </p>
            ) : (
              episFiltrados.map(epi => (
                <Checkbox
                  key={epi.id}
                  label={`${epi.nome} (CA ${epi.numeroCa})${
                    epi.classificacao
                      ? ` — ${rotuloClassificacaoEpi[epi.classificacao]}`
                      : ''
                  }`}
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
              {episSelecionados.length === 1
                ? 'EPI selecionado no total'
                : 'EPIs selecionados no total'}
              {filtroClassificacao &&
                ` (${episFiltrados.length} ${
                  episFiltrados.length === 1 ? 'visível' : 'visíveis'
                })`}
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
