// Cadastro e edição de área de risco pelo Gestor (UC03): código, grau, setor físico, agentes,
// limites e EPIs obrigatórios, com justificativa na edição. O fluxo de envio vem do hook useEnvioFormulario
// (Template Method, #233); aqui ficam a leitura do rascunho, a validação e as chamadas à API.

import { useState } from 'react'
import { Alert, Button, Checkbox, Dialog, Select, TextArea, TextField } from '../../shared/components'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'
import type { Validacao } from '../../shared/formularios/envioFormulario'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import type { Epi } from '../epis/epi'
import {
  atualizarAreaRisco,
  cadastrarAreaRisco,
  descreverSetor,
  niveisPerigo,
  rotuloNivelPerigo,
  type AreaRisco,
  type DadosAtualizacaoAreaRisco,
  type NivelPerigo,
  type Setor,
} from './api'

const descreverFalha = criarDescritorDeFalha({},
  usarMensagemDaApi('Não foi possível salvar a área de risco. Verifique os dados.'))

type ErrosArea = { geral?: string }
type DadosArea = DadosAtualizacaoAreaRisco & { codigo: string }
type RascunhoArea = {
  codigo: string
  nome: string
  descricao: string
  nivelPerigo: NivelPerigo
  setorId: string
  agentesTexto: string
  limites: string
  episSelecionados: number[]
  justificativa: string
}

/** Mesmas regras e mensagens de antes, na mesma ordem: a primeira pendência encontrada é a exibida. */
function validarArea(rascunho: RascunhoArea): Validacao<DadosArea, ErrosArea> {
  if (!rascunho.codigo.trim()) return { erros: { geral: 'O código da área é obrigatório.' } }
  if (!rascunho.nome.trim()) return { erros: { geral: 'O nome da área é obrigatório.' } }
  if (rascunho.episSelecionados.length === 0) {
    return { erros: { geral: 'A área de risco exige ao menos um EPI obrigatório para acesso.' } }
  }
  const agentesRisco = rascunho.agentesTexto.split(',').map(a => a.trim()).filter(Boolean)
  if (agentesRisco.length === 0) return { erros: { geral: 'Ao menos um agente de risco deve ser informado.' } }
  if (!rascunho.limites.trim()) return { erros: { geral: 'Os limites físicos ou operacionais são obrigatórios.' } }
  return {
    erros: {},
    dados: {
      codigo: rascunho.codigo.trim(),
      nome: rascunho.nome.trim(),
      descricao: rascunho.descricao.trim() || undefined,
      nivelPerigo: rascunho.nivelPerigo,
      setorId: rascunho.setorId ? Number(rascunho.setorId) : undefined,
      agentesRisco,
      limites: rascunho.limites.trim(),
      episObrigatoriosIds: rascunho.episSelecionados,
      justificativa: rascunho.justificativa.trim() || undefined,
    },
  }
}

export function AreaRiscoFormDialog({
  open,
  area,
  setores,
  epis,
  aoFechar,
  aoSalvar,
}: {
  open: boolean
  area?: AreaRisco | null
  setores: Setor[]
  epis: Epi[]
  aoFechar: () => void
  aoSalvar: (area: AreaRisco, modo: 'criado' | 'atualizado') => void
}) {
  const editando = !!area

  return (
    <Dialog
      open={open}
      title={editando ? `Editar área: ${area?.codigo}` : 'Cadastrar área de risco'}
      description={
        editando
          ? 'Atualize perigos, agentes, limites e os EPIs obrigatórios para acesso.'
          : 'Cadastre um novo setor ou área de risco com as diretrizes e EPIs de acesso.'
      }
      onClose={aoFechar}
    >
      {open && (
        <FormularioArea
          key={area?.id ?? 'nova'}
          area={area ?? null}
          setores={setores}
          epis={epis}
          aoFechar={aoFechar}
          aoSalvar={aoSalvar}
        />
      )}
    </Dialog>
  )
}

function FormularioArea({
  area,
  setores,
  epis,
  aoFechar,
  aoSalvar,
}: {
  area: AreaRisco | null
  setores: Setor[]
  epis: Epi[]
  aoFechar: () => void
  aoSalvar: (area: AreaRisco, modo: 'criado' | 'atualizado') => void
}) {
  const editando = !!area

  const [codigo, setCodigo] = useState(area?.codigo ?? '')
  const [nome, setNome] = useState(area?.nome ?? '')
  const [descricao, setDescricao] = useState(area?.descricao ?? '')
  const [nivelPerigo, setNivelPerigo] = useState<NivelPerigo>(area?.nivelPerigo ?? 'MEDIO')
  const [setorId, setSetorId] = useState<string>(area && area.setorId !== null ? String(area.setorId) : '')
  const [agentesTexto, setAgentesTexto] = useState(area ? area.agentesRisco.join(', ') : '')
  const [limites, setLimites] = useState(area?.limites ?? '')
  const [episSelecionados, setEpisSelecionados] = useState<number[]>(area ? area.episObrigatorios.map(e => e.id) : [])
  const [justificativa, setJustificativa] = useState('')

  function alternarEpi(id: number) {
    setEpisSelecionados(atual =>
      atual.includes(id) ? atual.filter(item => item !== id) : [...atual, id]
    )
  }

  const { enviando, erros, falha, onSubmit } = useEnvioFormulario({
    ler: () => ({ codigo, nome, descricao, nivelPerigo, setorId, agentesTexto, limites, episSelecionados, justificativa }),
    validar: validarArea,
    ordem: [],
    enviar: async ({ codigo: codigoArea, justificativa: justificativaEdicao, ...comuns }) => {
      if (editando && area) {
        aoSalvar(await atualizarAreaRisco(area.id, { ...comuns, justificativa: justificativaEdicao }), 'atualizado')
      } else {
        aoSalvar(await cadastrarAreaRisco({ codigo: codigoArea, ...comuns }), 'criado')
      }
      aoFechar()
    },
    descreverFalha,
  }, {} as ErrosArea)
  // A validação devolve uma mensagem única; ela aparece no mesmo alerta da falha da API.
  const erro = erros.geral || falha

  return (
      <form onSubmit={onSubmit} className="sp-dialog-body" noValidate>
        {erro && (
          <Alert tone="danger" title="Não foi possível salvar" live="assertive">
            {erro}
          </Alert>
        )}

        <div className="sp-form-grid">
          <TextField
            label="Código da área *"
            value={codigo}
            onChange={e => setCodigo(e.target.value)}
            disabled={editando}
            helper={editando ? 'O código da área é fixo após o cadastro.' : 'Ex: SET-01, DOCA-02'}
            required
          />

          <TextField
            label="Nome da área *"
            value={nome}
            onChange={e => setNome(e.target.value)}
            placeholder="Nome descritivo da área ou setor"
            required
          />

          <Select
            label="Grau de perigo *"
            value={nivelPerigo}
            onChange={e => setNivelPerigo(e.target.value as NivelPerigo)}
            helper="Vocabulário aprovado de classificação de risco."
            required
          >
            {niveisPerigo.map(nivel => (
              <option key={nivel} value={nivel}>
                {rotuloNivelPerigo[nivel]}
              </option>
            ))}
          </Select>

          <Select
            label="Setor físico (opcional)"
            value={setorId}
            onChange={e => setSetorId(e.target.value)}
            helper="Vincule à planta industrial se aplicável."
          >
            <option value="">Sem setor físico específico</option>
            {setores.map(s => (
              <option key={s.id} value={s.id}>
                {descreverSetor(s)}
              </option>
            ))}
          </Select>
        </div>

        <TextField
          label="Limites físicos ou operacionais *"
          value={limites}
          onChange={e => setLimites(e.target.value)}
          placeholder="Ex: Entre os pilares A e B, piso térreo"
          helper="Demarcação dos limites para orientar o acesso seguro (obrigatório)."
          required
        />

        <TextField
          label="Agentes de risco identificados *"
          value={agentesTexto}
          onChange={e => setAgentesTexto(e.target.value)}
          placeholder="Ex: Ruído, Poeira, Alta tensão"
          helper="Separe múltiplos agentes por vírgula (ao menos um obrigatório)."
          required
        />

        <TextArea
          label="Descrição detalhada (opcional)"
          value={descricao}
          onChange={e => setDescricao(e.target.value)}
          placeholder="Informações adicionais sobre as operações realizadas no local..."
          rows={3}
        />

        <fieldset className="sp-field">
          <legend className="sp-field-label">
            EPIs obrigatórios para acesso à área *
          </legend>
          <p className="sp-field-message">
            Selecione todos os EPIs de uso obrigatório para entrada neste setor (mínimo 1).
          </p>
          <div className="sp-escolha-multipla" style={{ maxHeight: '180px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' }}>
            {epis.length === 0 ? (
              <p className="sp-texto-secundario">Nenhum EPI disponível no catálogo.</p>
            ) : (
              epis.map(epi => (
                <Checkbox
                  key={epi.id}
                  label={`${epi.nome} (CA ${epi.numeroCa})`}
                  checked={episSelecionados.includes(epi.id)}
                  onChange={() => alternarEpi(epi.id)}
                />
              ))
            )}
          </div>
        </fieldset>

        {editando && (
          <TextField
            label="Justificativa da alteração (opcional)"
            value={justificativa}
            onChange={e => setJustificativa(e.target.value)}
            placeholder="Ex: Revisão periódica de perigos do setor"
            helper="Histórico para auditoria de segurança."
          />
        )}

        <div className="sp-dialog-actions">
          <Button type="button" variant="secondary" onClick={aoFechar} disabled={enviando}>
            Cancelar
          </Button>
          <Button type="submit" variant="primary" disabled={enviando}>
            {enviando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Cadastrar área'}
          </Button>
        </div>
      </form>
  )
}
