import { useEffect, useState } from 'react'

/**
 * Hook para monitorar estado de conectividade de rede com base em eventos do navegador.
 * SRP: Responsabilidade única de observar status online/offline.
 */
export function useNetworkStatus(): { online: boolean } {
  const [online, setOnline] = useState<boolean>(() =>
    typeof navigator !== 'undefined' ? navigator.onLine : true
  )

  useEffect(() => {
    function handleOnline() {
      setOnline(true)
    }

    function handleOffline() {
      setOnline(false)
    }

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }, [])

  return { online }
}
