package cl.licitawatch.usuarios.bs.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bs.client.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

/** Contrato REST de MS.usuarios.bd. */
@HttpExchange("/bd")
public interface UsuarioBdClient {

    @GetExchange("/catalogos/roles")
    List<CatalogoDto> roles();

    @GetExchange("/catalogos/rubros")
    List<CatalogoDto> rubros();

    @GetExchange("/catalogos/rubros/{id}")
    CatalogoDto rubro(@PathVariable Integer id);

    @PostExchange("/catalogos/rubros")
    CatalogoDto crearRubro(@RequestBody NombreDto request);

    @GetExchange("/catalogos/regiones")
    List<CatalogoDto> regiones();

    @GetExchange("/catalogos/regiones/{id}")
    CatalogoDto region(@PathVariable Integer id);

    @GetExchange("/catalogos/regiones/{id}/ciudades")
    List<CiudadDto> ciudades(@PathVariable Integer id);

    @GetExchange("/catalogos/tamanos-empresa")
    List<CatalogoDto> tamanosEmpresa();

    @GetExchange("/usuarios")
    PaginaResponse<PerfilBdDto> listar(@RequestParam(required = false) String rol, @RequestParam(required = false) Boolean activo,
                                       @RequestParam(required = false) String q, @RequestParam int page, @RequestParam int size);

    @PostExchange("/usuarios")
    PerfilBdDto crear(@RequestBody CrearUsuarioBdDto request);

    @GetExchange("/usuarios/{id}")
    UsuarioBdDto obtener(@PathVariable Integer id);

    @GetExchange("/usuarios/buscar")
    UsuarioBdDto buscarPorEmail(@RequestParam String email);

    @GetExchange("/usuarios/existe")
    ExisteDto existeEmail(@RequestParam String email);

    @PatchExchange("/usuarios/{id}")
    UsuarioBdDto actualizar(@PathVariable Integer id, @RequestBody ActualizarUsuarioBdDto request);

    @GetExchange("/usuarios/{id}/perfil")
    PerfilBdDto perfil(@PathVariable Integer id);

    @PostExchange("/usuarios/{id}/perfil")
    PerfilBdDto crearPerfil(@PathVariable Integer id, @RequestBody CrearPerfilBdDto request);

    @PutExchange("/usuarios/{id}/perfil")
    PerfilBdDto actualizarPerfil(@PathVariable Integer id, @RequestBody ActualizarPerfilBdDto request);

    @GetExchange("/usuarios/{id}/perfiles")
    PerfilesUsuarioDto perfiles(@PathVariable Integer id);

    @GetExchange("/perfiles/existe-rut")
    ExisteDto existeRut(@RequestParam String rut, @RequestParam String rol);

    @GetExchange("/licitadores/{id}")
    PerfilBdDto licitador(@PathVariable Integer id);

    @GetExchange("/licitadores")
    List<PerfilBdDto> licitadores(@RequestParam List<Integer> ids);

    @GetExchange("/pymes/{id}")
    PerfilBdDto pyme(@PathVariable Integer id);

    @GetExchange("/pymes")
    List<PerfilBdDto> pymes(@RequestParam List<Integer> ids);

    @GetExchange("/pymes")
    List<PerfilBdDto> pymesPorRubro(@RequestParam Integer rubroId);
}
