CREATE TABLE outbox_evento (
    id           BIGSERIAL PRIMARY KEY,
    tipo_evento  VARCHAR(255)             NOT NULL,
    payload      TEXT                     NOT NULL,
    ocurrido_en  TIMESTAMP WITH TIME ZONE NOT NULL,
    publicado    BOOLEAN                  NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_evento_pendientes ON outbox_evento (id) WHERE NOT publicado;
