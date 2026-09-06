import { useRef, useState, type FormEvent } from 'react'
import { Alert, Button, FileUpload, Radio, Select, TextArea, Toast } from '../../shared/components'

export function FormExample() {
  const [errors, setErrors] = useState<{ sector?: string; description?: string }>({})
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [message, setMessage] = useState('')
  const sectorRef = useRef<HTMLSelectElement>(null)
  const descriptionRef = useRef<HTMLTextAreaElement>(null)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (saving) return
    const form = new FormData(event.currentTarget)
    if (!String(form.get('sector') ?? '').trim() || !String(form.get('description') ?? '').trim()) {
      setErrors({
        sector: !String(form.get('sector') ?? '').trim() ? 'Selecione o setor para continuar.' : undefined,
        description: !String(form.get('description') ?? '').trim() ? 'Descreva os fatos antes de enviar o registro.' : undefined,
      })
      if (!form.get('sector')) sectorRef.current?.focus()
      else descriptionRef.current?.focus()
      return
    }
    setErrors({})
    setSaving(true)
    // Only simulates latency in this catalog. Feature forms must await their API.
    await new Promise(resolve => window.setTimeout(resolve, 900))
    setSaving(false)
    setSaved(true)
    setMessage('Demonstração concluída. Nenhum registro foi enviado.')
  }

  return <form className="ds-form" onSubmit={submit} noValidate onReset={() => {
    setErrors({}); setSaved(false); setMessage('')
  }}>
    <p className="ds-muted">Exemplo interativo com dados fictícios. Preencha os campos e teste o envio.</p>
    <fieldset disabled={saving}>
      <legend className="sp-field-label">Tipo de ocorrência</legend>
      <div className="ds-actions"><Radio name="occurrence-type" value="incident" label="Incidente" defaultChecked />
        <Radio name="occurrence-type" value="accident" label="Acidente" /></div>
    </fieldset>
    <Select label="Setor (obrigatório)" name="sector" ref={sectorRef} required defaultValue=""
      helper="Informe o setor onde ocorreu o fato." disabled={saving}
      error={errors.sector}>
      <option value="">Selecione o setor</option><option value="loading">Área de carga</option><option value="workshop">Oficina de manutenção</option>
    </Select>
    <TextArea label="Descrição dos fatos (obrigatório)" name="description" ref={descriptionRef} required
      placeholder="Descreva o que aconteceu." helper="Descreva o que observou, sem presumir a causa."
      disabled={saving} error={errors.description} />
    <FileUpload label="Anexos" name="attachments" multiple helper="A seleção é apenas demonstrativa; os arquivos não são enviados." disabled={saving} />
    {(errors.sector || errors.description) && <Alert tone="danger" title="Confira os campos obrigatórios" live="assertive">Os dados preenchidos foram mantidos. Corrija os campos indicados e tente novamente.</Alert>}
    <div className="ds-actions ds-actions--end">
      <Button type="reset" variant="quiet" disabled={saving}>Limpar exemplo</Button>
      <Button type="submit" loading={saving} icon="check">Testar envio</Button>
    </div>
    {saved && <p className="ds-muted">Campos validados. Este formulário não está conectado ao sistema.</p>}
    <Toast message={message} onDismiss={() => setMessage('')} />
  </form>
}
