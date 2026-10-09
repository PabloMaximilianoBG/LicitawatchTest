package cl.licitawatch.ventas.bd.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.ventas.bd.dto.request.PagoBdRequest;
import cl.licitawatch.ventas.bd.dto.request.SuscripcionBdRequest;
import cl.licitawatch.ventas.bd.dto.request.VentaBdRequest;
import cl.licitawatch.ventas.bd.dto.response.*;
import cl.licitawatch.ventas.bd.entity.*;
import cl.licitawatch.ventas.bd.mapper.VentasBdMapper;
import cl.licitawatch.ventas.bd.repository.*;
import cl.licitawatch.ventas.bd.service.VentasDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentasDataServiceImpl implements VentasDataService {
    private final PlanSuscripcionRepository planRepository;
    private final EstadoSuscripcionRepository estadoSuscripcionRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;
    private final VentasBdMapper mapper;

    @Override
    public List<CatalogoResponse> planes() {
        return planRepository.findAll(Sort.by("id")).stream().map(p -> new CatalogoResponse(p.getId(), p.getNombre())).toList();
    }

    @Override
    public PaginaResponse<SuscripcionBdResponse> suscripciones(Integer usuarioId, String estado, String plan, int page, int size) {
        Specification<Suscripcion> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> p = new ArrayList<>();
            if (usuarioId != null) {
                p.add(cb.equal(root.get("usuarioId"), usuarioId));
            }
            if (estado != null && !estado.isBlank()) {
                p.add(cb.equal(cb.lower(root.get("estadoSuscripcion").get("nombre")), estado.toLowerCase()));
            }
            if (plan != null && !plan.isBlank()) {
                p.add(cb.equal(cb.lower(root.get("plan").get("nombre")), plan.toLowerCase()));
            }
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<Suscripcion> pagina = suscripcionRepository.findAll(spec, PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id")));
        return PaginaResponse.de(pagina, this::suscripcionDto);
    }

    @Override
    public List<SuscripcionBdResponse> suscripcionesDeUsuario(Integer usuarioId) {
        return suscripcionRepository.findByUsuarioIdOrderByIdDesc(usuarioId).stream().map(this::suscripcionDto).toList();
    }

    @Override
    public SuscripcionBdResponse suscripcion(Integer id) {
        return suscripcionDto(suscripcionEntidad(id));
    }

    @Override
    @Transactional
    public SuscripcionBdResponse crearSuscripcion(SuscripcionBdRequest r) {
        Suscripcion s = suscripcionRepository.save(Suscripcion.builder().usuarioId(r.usuarioId()).plan(plan(r.plan()))
                .estadoSuscripcion(estadoSuscripcion(r.estado())).build());
        return suscripcionDto(s);
    }

    @Override
    @Transactional
    public SuscripcionBdResponse cambiarEstadoSuscripcion(Integer id, String estado) {
        Suscripcion s = suscripcionEntidad(id);
        s.setEstadoSuscripcion(estadoSuscripcion(estado));
        return suscripcionDto(s);
    }

    @Override
    public List<Integer> usuariosPremium(List<Integer> usuarioIds) {
        return usuarioIds == null || usuarioIds.isEmpty() ? List.of() : suscripcionRepository.usuariosPremiumActivos(usuarioIds);
    }

    @Override
    public List<SuscripcionBdResponse> premiumActivas() {
        return suscripcionRepository.findByPlanNombreAndEstadoSuscripcionNombre("Premium", "Activa").stream()
                .map(this::suscripcionDto).toList();
    }

    @Override
    @Transactional
    public VentaBdResponse crearVenta(VentaBdRequest r) {
        Venta v = ventaRepository.save(Venta.builder().suscripcion(suscripcionEntidad(r.suscripcionId())).monto(r.monto())
                .fecha(r.fecha()).build());
        return mapper.venta(v, null);
    }

    @Override
    public VentaBdResponse venta(Integer id) {
        Venta v = ventaEntidad(id);
        return mapper.venta(v, pagoRepository.findByVentaId(id).orElse(null));
    }

    @Override
    public PaginaResponse<VentaBdResponse> ventas(Integer usuarioId, int page, int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
        Page<Venta> pagina = usuarioId == null ? ventaRepository.findAll(pr) : ventaRepository.findBySuscripcionUsuarioId(usuarioId, pr);
        Map<Integer, Pago> pagos = pagoRepository.findByVentaIdIn(pagina.getContent().stream().map(Venta::getId).toList()).stream()
                .collect(Collectors.toMap(p -> p.getVenta().getId(), Function.identity()));
        return PaginaResponse.de(pagina, v -> mapper.venta(v, pagos.get(v.getId())));
    }

    @Override
    public List<VentaBdResponse> ventasPendientesPremium(Integer usuarioId) {
        return ventaRepository.pendientesPremium(usuarioId).stream().map(v -> mapper.venta(v, null)).toList();
    }

    @Override
    @Transactional
    public VentaBdResponse registrarPago(Integer ventaId, PagoBdRequest r) {
        Venta v = ventaEntidad(ventaId);
        if (pagoRepository.findByVentaId(ventaId).isPresent()) {
            throw new ConflictException("PAGO_YA_REGISTRADO", "La venta ya tiene un pago registrado");
        }
        Pago p = pagoRepository.save(Pago.builder().venta(v).idTransaccion(r.idTransaccion())
                .metodoPago(metodoPagoRepository.findByNombreIgnoreCase(r.metodo())
                        .orElseThrow(() -> new BadRequestException("METODO_INVALIDO", "Método de pago inválido")))
                .estadoPago(estadoPagoRepository.findByNombreIgnoreCase(r.estado())
                        .orElseThrow(() -> new BadRequestException("ESTADO_INVALIDO", "Estado de pago inválido")))
                .build());
        if (r.activarSuscripcion()) {
            Suscripcion nueva = v.getSuscripcion();
            EstadoSuscripcion cancelada = estadoSuscripcion("Cancelada");
            for (Suscripcion s : suscripcionRepository.findByUsuarioIdAndEstadoSuscripcionNombre(nueva.getUsuarioId(), "Activa")) {
                if (!s.getId().equals(nueva.getId())) {
                    s.setEstadoSuscripcion(cancelada);
                }
            }
            nueva.setEstadoSuscripcion(estadoSuscripcion("Activa"));
        }
        return mapper.venta(v, p);
    }

    @Override
    public ResumenBdResponse resumen() {
        return new ResumenBdResponse(ventaRepository.totalAprobado(), pagoRepository.countByEstadoPagoNombre("Aprobado"),
                pagoRepository.countByEstadoPagoNombre("Rechazado"), ventaRepository.contarSinPago(),
                suscripcionRepository.countByPlanNombreAndEstadoSuscripcionNombre("Premium", "Activa"),
                suscripcionRepository.countByPlanNombreAndEstadoSuscripcionNombre("Estándar", "Activa"));
    }

    private SuscripcionBdResponse suscripcionDto(Suscripcion s) {
        List<Pago> aprobados = pagoRepository.aprobadosDeSuscripcion(s.getId());
        List<Venta> ventas = ventaRepository.findBySuscripcionIdOrderByIdDesc(s.getId());
        boolean sinPago = !ventas.isEmpty() && pagoRepository.findByVentaIdIn(ventas.stream().map(Venta::getId).toList()).size() < ventas.size();
        return SuscripcionBdResponse.builder().id(s.getId()).usuarioId(s.getUsuarioId()).plan(s.getPlan().getNombre())
                .estado(s.getEstadoSuscripcion().getNombre())
                .fechaUltimoPagoAprobado(aprobados.isEmpty() ? null : aprobados.get(0).getVenta().getFecha())
                .tieneVentaSinPago(sinPago).build();
    }

    private Suscripcion suscripcionEntidad(Integer id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUSCRIPCION_NO_ENCONTRADA", "La suscripción no existe"));
    }

    private Venta ventaEntidad(Integer id) {
        return ventaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("VENTA_NO_ENCONTRADA", "La venta no existe"));
    }

    private PlanSuscripcion plan(String nombre) {
        return planRepository.findByNombreIgnoreCase(nombre).orElseThrow(() -> new BadRequestException("PLAN_INVALIDO", "Plan inválido"));
    }

    private EstadoSuscripcion estadoSuscripcion(String nombre) {
        return estadoSuscripcionRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new BadRequestException("ESTADO_INVALIDO", "Estado de suscripción inválido"));
    }
}
