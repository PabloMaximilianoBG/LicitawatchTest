package cl.licitawatch.coincidenciasms.controller;

import cl.licitawatch.coincidenciasms.entity.Coincidencia;
import cl.licitawatch.coincidenciasms.repository.CoincidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coincidencias")
@RequiredArgsConstructor
public class CoincidenciaController {

    private final CoincidenciaRepository coincidenciaRepository;

    @GetMapping("/usuario/{usuarioId}")
    public List<Coincidencia> porUsuario(@PathVariable Long usuarioId) {
        return coincidenciaRepository.findByUsuarioIdOrderByScoreDesc(usuarioId);
    }
}
