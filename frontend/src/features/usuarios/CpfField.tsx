import type { ComponentProps, InputEvent } from 'react'
import { TextField } from '../../shared/components'
import { mascararCpf } from './formatos'

/**
 * Campo de CPF com máscara `000.000.000-00` aplicada enquanto o usuário digita. O campo continua
 * não controlado: o valor mascarado fica no próprio `<input>` e é lido pelo `FormData` do formulário.
 */
export function CpfField({ defaultValue, onInput, ...props }: ComponentProps<typeof TextField>) {
  function mascarar(event: InputEvent<HTMLInputElement>) {
    const campo = event.currentTarget
    campo.value = mascararCpf(campo.value)
    onInput?.(event)
  }
  return <TextField {...props} inputMode="numeric" maxLength={14} autoComplete="off"
    defaultValue={typeof defaultValue === 'string' ? mascararCpf(defaultValue) : defaultValue} onInput={mascarar} />
}
