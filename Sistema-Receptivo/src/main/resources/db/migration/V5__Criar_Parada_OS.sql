-- V5__Criar_Parada_OS.sql
CREATE TABLE paradas_os (
                            id              SERIAL PRIMARY KEY,
                            os_id           INT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
                            ordem_parada    INT NOT NULL,
                            local_parada    VARCHAR(100) NOT NULL,
                            latitude        DECIMAL(10,7),
                            longitude       DECIMAL(10,7),
                            horario_previsto TIME,
                            acao            VARCHAR(50),         -- ex: 'EMBARQUE', 'DESEMBARQUE'
                            status_parada   VARCHAR(20) DEFAULT 'PENDENTE'
);

-- Tabela pivot: quais transfers compõem cada parada
CREATE TABLE parada_os_transfers (
                                     parada_os_id  INT REFERENCES paradas_os(id) ON DELETE CASCADE,
                                     transfer_id   INT REFERENCES transfers(id) ON DELETE CASCADE,
                                     PRIMARY KEY (parada_os_id, transfer_id)
);