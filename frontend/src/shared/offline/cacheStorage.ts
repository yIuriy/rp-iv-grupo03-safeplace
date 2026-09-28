/**
 * Contrato de armazenamento em cache local para suporte offline (RNF08).
 * Princípio da Segregação de Interfaces (ISP) e Inversão de Dependência (DIP).
 */

export interface CacheEntry<T> {
  dados: T
  timestamp: number
  contaEmail: string
}

export interface CacheStoragePort {
  getItem<T>(chave: string, contaEmail: string): CacheEntry<T> | null
  setItem<T>(chave: string, contaEmail: string, dados: T): void
  removeItem(chave: string, contaEmail: string): void
  limparTudoDaConta(contaEmail: string): void
}

const PREFIXO_CACHE = 'safeplace.cache.'

export class LocalStorageCacheAdapter implements CacheStoragePort {
  private montarChave(chave: string, contaEmail: string): string {
    return `${PREFIXO_CACHE}${contaEmail}.${chave}`
  }

  getItem<T>(chave: string, contaEmail: string): CacheEntry<T> | null {
    try {
      const chaveCompleta = this.montarChave(chave, contaEmail)
      const bruto = window.localStorage.getItem(chaveCompleta)
      if (!bruto) return null
      const entrada = JSON.parse(bruto) as CacheEntry<T>
      if (entrada && entrada.contaEmail === contaEmail && typeof entrada.timestamp === 'number') {
        return entrada
      }
      return null
    } catch {
      return null
    }
  }

  setItem<T>(chave: string, contaEmail: string, dados: T): void {
    try {
      const chaveCompleta = this.montarChave(chave, contaEmail)
      const entrada: CacheEntry<T> = {
        dados,
        timestamp: Date.now(),
        contaEmail,
      }
      window.localStorage.setItem(chaveCompleta, JSON.stringify(entrada))
    } catch {
      // Falha silenciosa de quota ou armazenamento desabilitado
    }
  }

  removeItem(chave: string, contaEmail: string): void {
    try {
      const chaveCompleta = this.montarChave(chave, contaEmail)
      window.localStorage.removeItem(chaveCompleta)
    } catch {
      // Falha silenciosa
    }
  }

  limparTudoDaConta(contaEmail: string): void {
    try {
      const prefixo = `${PREFIXO_CACHE}${contaEmail}.`
      const chavesParaRemover: string[] = []
      for (let i = 0; i < window.localStorage.length; i++) {
        const key = window.localStorage.key(i)
        if (key && key.startsWith(prefixo)) {
          chavesParaRemover.push(key)
        }
      }
      for (const key of chavesParaRemover) {
        window.localStorage.removeItem(key)
      }
    } catch {
      // Falha silenciosa
    }
  }
}

export const defaultCacheStorage: CacheStoragePort = new LocalStorageCacheAdapter()
