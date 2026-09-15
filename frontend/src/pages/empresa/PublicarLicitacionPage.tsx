import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { actualizarLicitacion, crearLicitacion, obtenerLicitacion } from "../../api/licitaciones";
import { obtenerMensajeError } from "../../api/client";
import Card from "../../components/ui/Card";
import Input from "../../components/ui/Input";
import Button from "../../components/ui/Button";
import Alert from "../../components/ui/Alert";
import Spinner from "../../components/ui/Spinner";

export default function PublicarLicitacionPage() {
  const { id } = useParams<{ id: string }>();
  const esEdicion = Boolean(id);
  const navigate = useNavigate();

  const [titulo, setTitulo] = useState("");
  const [rubro, setRubro] = useState("");
  const [montoEstimado, setMontoEstimado] = useState("");
  const [region, setRegion] = useState("");
  const [fechaCierre, setFechaCierre] = useState("");
  const [cargandoInicial, setCargandoInicial] = useState(esEdicion);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    obtenerLicitacion(Number(id))
      .then((l) => {
        setTitulo(l.titulo);
        setRubro(l.rubro);
        setMontoEstimado(l.montoEstimado?.toString() ?? "");
        setRegion(l.region);
        setFechaCierre(l.fechaCierre);
      })
      .catch((err) => setError(obtenerMensajeError(err)))
      .finally(() => setCargandoInicial(false));
  }, [id]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setEnviando(true);
    const payload = {
      titulo,
      rubro,
      montoEstimado: montoEstimado ? Number(montoEstimado) : null,
      region,
      fechaCierre,
    };
    try {
      const resultado = esEdicion ? await actualizarLicitacion(Number(id), payload) : await crearLicitacion(payload);
      navigate(`/licitaciones/${resultado.id}`);
    } catch (err) {
      setError(obtenerMensajeError(err, "No se pudo guardar la licitación."));
    } finally {
      setEnviando(false);
    }
  }

  if (cargandoInicial) return <Spinner />;

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="mb-1 text-2xl font-bold text-white">{esEdicion ? "Editar licitación" : "Publicar licitación"}</h1>
      <p className="mb-6 text-sm text-slate-400">
        {esEdicion ? "Actualiza los datos de tu licitación." : "Completa los datos para publicar una nueva licitación."}
      </p>

      <Card>
        {error && (
          <div className="mb-4">
            <Alert tipo="error">{error}</Alert>
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Título" required value={titulo} onChange={(e) => setTitulo(e.target.value)} />
          <Input label="Rubro" required value={rubro} onChange={(e) => setRubro(e.target.value)} />
          <Input label="Región" required value={region} onChange={(e) => setRegion(e.target.value)} />
          <Input
            label="Monto estimado (CLP, opcional)"
            type="number"
            min={0}
            value={montoEstimado}
            onChange={(e) => setMontoEstimado(e.target.value)}
          />
          <Input
            label="Fecha de cierre"
            type="date"
            required
            value={fechaCierre}
            onChange={(e) => setFechaCierre(e.target.value)}
          />
          <Button type="submit" className="w-full" loading={enviando}>
            {esEdicion ? "Guardar cambios" : "Publicar licitación"}
          </Button>
        </form>
      </Card>
    </div>
  );
}
