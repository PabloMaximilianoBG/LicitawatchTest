package cl.licitawatch.usuarios.bs.service;

import cl.licitawatch.usuarios.bs.dto.request.*;
import cl.licitawatch.usuarios.bs.dto.response.LoginResponse;
import cl.licitawatch.usuarios.bs.dto.response.MensajeResponse;
import cl.licitawatch.usuarios.bs.dto.response.RegistroResponse;

public interface AuthService {
    RegistroResponse registrarLicitador(RegistroLicitadorRequest request);

    RegistroResponse registrarPyme(RegistroPymeRequest request);

    LoginResponse login(LoginRequest request);

    MensajeResponse confirmarCuenta(TokenRequest request);

    MensajeResponse reenviarConfirmacion(EmailRequest request);

    MensajeResponse recuperarPassword(EmailRequest request);

    MensajeResponse restablecerPassword(RestablecerPasswordRequest request);
}
