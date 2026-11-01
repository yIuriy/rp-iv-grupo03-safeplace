// Cadastro e edição de tarefa operacional (UC11): descrição, grau de perigo, área de risco e setores. O
// fluxo de envio vem do hook useEnvioFormulario (Template Method, #233); aqui ficam a validação e as
// chamadas à API, inclusive a reclassificação do grau na edição.

import { useState, useEffect } from 'react'
import { Alert, Button, Checkbox, Dialog, Select, TextArea } from '../../shared/components'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'
import type { Validacao } from '../../shared/formularios/envioFormulario'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import {
  listarAreasRisco,
  listarSetores,
  type AreaRisco,
  type Setor,
} from '../areas-risco/api'
import {
  atualizarTarefa,
  cadastrarTarefa,
  classificarTarefa,
  niveisPerigo,
  rotuloNivelPerigo,
  type NivelPerigo,
  type Tarefa,
} from './api'

type ErrosTarefa = { geral?: string }

/** Mesma regra e mensagem de antes: só a descrição é obrigatória. */
function validarTarefa(rascunho: { descricao: string; areaRiscoId: string }): Validacao<
  { descricao: string; areaRiscoId?: number },
  ErrosTarefa
> {
  const descricao = rascunho.descricao.trim()
  if (!descricao) return { erros: { geral: 'A descrição da atividade é obrigatória.' } }
  return { erros: {}, dados: { descricao, areaRiscoId: rascunho.areaRiscoId ? Number(rascunho.areaRiscoId) : undefined } }
}

export function TarefaFormDialog({
  open,
  tarefa,
  aoFechar,
  aoSalvar,
}: {
  open: boolean
  tarefa?: Tarefa | null
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  const editando = !!tarefa

  return (
    <Dialog
      open={open}
      title={editando ? `Editar tarefa: #${tarefa.id}` : 'Cadastrar nova tarefa'}
      description={
        editando
          ? 'Atualize a descrição, área de risco e setores físicos vinculados à tarefa operacional.'
          : 'Informe a descrição da atividade operacional, área de risco e setores físicos.'
      }
      onClose={aoFechar}
    >
      {open && (
        <FormularioTarefa
          key={tarefa?.id ?? 'nova'}
          tarefa={tarefa}
          aoFechar={aoFechar}
          aoSalvar={aoSalvar}
        />
      )}
    </Dialog>
  )
}

function FormularioTarefa({
  tarefa,
  aoFechar,
  aoSalvar,
}: {
  tarefa?: Tarefa | null
  aoFechar: () => void
  aoSalvar: (tarefa: Tarefa) => void
}) {
  const editando = !!tarefa
  const [descricao, setDescricao] = useState(tarefa?.descricao ?? '')
  const [nivelPerigo, setNivelPerigo] = useState<string>(tarefa?.nivelPerigo ?? '')
  const [areaRiscoId, setAreaRiscoId] = useState<string>(
    tarefa?.areaRiscoId ? String(tarefa.areaRiscoId) : ''
  )
  const [setoresSelecionados, setSetoresSelecionados] = useState<number[]>(
    tarefa?.setoresIds ?? []
  )

  const [areas, setAreas] = useState<AreaRisco[]>([])
  const [setores, setSetores] = useState<Setor[]>([])
  // O formulário é recriado a cada abertura (key), então já nasce carregando as relações.
  const [carregandoRelacoes, setCarregandoRelacoes] = useState(true)

  useEffect(() => {
    let ativo = true
    Promise.all([listarAreasRisco(), listarSetores()])
      .then(([listaAreas, listaSetores]) => {
        if (ativo) {
          setAreas(listaAreas)
          setSetores(listaSetores)
        }
      })
      .catch(() => {
        // Falha tolerante na busca de áreas/setores
      })
      .finally(() => {
        if (ativo) setCarregandoRelacoes(false)
      })

    return () => {
      ativo = false
    }
  }, [])

  function alternarSetor(id: number) {
    setSetoresSelecionados(atuais =>
      atuais.includes(id) ? atuais.filter(s => s !== id) : [...atuais, id]
    )
  }

  const { enviando, erros, falha, onSubmit } = useEnvioFormulario({
    ler: () => ({ descricao, areaRiscoId }),
    validar: validarTarefa,
    ordem: [],
    enviar: async ({ descricao: descricaoTarefa, areaRiscoId: areaId }) => {
      if (editando && tarefa) {
        let resultado = await atualizarTarefa(tarefa.id, {
          descricao: descricaoTarefa,
          areaRiscoId: areaId,
          setoresIds: setoresSelecionados,
        })
        if (nivelPerigo && nivelPerigo !== (tarefa.nivelPerigo ?? '')) {
          resultado = await classificarTarefa(tarefa.id, {
            nivelPerigo: nivelPerigo as NivelPerigo,
          })
        }
        aoSalvar(resultado)
      } else {
        const criada = await cadastrarTarefa({
          descricao: descricaoTarefa,
          nivelPerigo: nivelPerigo ? (nivelPerigo as NivelPerigo) : undefined,
          areaRiscoId: areaId,
          setoresIds: setoresSelecionados,
        })
        aoSalvar(criada)
      }
      aoFechar()
    },
    descreverFalha: criarDescritorDeFalha({}, usarMensagemDaApi(
      `Não foi possível ${editando ? 'atualizar' : 'cadastrar'} a tarefa. Verifique os dados informados.`
    )),
  }, {} as ErrosTarefa)
  // A validação devolve uma mensagem única; ela aparece no mesmo alerta da falha da API.
  const erro = erros.geral || falha

  return (
    <form onSubmit={onSubmit} className="sp-dialog-body" noValidate>
      {erro && (
        <Alert tone="danger" title="Não foi possível salvar" live="assertive">
          {erro}
        </Alert>
      )}

      <TextArea
        label="Descrição da atividade operacional *"
        value={descricao}
        onChange={e => setDescricao(e.target.value)}
        placeholder="Ex: Solda em altura na estrutura metálica do galpão de estoque..."
        rows={3}
        required
        autoFocus
      />

      <Select
        label="Área de risco (opcional)"
        value={areaRiscoId}
        onChange={e => setAreaRiscoId(e.target.value)}
        helper="Vincule a tarefa a uma área de risco delimitada se aplicável."
      >
        <option value="">Sem área de risco específica</option>
        {areas.map(a => (
          <option key={a.id} value={a.id}>
            {a.codigo} - {a.nome}
          </option>
        ))}
      </Select>

      <fieldset style={{ border: '1px solid var(--sp-border, #e2e8f0)', borderRadius: '6px', padding: '0.75rem' }}>
        <legend style={{ fontSize: '0.875rem', fontWeight: 600, padding: '0 0.25rem' }}>
          Setores físicos vinculados (opcional)
        </legend>
        {carregandoRelacoes ? (
          <p className="sp-texto-secundario">Carregando setores físicos...</p>
        ) : setores.length === 0 ? (
          <p className="sp-texto-secundario">Nenhum setor físico cadastrado.</p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '0.5rem', marginTop: '0.5rem' }}>
            {setores.map(s => (
              <Checkbox
                key={s.id}
                label={s.nome}
                checked={setoresSelecionados.includes(s.id)}
                onChange={() => alternarSetor(s.id)}
              />
            ))}
          </div>
        )}
      </fieldset>

      {(!editando || !tarefa?.classificada) && (
        <Select
          label="Grau de periculosidade (opcional)"
          value={nivelPerigo}
          onChange={e => setNivelPerigo(e.target.value)}
          helper="Selecione caso a atividade já tenha sido classificada tecnicamente."
        >
          <option value="">Não classificada no momento</option>
          {niveisPerigo.map(nivel => (
            <option key={nivel} value={nivel}>
              {rotuloNivelPerigo[nivel]}
            </option>
          ))}
        </Select>
      )}

      <div className="sp-dialog-actions">
        <Button type="button" variant="secondary" onClick={aoFechar} disabled={enviando}>
          Cancelar
        </Button>
        <Button type="submit" variant="primary" disabled={enviando}>
          {enviando
            ? 'Salvando...'
            : editando
            ? 'Salvar alterações'
            : 'Cadastrar tarefa'}
        </Button>
      </div>
    </form>
  )
}
