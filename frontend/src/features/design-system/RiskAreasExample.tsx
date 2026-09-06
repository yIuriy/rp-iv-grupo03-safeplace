import { useState } from 'react'
import { Link } from 'react-router-dom'
import { AppShell, NavItem } from '../../shared/layout'
import { Button, Dialog, EmptyState, RiskAreaCard, Select, TextField, type FeedbackTone } from '../../shared/components'
import './catalog.css'

const areas = [
  { id: 'SET-002', title: 'Área de carga', location: 'SET-002 • Doca de recebimento',
    hazards: 'Movimentação de materiais e circulação de veículos.', ppe: 'Capacete de segurança; calçado de proteção.',
    riskLabel: 'Alto', riskTone: 'danger' as FeedbackTone },
  { id: 'SET-005', title: 'Oficina de manutenção', location: 'SET-005 • Bancadas 01 a 04',
    hazards: 'Contato com peças e uso de ferramentas durante os serviços.', ppe: 'Óculos de proteção; luvas de proteção.',
    riskLabel: 'Médio', riskTone: 'warning' as FeedbackTone },
]

function normalize(text: string) {
  return text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR')
}

export function RiskAreasExample() {
  const [query, setQuery] = useState('')
  const [level, setLevel] = useState('')
  const [selected, setSelected] = useState<(typeof areas)[number] | null>(null)
  const [registerOpen, setRegisterOpen] = useState(false)
  const filtered = areas.filter(area => normalize(`${area.title} ${area.location}`).includes(normalize(query.trim())) && (!level || area.riskLabel === level))

  return <AppShell roleLabel="Gestor de segurança" navigation={<>
    <NavItem href="/design-system#form-example" icon="clipboard">Ocorrências</NavItem>
    <NavItem href="/design-system#data" icon="package">EPIs</NavItem>
    <NavItem href="/design-system/examples/risk-areas" icon="map-pin" active>Áreas de risco</NavItem>
    <NavItem href="/design-system#navigation" icon="users">Colaboradores</NavItem>
  </>}>
    <div className="ds-reference">
      <div className="ds-page-heading">
        <div><h1 className="sp-type-heading-md">Áreas de risco</h1><p className="ds-muted">Consulte os perigos e a proteção vinculada a cada setor.</p></div>
        <Button icon="plus" className="ds-register-button" onClick={() => setRegisterOpen(true)}>Cadastrar área</Button>
      </div>
      <div className="ds-reference-filters">
        <TextField label="Buscar setor" placeholder="Nome, código ou localização" helper="Consulte as áreas cadastradas."
          value={query} onChange={event => setQuery(event.target.value)} />
        <Select label="Grau de perigo" helper="Baixo, médio, alto ou crítico." value={level} onChange={event => setLevel(event.target.value)}>
          <option value="">Todos os graus</option><option>Baixo</option><option>Médio</option><option>Alto</option><option>Crítico</option>
        </Select>
      </div>
      <p className="ds-result-count" role="status">{filtered.length} {filtered.length === 1 ? 'setor encontrado' : 'setores cadastrados'}</p>
      {filtered.length ? <div className="ds-risk-grid">{filtered.map(area => <RiskAreaCard key={area.id} {...area} onConsult={() => setSelected(area)} />)}</div>
        : <EmptyState title="Nenhuma área encontrada" description="Tente outro nome ou altere o grau de perigo." actionLabel="Limpar filtros" onAction={() => { setQuery(''); setLevel('') }} />}
      <footer className="ds-example-note"><span>Dados fictícios para demonstração dos componentes.</span><Link to="/design-system">Voltar ao catálogo</Link></footer>
    </div>
    <Dialog open={!!selected} title={selected?.title ?? 'Área de risco'} description="Dados de exemplo. A classificação exibida não define uma avaliação técnica." onClose={() => setSelected(null)}>
      {selected && <dl className="sp-risk-facts ds-dialog-content"><div><dt>Localização</dt><dd>{selected.location}</dd></div><div><dt>Perigos mapeados</dt><dd>{selected.hazards}</dd></div><div><dt>EPIs vinculados</dt><dd>{selected.ppe}</dd></div></dl>}
      <Button variant="secondary" onClick={() => setSelected(null)}>Fechar consulta</Button>
    </Dialog>
    <Dialog open={registerOpen} title="Cadastrar área" description="Esta tela demonstra a composição visual. O cadastro será conectado à funcionalidade de áreas de risco do projeto." onClose={() => setRegisterOpen(false)}>
      <div className="sp-dialog-actions"><Button variant="secondary" onClick={() => setRegisterOpen(false)}>Voltar à consulta</Button><Link className="sp-button sp-button--primary" to="/design-system#form-example">Ver formulário de exemplo</Link></div>
    </Dialog>
  </AppShell>
}
