ALTER TABLE pedidos
    ADD COLUMN estado             VARCHAR(30) NOT NULL DEFAULT 'CONFIRMADO',
    ADD COLUMN motivo_cancelacion VARCHAR(500);

-- El DEFAULT solo existe para rellenar las filas ya existentes (creadas antes de este capítulo,
-- cuando "guardado" ya significaba "confirmado"). A partir de aquí, Pedido.crear(...) siempre
-- fija el estado explícitamente — un DEFAULT permanente ocultaría el fallo si algún día se olvida.
ALTER TABLE pedidos
    ALTER COLUMN estado DROP DEFAULT;
