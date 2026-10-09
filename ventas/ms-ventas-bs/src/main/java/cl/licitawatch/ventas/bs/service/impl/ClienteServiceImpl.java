package cl.licitawatch.ventas.bs.service.impl;

import cl.licitawatch.ventas.bs.client.UsuarioClient;
import cl.licitawatch.ventas.bs.client.dto.UsuarioDto;
import cl.licitawatch.ventas.bs.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final UsuarioClient usuarios;

    @Override
    public Map<Integer, UsuarioDto> clientes(List<Integer> usuarioIds) {
        Map<Integer, UsuarioDto> m = new HashMap<>();
        for (Integer id : new LinkedHashSet<>(usuarioIds)) {
            try {
                m.put(id, usuarios.usuario(id));
            } catch (Exception e) {
                log.debug("Usuario {} no disponible: {}", id, e.getMessage());
            }
        }
        return m;
    }
}
