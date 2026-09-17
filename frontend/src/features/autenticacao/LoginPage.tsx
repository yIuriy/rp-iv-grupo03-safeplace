import { useEffect, useRef, useState, type FormEvent } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import logo from '../../../design-system/assets/logo-primary.svg'
import { Alert, Button, TextField } from '../../shared/components'
import { statusDoErro } from '../../shared/api/erros'
import { useAutenticacao } from './contexto'

const destinoPadrao = '/usuarios'

/** Volta à tela que pediu o login, se ela veio de `ExigirSessao`; senão abre a tela inicial. */
function destinoDe(state: unknown): string {
  const de = state !== null && typeof state === 'object' ? (state as { de?: unknown }).de : undefined
  return typeof de === 'string' && de.startsWith('/') && !de.startsWith('/login') ? de : destinoPadrao
}

function descreverFalha(erro: unknown): string {
  return statusDoErro(erro) === 401
    ? 'E-mail ou senha incorretos. Confira os dados e tente novamente.'
    : 'Não foi possível entrar agora. Verifique sua conexão e tente novamente.'
}

export function LoginPage() {
  const { sessao, motivoSaida, entrar } = useAutenticacao()
  const { state } = useLocation()
  const [erros, setErros] = useState<{ email?: string; senha?: string }>({})
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)
  const emailRef = useRef<HTMLInputElement>(null)
  const senhaRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    const anterior = document.title
    document.title = 'Entrar | SafePlace'
    return () => { document.title = anterior }
  }, [])

  if (sessao) return <Navigate to={destinoDe(state)} replace />

  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando) return
    const dados = new FormData(event.currentTarget)
    const email = String(dados.get('email') ?? '').trim()
    const senha = String(dados.get('senha') ?? '')
    const pendencias = {
      email: email ? undefined : 'Informe o e-mail de acesso.',
      senha: senha ? undefined : 'Informe a senha.',
    }
    setErros(pendencias)
    if (pendencias.email || pendencias.senha) {
      ;(pendencias.email ? emailRef : senhaRef).current?.focus()
      return
    }
    setFalha('')
    setEnviando(true)
    try {
      await entrar(email, senha)
    } catch (erro) {
      setFalha(descreverFalha(erro))
      setEnviando(false)
      senhaRef.current?.focus()
    }
  }

  return <main id="main-content" className="sp-login-page">
    <section className="sp-login-card" aria-labelledby="login-titulo">
      <img src={logo} alt="SafePlace" className="sp-login-logo" />
      <div className="sp-login-heading">
        <h1 id="login-titulo" className="sp-type-heading-md">Entrar no SafePlace</h1>
        <p>Acesso para Gestores de Segurança e Supervisores com o e-mail corporativo e a senha recebida.</p>
      </div>
      {motivoSaida === 'expirada' && !falha && <Alert tone="warning" title="Sua sessão expirou" live="polite">
        Entre novamente para continuar de onde parou.
      </Alert>}
      {motivoSaida === 'manual' && !falha && <Alert tone="info" title="Você saiu do SafePlace" live="polite">
        Entre novamente quando precisar.
      </Alert>}
      <form className="sp-form" onSubmit={enviar} noValidate>
        <TextField label="E-mail" name="email" type="email" autoComplete="username" ref={emailRef}
          required error={erros.email} />
        <TextField label="Senha" name="senha" type="password" autoComplete="current-password" ref={senhaRef}
          required error={erros.senha} />
        {falha && <Alert tone="danger" title="Não foi possível entrar" live="assertive">{falha}</Alert>}
        <Button type="submit" loading={enviando} className="sp-login-submit">Entrar</Button>
      </form>
    </section>
  </main>
}
