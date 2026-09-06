import { useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { AppShell, NavItem } from '../../shared/layout'
import { Alert, Badge, Button, Checkbox, ConfirmDialog, EmptyState, HistoryItem,
  Icon, IconButton, Radio, SearchInput, Select, Table, TableRow, Tabs, TextArea, TextField, Toast, Toggle,
  type ButtonProps, type IconName } from '../../shared/components'
import { FormExample } from './FormExample'
import './catalog.css'

const figmaUrl = 'https://www.figma.com/design/9tatIII5caffv6gJmdlH3t/Logotipo?node-id=74-99'
const iconNames: IconName[] = ['plus', 'check', 'x', 'info', 'alert-triangle', 'search', 'chevron-down', 'chevron-right', 'arrow-left', 'calendar', 'paperclip', 'clipboard', 'package', 'map-pin', 'users', 'more-horizontal', 'filter', 'loader', 'check-circle']
const buttonKinds: { variant: ButtonProps['variant']; title: string; label: string }[] = [
  { variant: 'primary', title: 'Principal', label: 'Salvar registro' },
  { variant: 'secondary', title: 'Secundário', label: 'Consultar EPI' },
  { variant: 'quiet', title: 'Discreto', label: 'Cancelar' },
  { variant: 'danger', title: 'Destrutivo', label: 'Remover anexo' },
]

function Section({ id, number, title, description, children }: { id: string; number: string; title: string; description: string; children: ReactNode }) {
  return <section id={id} className="ds-section" aria-labelledby={`${id}-title`}>
    <div className="ds-section-heading"><span className="ds-section-number">{number}</span><div><h2 id={`${id}-title`} className="sp-type-heading-md">{title}</h2><p className="ds-muted">{description}</p></div></div>
    {children}
  </section>
}

export function Catalog() {
  const [message, setMessage] = useState('')
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [tab, setTab] = useState('details')
  const [query, setQuery] = useState('')
  const [onlyAvailable, setOnlyAvailable] = useState(false)
  const [selected, setSelected] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [submissions, setSubmissions] = useState(0)
  const equipment = [
    { id: 'EPI-001', name: 'Capacete de segurança', detail: 'Proteção da cabeça', status: 'Disponível', tone: 'success' as const },
    { id: 'EPI-002', name: 'Óculos de proteção', detail: 'Proteção dos olhos', status: 'Emprestado', tone: 'info' as const },
    { id: 'EPI-003', name: 'Protetor auricular', detail: 'Proteção auditiva', status: 'Em manutenção', tone: 'warning' as const },
  ].filter(item => `${item.id} ${item.name}`.toLocaleLowerCase('pt-BR').includes(query.toLocaleLowerCase('pt-BR')) && (!onlyAvailable || item.status === 'Disponível'))

  return <AppShell roleLabel="Biblioteca de interface · v1.0" navigation={<>
    <NavItem href="#start" icon="info">Começar</NavItem>
    <NavItem href="#foundations" icon="clipboard">Identidade visual</NavItem>
    <NavItem href="#buttons" icon="plus">Botões</NavItem>
    <NavItem href="#fields" icon="filter">Campos e seleção</NavItem>
    <NavItem href="#feedback" icon="alert-triangle">Avisos</NavItem>
    <NavItem href="#data" icon="package">Listas e histórico</NavItem>
    <NavItem href="#navigation" icon="map-pin">Navegação e ícones</NavItem>
    <NavItem href="#form-example" icon="check-circle">Formulário</NavItem>
  </>}>
    <div className="ds-catalog">
      <header id="start" className="ds-intro">
        <p className="ds-eyebrow">SAFEPLACE / BIBLIOTECA DE INTERFACE</p>
        <h1 className="sp-type-heading-lg">Design system</h1>
        <p className="ds-intro-description">Os mesmos componentes em todas as telas. Explore os estados, teste as interações e use esta referência para construir o front.</p>
        <div className="ds-actions"><Link className="sp-button sp-button--primary" to="/design-system/examples/risk-areas"><Icon name="map-pin" />Ver tela de referência</Link>
          <a className="sp-button sp-button--secondary" href={figmaUrl} target="_blank" rel="noreferrer">Abrir no Figma<Icon name="chevron-right" /></a></div>
        <div className="ds-quick-start"><p className="sp-field-label">Para usar no código</p><pre><code>{"import { Button, TextField } from './shared/components'\n\n<Button icon=\"plus\" onClick={openForm}>Cadastrar área</Button>\n<TextField label=\"Setor\" name=\"sector\" helper=\"Informe o setor.\" />"}</code></pre><p className="ds-muted">Importe os componentes compartilhados. As cores, fontes, medidas e estados já estão definidos.</p></div>
      </header>

      <Section id="foundations" number="01" title="Identidade visual" description="Cores com função definida, tipografia legível e espaçamento consistente.">
        <div className="ds-swatches">
          {[['brand-primary', 'Ação principal', '#174B3F'], ['brand-soft', 'Seleção', '#DDF2E7'], ['brand-accent', 'Destaque', '#D7EB83'], ['surface-page', 'Fundo', '#F6F7F2'], ['text-primary', 'Texto', '#172B26'], ['text-secondary', 'Texto de apoio', '#52645C']].map(([token, name, hex]) => <div className="ds-swatch" key={token}><span style={{ backgroundColor: `var(--sp-${token})` }} /><strong>{name}</strong><code>{hex}</code><small>--sp-{token}</small></div>)}
        </div>
        <div className="ds-type-samples"><div><p className="ds-eyebrow">MANROPE · TÍTULOS</p><p className="sp-type-heading-md">Segurança começa<br />com informação clara.</p><p className="ds-muted">28 / 36 px · peso 600</p></div><div><p className="ds-eyebrow">INTER · CONTEÚDO</p><p>Consulte os registros, acompanhe as ocorrências e encontre as informações do setor.</p><p className="ds-muted">16 / 24 px · peso 400</p><p className="sp-field-label">Ações e rótulos · 14 / 20 px · peso 600</p></div></div>
        <div className="ds-spacing" aria-label="Escala de espaçamento">{[4, 8, 12, 16, 24, 32, 48, 64, 80].map(size => <div key={size}><span style={{ width: size }} /><code>{size}px</code></div>)}</div>
      </Section>

      <Section id="buttons" number="02" title="Botões e ações" description="Uma ação principal por contexto. Passe o mouse ou use Tab para conferir os estados.">
        <div className="ds-button-grid">{buttonKinds.map(kind => <div key={kind.variant} className="ds-button-column"><h3 className="sp-field-label">{kind.title}</h3><Button variant={kind.variant} onClick={() => kind.variant === 'danger' ? setConfirmOpen(true) : setMessage(`Ação de exemplo: ${kind.label}.`)}>{kind.label}</Button><span className="ds-state-label">Desabilitado</span><Button variant={kind.variant} disabled>{kind.label}</Button><span className="ds-state-label">Carregando</span><Button variant={kind.variant} loading>{kind.label}</Button></div>)}</div>
        <div className="ds-actions"><Button icon="plus" onClick={() => setMessage('Exemplo de ação com ícone.')}>Adicionar EPI</Button><Button variant="quiet" icon="arrow-left" onClick={() => setMessage('Exemplo de ação de retorno.')}>Voltar</Button><IconButton icon="more-horizontal" label="Mais opções de exemplo" onClick={() => setMessage('Use este botão para ações relacionadas ao registro.')} /><IconButton icon="more-horizontal" label="Mais opções indisponíveis" disabled /></div>
        <div className="ds-interaction-example"><div className="ds-actions"><Button loading={loading} onClick={() => { setLoading(true); setSubmissions(count => count + 1) }}>Simular espera</Button><Button variant="secondary" disabled={!loading} onClick={() => setLoading(false)}>Finalizar espera</Button></div><p role="status" className="ds-muted">{submissions} {submissions === 1 ? 'ativação' : 'ativações'}. Durante a espera, o botão bloqueia novos cliques.</p></div>
        <pre><code>{'<Button variant="primary" loading={saving} type="submit">Salvar registro</Button>'}</code></pre>
      </Section>

      <Section id="fields" number="03" title="Campos e seleção" description="O rótulo permanece visível. A mensagem de apoio orienta e o erro explica como corrigir.">
        <div className="ds-field-grid">
          <TextField label="Setor vazio" placeholder="Digite o setor" helper="Informe o setor onde ocorreu o fato." />
          <TextField label="Setor preenchido" defaultValue="Área de carga" helper="Clique no campo para conferir o foco." />
          <TextField label="Setor com erro" placeholder="Digite o setor" error="Informe o setor para continuar." />
          <TextField label="Setor desabilitado" defaultValue="Área de carga" disabled helper="Edição indisponível neste momento." />
          <TextField label="Setor em consulta" value="Área de carga" readOnly helper="Este campo permite apenas leitura." />
          <SearchInput label="Buscar no catálogo" hiddenLabel placeholder="Nome ou código" helper="Ícone original do Figma, com 20 px." />
        </div>
        <div className="ds-two-columns"><TextArea label="Descrição dos fatos" defaultValue="Durante a movimentação de materiais, uma caixa caiu próxima à passagem. A área foi isolada para verificação." helper="Descreva o que observou, sem presumir a causa." /><div className="ds-stack"><Select label="Selecionar setor" defaultValue="" helper="Escolha uma opção cadastrada."><option value="">Selecione o setor</option><option>Área de carga</option><option>Oficina de manutenção</option></Select><Select label="Seleção indisponível" defaultValue="Área de carga" disabled><option>Área de carga</option></Select></div></div>
        <div className="ds-choice-grid"><fieldset><legend>Caixa de seleção</legend><Checkbox label="Selecionar EPI" /><Checkbox label="EPI selecionado" defaultChecked /><Checkbox label="EPI indisponível" disabled /></fieldset><fieldset><legend>Escolha única</legend><Radio label="Incidente" name="catalog-occurrence" value="incident" defaultChecked /><Radio label="Acidente" name="catalog-occurrence" value="accident" /><Radio label="Opção indisponível" name="catalog-occurrence" value="disabled" disabled /></fieldset><fieldset><legend>Alternância</legend><Toggle label="Mostrar somente pendências" /><Toggle label="Receber atualizações" defaultChecked /><Toggle label="Alternância indisponível" disabled /></fieldset></div>
        <pre><code>{'<TextField label="Setor" name="sector" error={errors.sector} />\n<Radio name="type" value="incident" label="Incidente" />'}</code></pre>
      </Section>

      <Section id="feedback" number="04" title="Avisos e respostas" description="Use texto e cor juntos. Preserve o preenchimento quando uma operação falhar.">
        <div className="ds-stack"><Alert title="Modo de consulta offline">Você está vendo dados já sincronizados. As alterações ficam disponíveis quando a conexão voltar.</Alert><Alert tone="success" title="Ocorrência registrada">O protocolo está disponível para consulta. O registro foi encaminhado ao gestor de segurança.</Alert><Alert tone="warning" title="Manutenção pendente">Confira os equipamentos que aguardam manutenção antes de registrar uma nova entrega.</Alert><Alert tone="danger" title="Não foi possível salvar">Os dados preenchidos foram mantidos. Verifique a conexão e tente novamente.</Alert></div>
        <p className="ds-muted">Mensagens acima são amostras visuais de estados do sistema.</p>
        <div className="ds-actions"><Badge tone="success">Disponível</Badge><Badge tone="info">Emprestado</Badge><Badge tone="warning">Em manutenção</Badge><Badge tone="danger">Ação necessária</Badge></div>
        <div className="ds-actions"><Button variant="secondary" onClick={() => setConfirmOpen(true)}>Testar confirmação de saída</Button><Button variant="secondary" onClick={() => setMessage('Aviso de exemplo. Nenhuma alteração foi enviada.')}>Exibir aviso breve</Button></div>
        <pre><code>{'<Alert tone="danger" title="Não foi possível salvar" live="assertive">\n  Os dados preenchidos foram mantidos. Tente novamente.\n</Alert>'}</code></pre>
      </Section>

      <Section id="data" number="05" title="Listas e histórico" description="Identificação clara, situação por escrito e uma saída quando a busca não encontra resultados.">
        <div className="ds-data-toolbar"><SearchInput label="Buscar EPI" placeholder="Buscar EPI por nome ou código" value={query} onChange={event => setQuery(event.target.value)} /><Toggle label="Somente disponíveis" checked={onlyAvailable} onChange={event => setOnlyAvailable(event.target.checked)} /></div>
        {equipment.length > 0 ? <Table caption="EPIs de exemplo"><thead><tr><th scope="col"><span className="sp-sr-only">Seleção</span></th><th scope="col">Equipamento</th><th scope="col">Código</th><th scope="col">Situação</th><th scope="col"><span className="sp-sr-only">Ações</span></th></tr></thead><tbody>{equipment.map(item => <TableRow key={item.id}><td><Checkbox label={`Selecionar ${item.name}`} className="ds-table-checkbox" checked={selected.includes(item.id)} onChange={event => setSelected(previous => event.target.checked ? [...previous, item.id] : previous.filter(id => id !== item.id))} /></td><td><strong>{item.name}</strong><small>{item.detail}</small></td><td>{item.id}</td><td><Badge tone={item.tone}>{item.status}</Badge></td><td><IconButton label={`Consultar ${item.name}`} icon="chevron-right" onClick={() => setMessage(`Exemplo: ${item.name}, ${item.status.toLowerCase()}.`)} /></td></TableRow>)}</tbody></Table> : <EmptyState title="Nenhum EPI encontrado" description="Revise a busca ou remova o filtro de situação." actionLabel="Limpar busca" onAction={() => { setQuery(''); setOnlyAvailable(false) }} />}
        <p className="ds-muted" role="status">{selected.length} {selected.length === 1 ? 'equipamento selecionado' : 'equipamentos selecionados'}.</p>
        <ol className="ds-history"><HistoryItem dateTime="2026-09-04T14:30:00-03:00" dateLabel="04 set 2026 · 14:30" title="Manutenção concluída">Equipamento registrado como apto para uso após a verificação.</HistoryItem><HistoryItem dateTime="2026-09-03T10:15:00-03:00" dateLabel="03 set 2026 · 10:15" title="Encaminhado para manutenção">Verificação solicitada durante a devolução do equipamento.</HistoryItem></ol>
      </Section>

      <Section id="navigation" number="06" title="Navegação e ícones" description="Nomes visíveis, seleção clara e os mesmos 19 ícones do arquivo de design.">
        <Tabs label="Informações do equipamento" value={tab} onValueChange={setTab} items={[
          { id: 'details', label: 'Dados do EPI', content: <p>Capacete de segurança · EPI-001. Use as setas do teclado para alternar entre as abas.</p> },
          { id: 'history', label: 'Histórico', content: <p>O histórico reúne os registros do equipamento em ordem cronológica inversa.</p> },
          { id: 'loans', label: 'Empréstimos', content: <p>Nenhum empréstimo neste exemplo.</p> },
        ]} />
        <div className="ds-icon-grid">{iconNames.map(name => <div key={name}><Icon name={name} /><code>{name}</code></div>)}</div>
        <p className="ds-muted">Ícones acompanham a cor do texto. Botões sem texto precisam de um nome acessível.</p>
        <pre><code>{'<IconButton icon="more-horizontal" label="Mais opções do EPI" />'}</code></pre>
      </Section>

      <Section id="form-example" number="07" title="Formulário em uso" description="Um exemplo completo para testar seleção, campos obrigatórios, anexos e espera durante o envio."><FormExample /></Section>
      <footer className="ds-footer"><p>SafePlace · Design system 1.0</p><a href={figmaUrl} target="_blank" rel="noreferrer">Consultar o arquivo de referência</a></footer>
    </div>
    <div className="ds-toast-position"><Toast message={message} onDismiss={() => setMessage('')} /></div>
    <ConfirmDialog open={confirmOpen} title="Descartar alterações?" description="O registro ainda não foi enviado. Ao sair, o que foi preenchido será perdido."
      confirmLabel="Descartar alterações" onClose={() => setConfirmOpen(false)} onConfirm={() => { setConfirmOpen(false); setMessage('Confirmação testada. Nenhum dado real foi alterado.') }} />
  </AppShell>
}
