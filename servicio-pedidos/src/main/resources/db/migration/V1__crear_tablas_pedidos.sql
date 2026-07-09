CREATE TABLE pedidos (
    id             VARCHAR(36) PRIMARY KEY,
    cliente_id     VARCHAR(36)              NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE lineas_pedido (
    id               BIGSERIAL PRIMARY KEY,
    pedido_id        VARCHAR(36)    NOT NULL REFERENCES pedidos (id),
    producto_id      VARCHAR(36)    NOT NULL,
    cantidad         INTEGER        NOT NULL,
    precio_unitario  NUMERIC(19, 2) NOT NULL
);

CREATE INDEX idx_lineas_pedido_pedido_id ON lineas_pedido (pedido_id);
