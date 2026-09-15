-- El plan Estandar es gratuito y viene activo por defecto en toda cuenta
-- (no requiere pago via Webpay); solo Premium se contrata. Corrige el precio
-- sembrado en V1 para instalaciones que ya habian arrancado antes de este
-- cambio - en instalaciones nuevas, PlanSeeder ya siembra el precio correcto
-- y esta migracion simplemente no encuentra nada que actualizar.
UPDATE plan_suscripcion
SET precio = 0,
    descripcion = 'Plan gratuito, incluido por defecto en toda cuenta nueva - acceso funcional con limite mensual.'
WHERE nombre = 'ESTANDAR';
