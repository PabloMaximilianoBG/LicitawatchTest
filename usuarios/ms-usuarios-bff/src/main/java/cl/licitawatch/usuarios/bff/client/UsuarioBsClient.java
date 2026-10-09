package cl.licitawatch.usuarios.bff.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bff.dto.request.*;
import cl.licitawatch.usuarios.bff.dto.response.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

/** Contrato REST de MS.usuarios.bs. */
@HttpExchange("/bs")
public interface UsuarioBsClient {

    @PostExchange("/auth/registro/licitador")
    RegistroResponse registrarLicitador(@RequestBody RegistroLicitadorRequest request);

    @PostExchange("/auth/registro/pyme")
    RegistroResponse registrarPyme(@RequestBody RegistroPymeRequest request);

    @PostExchange("/auth/login")
    LoginResponse login(@RequestBody LoginRequest request);

    @PostExchange("/auth/confirmar-cuenta")
    MensajeResponse confirmarCuenta(@RequestBody TokenRequest request);

    @PostExchange("/auth/reenviar-confirmacion")
    MensajeResponse reenviarConfirmacion(@RequestBody EmailRequest request);

    @PostExchange("/auth/recuperar-password")
    MensajeResponse recuperarPassword(@RequestBody EmailRequest request);

    @PostExchange("/auth/restablecer-password")
    MensajeResponse restablecerPassword(@RequestBody RestablecerPasswordRequest request);

    @GetExchange("/usuarios/me")
    PerfilResponse me();

    @PutExchange("/usuarios/me")
    PerfilResponse actualizarMe(@RequestBody ActualizarPerfilRequest request);

    @GetExchange("/licitadores/{id}")
    PerfilResponse licitador(@PathVariable Integer id);

    @GetExchange("/pymes/{id}")
    PerfilResponse pyme(@PathVariable Integer id);

    @GetExchange("/catalogos/rubros")
    List<CatalogoResponse> rubros();

    @GetExchange("/catalogos/regiones")
    List<CatalogoResponse> regiones();

    @GetExchange("/catalogos/regiones/{id}/ciudades")
    List<CiudadResponse> ciudades(@PathVariable Integer id);

    @GetExchange("/catalogos/tamanos-empresa")
    List<CatalogoResponse> tamanosEmpresa();

    @GetExchange("/admin/usuarios")
    PaginaResponse<PerfilResponse> adminListar(@RequestParam(required = false) String rol, @RequestParam(required = false) Boolean activo,
                                               @RequestParam(required = false) String q, @RequestParam int page, @RequestParam int size);

    @GetExchange("/admin/usuarios/{id}")
    PerfilResponse adminObtener(@PathVariable Integer id);

    @PostExchange("/admin/usuarios")
    PerfilResponse adminCrear(@RequestBody AdminCrearUsuarioRequest request);

    @PutExchange("/admin/usuarios/{id}/perfil")
    PerfilResponse adminActualizarPerfil(@PathVariable Integer id, @RequestBody ActualizarPerfilRequest request);

    @PatchExchange("/admin/usuarios/{id}/estado")
    PerfilResponse adminCambiarEstado(@PathVariable Integer id, @RequestBody CambiarEstadoRequest request);

    @PatchExchange("/admin/usuarios/{id}/rol")
    PerfilResponse adminCambiarRol(@PathVariable Integer id, @RequestBody CambiarRolRequest request);

    @PostExchange("/admin/catalogos/rubros")
    CatalogoResponse adminCrearRubro(@RequestBody RubroRequest request);
}
