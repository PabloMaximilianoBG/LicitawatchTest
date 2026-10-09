package cl.licitawatch.ventas.bs.service;

import cl.licitawatch.ventas.bs.dto.response.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> listar();
}
