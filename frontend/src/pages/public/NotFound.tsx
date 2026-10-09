import { Link } from 'react-router-dom'
import { Button, NotFoundState } from '@/components/ui'

export default function NotFound() {
  return (
    <div className="grid min-h-screen place-items-center bg-ink-50 px-4">
      <NotFoundState message="La página que buscas no existe." />
      <Link to="/" className="-mt-10"><Button variant="secondary">Volver al inicio</Button></Link>
    </div>
  )
}
