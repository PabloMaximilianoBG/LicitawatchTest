import { useEffect, useState } from 'react'
import { catalogosApi } from '@/api/services'
import type { Catalogo, Ciudad } from '@/types'

let cache: { rubros: Catalogo[]; regiones: Catalogo[]; tamanos: Catalogo[] } | null = null

/** Catálogos del ER (rubro, region, tamano_empresa) para los combobox: nunca texto libre. */
export function useCatalogos() {
  const [data, setData] = useState(cache)
  useEffect(() => {
    if (cache) return
    Promise.all([catalogosApi.rubros(), catalogosApi.regiones(), catalogosApi.tamanos()])
      .then(([rubros, regiones, tamanos]) => { cache = { rubros, regiones, tamanos }; setData(cache) })
      .catch(() => undefined)
  }, [])
  return data ?? { rubros: [], regiones: [], tamanos: [] }
}

export function invalidarCatalogos() {
  cache = null
}

/** Ciudades de una región (combobox encadenado región -> ciudad). */
export function useCiudades(regionId: number | '' | null | undefined) {
  const [ciudades, setCiudades] = useState<Ciudad[]>([])
  useEffect(() => {
    if (!regionId) { setCiudades([]); return }
    catalogosApi.ciudades(Number(regionId)).then(setCiudades).catch(() => setCiudades([]))
  }, [regionId])
  return ciudades
}
