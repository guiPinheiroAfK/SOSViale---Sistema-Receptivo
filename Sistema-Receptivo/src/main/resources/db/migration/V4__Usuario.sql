CREATE TABLE usuarios (
    id        BIGSERIAL    PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    usuario   VARCHAR(50)  NOT NULL UNIQUE,
    senha     VARCHAR(255) NOT NULL,
    is_admin  BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO usuarios (nome, usuario, senha, is_admin)
VALUES ('Administrador', 'admin', 'admin123', TRUE);