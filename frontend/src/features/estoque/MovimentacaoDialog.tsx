// Diálogo de registro de entrada ou saída de estoque (UC06, passos 5 a 8), exclusivo do Gestor:
// tipo, quantidade e motivo, validação local, envio à API e exibição da recusa do servidor
// (por exemplo saída acima do saldo) mantendo os dados preenchidos. O fluxo de envio vem do hook
// useEnvioFormulario (Template Method, #243).

import { Alert, Button, Dialog, Radio, TextArea, TextField } from '../../shared/components'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import { descreverFalhaDeMovimentacao, registrarMovimentacao, type Epi, type Movimentacao } from './api'
import { lerDadosMovimentacao, ordemDosCamposDeMovimentacao, validarMovimentacao, type ErrosMovimentacao } from './validacaoMovimentacao'

export function MovimentacaoDialog({ epi, aoFechar, aoSalvar }: {
  epi: Epi | null
  aoFechar: () => void
  aoSalvar: (movimentacao: Movimentacao) => void
}) {
  // O formulário só existe com o diálogo aberto: fechar descarta erros, falha e estado de envio.
  return <Dialog open={epi !== null} title="Registrar movimentação" onClose={aoFechar}
    description={epi ? `${epi.nome}: saldo atual ${epi.quantidade}, mínimo ${epi.estoqueMinimo}. Data, hora e operador são registrados automaticamente.` : undefined}>
    {epi && <MovimentacaoFormulario key={epi.id} epi={epi} aoFechar={aoFechar} aoSalvar={aoSalvar} />}
  </Dialog>
}

function MovimentacaoFormulario({ epi, aoFechar, aoSalvar }: {
  epi: Epi
  aoFechar: () => void
  aoSalvar: (movimentacao: Movimentacao) => void
}) {
  const { enviando, erros, falha, onSubmit } = useEnvioFormulario({
    ler: form => lerDadosMovimentacao(new FormData(form)),
    validar: dados => {
      const pendencias = validarMovimentacao(dados, epi.quantidade)
      return { dados: ordemDosCamposDeMovimentacao.some(campo => pendencias[campo]) ? null : dados, erros: pendencias }
    },
    ordem: ordemDosCamposDeMovimentacao,
    enviar: async dados => {
      aoSalvar(await registrarMovimentacao(epi.id, dados))
      aoFechar()
    },
    descreverFalha: descreverFalhaDeMovimentacao,
  }, {} as ErrosMovimentacao)

  return <form className="sp-form" onSubmit={onSubmit} noValidate>
    <fieldset className="sp-fieldset">
      <legend className="sp-field-label">Tipo de movimentação</legend>
      <div className="sp-opcoes"><Radio label="Entrada (reposição)" name="tipo" value="ENTRADA" defaultChecked />
        <Radio label="Saída (baixa)" name="tipo" value="SAIDA" /></div>
    </fieldset>
    <TextField label="Quantidade" name="quantidade" type="number" inputMode="numeric" min={1} step={1} required
      error={erros.quantidade} helper="Unidades a movimentar. Uma saída não pode deixar o saldo negativo." />
    <TextArea label="Motivo" name="motivo" required rows={3} error={erros.motivo}
      helper="Ex.: recebimento de lote, entrega à equipe, ajuste de inventário." />
    {falha && <Alert tone="danger" title="Movimentação não registrada" live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoFechar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">Registrar</Button>
    </div>
  </form>
}
