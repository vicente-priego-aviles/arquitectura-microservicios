CREATE TABLE stock (
    producto_id VARCHAR(36) PRIMARY KEY,
    cantidad    INTEGER     NOT NULL
);

CREATE TABLE pedidos_procesados (
    pedido_id    VARCHAR(36) PRIMARY KEY,
    procesado_en TIMESTAMP WITH TIME ZONE NOT NULL
);
