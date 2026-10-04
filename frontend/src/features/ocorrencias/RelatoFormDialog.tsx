// Formulário de relato de ocorrência (UC09, relato rápido; UC05, cadastro) e de edição pelo Gestor
// (UC05, alternativo IV). Distingue acidente de incidente, pede a data do fato, o setor e a área de
// risco do setor, o colaborador envolvido e a descrição, e só os campos próprios de cada tipo. Valida
// antes do envio com foco no primeiro campo pendente e mantém os dados quando a API recusa.

import { useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Radio, Select, TextArea, TextField } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { descreverSetor, listarAreasRisco, listarSetores, niveisPerigo, rotuloNivelPerigo, type AreaRisco, type Setor } from '../areas-risco/api'
import { listarColaboradores } from '../usuarios/api'
import {
  atualizarOcorrencia,
  descreverFalhaDeOcorrencia,
  registrarOcorrencia,
  type OcorrenciaResumo,
  type TipoOcorrencia,
} from './api'
import { rotuloTipoOcorrencia } from './formatos'
import { agoraLocal, lerRelato, ordemDosCamposDoRelato, validarRelato, type ErrosRelato } from './validacaoRelato'

const carregarColaboradores = (signal: AbortSignal) => listarColaboradores({}, signal)

const causas = [
  { valor: 'FATOR_HUMANO', rotulo: 'Fator humano' },
  { valor: 'FALHA_EPI', rotulo: 'Falha de EPI' },
  { valor: 'OUTRO', rotulo: 'Outra causa' },
] as const

export function RelatoFormDialog({ aberto, ocorrencia, aoFechar, aoSalvar }: {
  aberto: boolean
  /** Presente na edição: o tipo fica fixo e os campos começam com os dados registrados. */
  ocorrencia?: OcorrenciaResumo | null
  aoFechar: () => void
  aoSalvar: (ocorrencia: OcorrenciaResumo) => void
}) {
  const edicao = Boolean(ocorrencia)
  return <Dialog open={aberto} onClose={aoFechar}
    title={ocorrencia ? `Editar ocorrência #${ocorrencia.idOcorrencia}` : 'Relatar ocorrência'}
    description={ocorrencia
      ? 'Altere os dados do relato. Tipo, data do registro, situação e protocolo são preservados, e a alteração fica registrada na auditoria.'
      : 'Relato rápido de acidente ou incidente. Campos marcados com * são obrigatórios; a data e a hora do registro e quem registrou são gravados automaticamente.'}>
    {aberto && <Formulario key={ocorrencia?.idOcorrencia ?? 'novo'} ocorrencia={ocorrencia ?? null}
      edicao={edicao} aoFechar={aoFechar} aoSalvar={aoSalvar} />}
  </Dialog>
}

function Formulario({ ocorrencia, edicao, aoFechar, aoSalvar }: {
  ocorrencia: OcorrenciaResumo | null
  edicao: boolean
  aoFechar: () => void
  aoSalvar: (ocorrencia: OcorrenciaResumo) => void
}) {
  const setores = useConsulta('setores-relato', listarSetores)
  const areas = useConsulta('areas-relato', listarAreasRisco)
  const colaboradores = useConsulta('colaboradores-relato', carregarColaboradores)

  if (setores.erro || areas.erro || colaboradores.erro) {
    return <div className="sp-dialog-body">
      <Alert tone="danger" title="Não foi possível carregar setores, áreas ou colaboradores" live="assertive">
        Sem essas listas o relato não pode ser preenchido. Verifique sua conexão e tente novamente.
      </Alert>
      <div className="sp-dialog-actions">
        <Button variant="secondary" onClick={aoFechar}>Cancelar</Button>
        <Button onClick={() => { setores.recarregar(); areas.recarregar(); colaboradores.recarregar() }}>Tentar novamente</Button>
      </div>
    </div>
  }
  if (!setores.dados || !areas.dados || !colaboradores.dados) {
    return <p className="sp-dialog-body" role="status">Carregando setores, áreas e colaboradores...</p>
  }
  const areaAtual = ocorrencia?.areaId ? areas.dados.find(area => area.id === ocorrencia.areaId) : undefined
  return <Campos ocorrencia={ocorrencia} edicao={edicao} aoFechar={aoFechar} aoSalvar={aoSalvar}
    setores={setores.dados} areas={areas.dados} colaboradores={colaboradores.dados}
    setorInicial={areaAtual?.setorId ? String(areaAtual.setorId) : ''} />
}

function Campos({ ocorrencia, edicao, aoFechar, aoSalvar, setores, areas, colaboradores, setorInicial }: {
  ocorrencia: OcorrenciaResumo | null
  edicao: boolean
  aoFechar: () => void
  aoSalvar: (ocorrencia: OcorrenciaResumo) => void
  setores: Setor[]
  areas: AreaRisco[]
  colaboradores: { id: number; nome: string }[]
  setorInicial: string
}) {
  const [tipo, setTipo] = useState<TipoOcorrencia | ''>(ocorrencia?.tipoOcorrencia ?? '')
  const [setorId, setSetorId] = useState(setorInicial)
  const [areaId, setAreaId] = useState(ocorrencia?.areaId ? String(ocorrencia.areaId) : '')
  const [erros, setErros] = useState<ErrosRelato>({})
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)
  const agora = agoraLocal()

  const areasDoSetor = setorId ? areas.filter(area => area.setorId === Number(setorId)) : []

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const form = event.currentTarget
    const leitura = lerRelato(new FormData(form), ocorrencia?.tipoOcorrencia)
    const pendencias = validarRelato(leitura, agoraLocal())
    setErros(pendencias)
    const primeiro = ordemDosCamposDoRelato.find(campo => pendencias[campo])
    if (primeiro) {
      const campo = form.elements.namedItem(primeiro)
      const alvo = campo instanceof RadioNodeList ? campo[0] : campo
      if (alvo instanceof HTMLElement) alvo.focus()
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      const salva = ocorrencia
        ? await atualizarOcorrencia(ocorrencia.idOcorrencia, leitura.dados)
        : await registrarOcorrencia(leitura.tipo as TipoOcorrencia, leitura.dados)
      aoSalvar(salva)
    } catch (erro) {
      setFalha(descreverFalhaDeOcorrencia(erro))
      setEnviando(false)
    }
  }

  return <form className="sp-form sp-relato" onSubmit={enviar} noValidate aria-label={edicao ? 'Editar ocorrência' : 'Relatar ocorrência'}>
    {edicao && ocorrencia
      ? <p className="sp-texto-secundario">Tipo: <strong>{rotuloTipoOcorrencia[ocorrencia.tipoOcorrencia]}</strong> (não muda na edição)</p>
      : <fieldset className="sp-fieldset" aria-describedby={erros.tipoOcorrencia ? 'relato-tipo-erro' : undefined}>
        <legend className="sp-field-label">Tipo de ocorrência *</legend>
        <div className="sp-opcoes">
          <Radio label="Acidente (houve lesão ou dano)" name="tipoOcorrencia" value="ACIDENTE"
            checked={tipo === 'ACIDENTE'} onChange={() => setTipo('ACIDENTE')} aria-invalid={erros.tipoOcorrencia ? true : undefined} />
          <Radio label="Incidente (quase acidente ou situação de risco)" name="tipoOcorrencia" value="INCIDENTE"
            checked={tipo === 'INCIDENTE'} onChange={() => setTipo('INCIDENTE')} aria-invalid={erros.tipoOcorrencia ? true : undefined} />
        </div>
        {erros.tipoOcorrencia && <p id="relato-tipo-erro" role="alert" className="sp-field-message sp-field-message--error">{erros.tipoOcorrencia}</p>}
      </fieldset>}

    <div className="sp-relato-grade">
      <TextField label="Data e hora do fato *" name="dataOcorrencia" type="datetime-local" max={agora} required
        defaultValue={ocorrencia?.dataOcorrencia.slice(0, 16)} error={erros.dataOcorrencia}
        helper="Quando o fato aconteceu. O horário do registro é gravado à parte." />
      <Select label="Colaborador envolvido *" name="colaboradorId" required error={erros.colaboradorId}
        defaultValue={ocorrencia?.colaboradorId ? String(ocorrencia.colaboradorId) : ''}
        helper={colaboradores.length === 0 ? 'Nenhum colaborador cadastrado. Cadastre-o em Usuários antes de relatar.' : undefined}>
        <option value="">Selecione o colaborador</option>
        {colaboradores.map(pessoa => <option key={pessoa.id} value={pessoa.id}>{pessoa.nome}</option>)}
      </Select>
      <Select label="Setor *" name="setorId" required value={setorId} error={erros.setorId}
        onChange={event => { setSetorId(event.target.value); setAreaId('') }}>
        <option value="">Selecione o setor</option>
        {setores.map(setor => <option key={setor.id} value={setor.id}>{descreverSetor(setor)}</option>)}
      </Select>
      <Select label="Área de risco *" name="areaId" required value={areaId} error={erros.areaId}
        disabled={!setorId} onChange={event => setAreaId(event.target.value)}
        helper={!setorId ? 'Escolha o setor para listar as áreas.'
          : areasDoSetor.length === 0 ? 'Este setor não tem áreas de risco cadastradas.' : undefined}>
        <option value="">Selecione a área</option>
        {areasDoSetor.map(area => <option key={area.id} value={area.id}>{area.nome}</option>)}
      </Select>
    </div>

    <TextArea label="Descrição dos fatos *" name="descricao" rows={4} required error={erros.descricao}
      defaultValue={ocorrencia?.descricao} helper="O que aconteceu, onde e como. Não é preciso investigar a causa raiz agora." />

    {tipo === 'ACIDENTE' && <fieldset className="sp-fieldset sp-relato-grade">
      <legend className="sp-field-label">Dados do acidente</legend>
      <Select label="Causa *" name="causaRaiz" required error={erros.causaRaiz} defaultValue={ocorrencia?.causaRaiz ?? ''}>
        <option value="">Selecione a causa</option>
        {causas.map(causa => <option key={causa.valor} value={causa.valor}>{causa.rotulo}</option>)}
      </Select>
      <Select label="Gravidade *" name="gravidade" required error={erros.gravidade} defaultValue={ocorrencia?.gravidade ?? ''}
        helper={erros.gravidade ? undefined : 'Alta ou crítica exige ação preventiva no plano de ação (UC07).'}>
        <option value="">Selecione a gravidade</option>
        {niveisPerigo.map(nivel => <option key={nivel} value={nivel}>{rotuloNivelPerigo[nivel]}</option>)}
      </Select>
      <TextField label="Tipo de lesão" name="tipo" defaultValue={ocorrencia?.tipo ?? ''} placeholder="Ex.: corte, queimadura" />
      <TextField label="Dano" name="dano" defaultValue={ocorrencia?.dano ?? ''} placeholder="Ex.: lesão na mão direita" />
      <TextField label="Destino do acidentado" name="destino" defaultValue={ocorrencia?.destino ?? ''} placeholder="Ex.: ambulatório, hospital" />
    </fieldset>}

    {tipo === 'INCIDENTE' && <fieldset className="sp-fieldset sp-relato-grade">
      <legend className="sp-field-label">Dados do incidente</legend>
      <TextField label="Situação de risco *" name="situacaoRisco" required error={erros.situacaoRisco}
        defaultValue={ocorrencia?.situacaoRisco ?? ''} placeholder="Ex.: piso escorregadio" />
      <TextField label="Potencial de dano *" name="potencialDano" required error={erros.potencialDano}
        defaultValue={ocorrencia?.potencialDano ?? ''} placeholder="Ex.: queda com fratura" />
    </fieldset>}

    {falha && <Alert tone="danger" title={edicao ? 'Alterações não salvas' : 'Relato não registrado'} live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoFechar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">{edicao ? 'Salvar alterações' : 'Enviar relato'}</Button>
    </div>
  </form>
}
