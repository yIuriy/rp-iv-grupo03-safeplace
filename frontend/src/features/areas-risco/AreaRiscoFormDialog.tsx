// Cadastro e edição de área de risco pelo Gestor (UC03): código, grau, setor físico, agentes,
// limites e EPIs obrigatórios, com justificativa na edição.

import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Button, Checkbox, Dialog, Select, TextArea, TextField } from '../../shared/components'
import { mensagemDaApi } from '../../shared/api/erros'
import type { Epi } from '../epis/api'
import {
  atualizarAreaRisco,
  cadastrarAreaRisco,
  descreverSetor,
  niveisPerigo,
  rotuloNivelPerigo,
  type AreaRisco,
  type NivelPerigo,
  type Setor,
} from './api'

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

  const [codigo, setCodigo] = useState('')
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const [nivelPerigo, setNivelPerigo] = useState<NivelPerigo>('MEDIO')
  const [setorId, setSetorId] = useState<string>('')
  const [agentesTexto, setAgentesTexto] = useState('')
  const [limites, setLimites] = useState('')
  const [episSelecionados, setEpisSelecionados] = useState<number[]>([])
  const [justificativa, setJustificativa] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (open) {
      if (area) {
        setCodigo(area.codigo)
        setNome(area.nome)
        setDescricao(area.descricao ?? '')
        setNivelPerigo(area.nivelPerigo)
        setSetorId(area.setorId !== null ? String(area.setorId) : '')
        setAgentesTexto(area.agentesRisco.join(', '))
        setLimites(area.limites ?? '')
        setEpisSelecionados(area.episObrigatorios.map(e => e.id))
        setJustificativa('')
      } else {
        setCodigo('')
        setNome('')
        setDescricao('')
        setNivelPerigo('MEDIO')
        setSetorId('')
        setAgentesTexto('')
        setLimites('')
        setEpisSelecionados([])
        setJustificativa('')
      }
      setErro(null)
      setEnviando(false)
    }
  }, [open, area])

  function alternarEpi(id: number) {
    setEpisSelecionados(atual =>
      atual.includes(id) ? atual.filter(item => item !== id) : [...atual, id]
    )
  }

  async function submeter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro(null)

    if (!codigo.trim()) {
      setErro('O código da área é obrigatório.')
      return
    }
    if (!nome.trim()) {
      setErro('O nome da área é obrigatório.')
      return
    }
    if (episSelecionados.length === 0) {
      setErro('A área de risco exige ao menos um EPI obrigatório para acesso.')
      return
    }

    const agentesRisco = agentesTexto
      .split(',')
      .map(a => a.trim())
      .filter(Boolean)
    const parsedSetorId = setorId ? Number(setorId) : undefined

    setEnviando(true)
    try {
      if (editando && area) {
        const atualizada = await atualizarAreaRisco(area.id, {
          nome: nome.trim(),
          descricao: descricao.trim() || undefined,
          nivelPerigo,
          setorId: parsedSetorId,
          agentesRisco,
          limites: limites.trim() || undefined,
          episObrigatoriosIds: episSelecionados,
          justificativa: justificativa.trim() || undefined,
        })
        aoSalvar(atualizada, 'atualizado')
      } else {
        const criada = await cadastrarAreaRisco({
          codigo: codigo.trim(),
          nome: nome.trim(),
          descricao: descricao.trim() || undefined,
          nivelPerigo,
          setorId: parsedSetorId,
          agentesRisco,
          limites: limites.trim() || undefined,
          episObrigatoriosIds: episSelecionados,
        })
        aoSalvar(criada, 'criado')
      }
      aoFechar()
    } catch (err: unknown) {
      setErro(mensagemDaApi(err) || 'Não foi possível salvar a área de risco. Verifique os dados.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog
      open={open}
      title={editando ? `Editar área: ${area?.codigo}` : 'Cadastrar área de risco'}
      description={
        editando
          ? 'Atualize perigos, agentes, limites e os EPIs obrigatórios para acesso (UC03).'
          : 'Cadastre um novo setor ou área de risco com as diretrizes e EPIs de acesso (UC03).'
      }
      onClose={aoFechar}
    >
      <form onSubmit={submeter} className="sp-dialog-body" noValidate>
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
          label="Limites físicos ou operacionais (opcional)"
          value={limites}
          onChange={e => setLimites(e.target.value)}
          placeholder="Ex: Entre os pilares A e B, piso térreo"
          helper="Demarcação dos limites para orientar o acesso seguro."
        />

        <TextField
          label="Agentes de risco identificados (opcional)"
          value={agentesTexto}
          onChange={e => setAgentesTexto(e.target.value)}
          placeholder="Ex: Ruído, Poeira, Alta tensão"
          helper="Separe múltiplos agentes por vírgula."
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
    </Dialog>
  )
}
