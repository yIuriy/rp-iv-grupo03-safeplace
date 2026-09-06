import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { Catalog } from './features/design-system/Catalog'
import { RiskAreasExample } from './features/design-system/RiskAreasExample'

function App() {
  return <BrowserRouter><Routes>
    <Route path="/" element={<Catalog />} />
    <Route path="/design-system" element={<Catalog />} />
    <Route path="/design-system/examples/risk-areas" element={<RiskAreasExample />} />
    <Route path="*" element={<main className="sp-main"><h1 className="sp-type-heading-md">Página não encontrada</h1><a href="/design-system">Voltar ao catálogo</a></main>} />
  </Routes></BrowserRouter>
}

export default App
