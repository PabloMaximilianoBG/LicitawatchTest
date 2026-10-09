package cl.licitawatch.licitaciones.bs.client;

import cl.licitawatch.licitaciones.bs.client.dto.CatalogoDto;
import cl.licitawatch.licitaciones.bs.client.dto.PerfilDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/** Contrato REST de MS.usuarios.bs: valida las REF (licitador_id, pyme_id, rubro_id, region_id). */
@HttpExchange("/bs")
public interface UsuarioClient {

    @GetExchange("/catalogos/rubros")
    List<CatalogoDto> rubros();

    @GetExchange("/catalogos/rubros/{id}")
    CatalogoDto rubro(@PathVariable Integer id);

    @GetExchange("/catalogos/regiones")
    List<CatalogoDto> regiones();

    @GetExchange("/catalogos/regiones/{id}")
    CatalogoDto region(@PathVariable Integer id);

    @GetExchange("/licitadores/{id}")
    PerfilDto licitador(@PathVariable Integer id);

    @GetExchange("/licitadores")
    List<PerfilDto> licitadores(@RequestParam List<Integer> ids);

    @GetExchange("/pymes/{id}")
    PerfilDto pyme(@PathVariable Integer id);

    @GetExchange("/pymes")
    List<PerfilDto> pymes(@RequestParam List<Integer> ids);

    @GetExchange("/pymes")
    List<PerfilDto> pymesPorRubro(@RequestParam Integer rubroId);
}
